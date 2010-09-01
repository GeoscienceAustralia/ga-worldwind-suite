package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.media.opengl.GLCapabilities;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nasa.worldwind.awt.WorldWindowGLCanvas;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationWriter;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationReader;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.layers.camerapath.CameraPathLayer;
import au.gov.ga.worldwind.animator.layers.depth.DepthLayer;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedBasicElevationModelFactory;
import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedElevationModel;
import au.gov.ga.worldwind.animator.layers.elevation.pervetex.ElevationTesselator;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateRetrievalService;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateTaskService;
import au.gov.ga.worldwind.animator.layers.other.ImmediateBMNGWMSLayer;
import au.gov.ga.worldwind.animator.layers.other.ImmediateLandsatI3WMSLayer;
import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;
import au.gov.ga.worldwind.animator.util.ChangeFrameListener;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.animator.util.FrameSlider;
import au.gov.ga.worldwind.animator.view.orbit.BasicOrbitView;
import au.gov.ga.worldwind.common.util.message.MessageSource;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.ResourceBundleMessageSource;

public class Animator
{
	private final static String DATA_DRIVE = "Z";

	private static final GLCapabilities caps = new GLCapabilities();

	static
	{
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(24);
		caps.setSampleBuffers(true);
		caps.setDoubleBuffered(true);
		caps.setNumSamples(4);
	}

	static
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");

	}

	public static void main(String[] args)
	{
		new Animator();
	}

	private JFrame frame;

	private WorldWindowGLCanvas wwd;
	private FrameSlider slider;
	private Animation animation = null;
	private File file = null;
	private boolean autokey = false;
	private boolean applying = false;
	private Updater updater;
	private boolean changed = false;
	private boolean stop = false;
	private boolean settingSlider = false;
	private ChangeListener animationChangeListener;
	private Layer crosshair;
	private JCheckBoxMenuItem useScaledZoomCheck;

	private DetailedElevationModel dem;
	private Layer bmng, landsat;

	/** The message source for the application */
	private MessageSource messageSource;

	public Animator()
	{
		Logging.logger().setLevel(Level.FINER);

		setupMessageSource();

		// ImmediateMode.setImmediate(true);

		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicOrbitView.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnimatorSceneController.class.getName());

		Configuration.setValue(AVKey.TASK_SERVICE_CLASS_NAME, ImmediateTaskService.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ImmediateRetrievalService.class.getName());

		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, ElevationTesselator.class.getName());

		Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, 16777216L * 8); // 128 mb

		animationChangeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				changed = true;
				setTitleBar();
			}
		};

		wwd = new WorldWindowGLCanvas(caps);
		animation = new WorldWindAnimationImpl(wwd);
		resetChanged();
		updater = new Updater();

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				quit();
			}
		});

		frame.setLayout(new BorderLayout());
		Model model = new BasicModel();
		wwd.setModel(model);
		setAnimationSize(animation.getRenderParameters().getImageDimension());
		frame.add(wwd, BorderLayout.CENTER);
		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(Angle.ZERO, Angle.POS180);

		CompoundElevationModel cem = new CompoundElevationModel();
		dem = new DetailedElevationModel(cem);
		model.getGlobe().setElevationModel(dem);

		LayerList layers = model.getLayers();

		Layer depth = new DepthLayer();
		layers.add(depth);

		bmng = new ImmediateBMNGWMSLayer();
		layers.add(bmng);

		landsat = new ImmediateLandsatI3WMSLayer();
		layers.add(landsat);

		ElevationModel earthem = (ElevationModel) new ExtendedBasicElevationModelFactory().createFromConfigSource("config/Earth/EarthElevationModelAsBil16.xml", null);
		ExtendedElevationModel eem = getEBEM(earthem);
		cem.addElevationModel(eem);

		CameraPathLayer cameraPathLayer = new CameraPathLayer(animation);
		layers.add(cameraPathLayer);
		
		crosshair = new CrosshairLayer();
		layers.add(crosshair);

		JPanel bottom = new JPanel(new BorderLayout());
		frame.add(bottom, BorderLayout.SOUTH);

		slider = new FrameSlider(0, 0, animation.getFrameCount());
		slider.setMinimumSize(new Dimension(0, 54));
		bottom.add(slider, BorderLayout.CENTER);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if (!settingSlider)
				{
					if (animation.getKeyFrameCount() > 0)
					{
						applyAnimationState();
						wwd.redraw();
					}
					stop = true;
				}
			}
		});
		slider.addChangeFrameListener(new ChangeFrameListener()
		{
			public void frameChanged(int index, int oldFrame, int newFrame)
			{
				KeyFrame oldKey = animation.getKeyFrame(oldFrame);
				KeyFrame newKey = new KeyFrameImpl(newFrame, oldKey.getParameterValues());

				animation.removeKeyFrame(oldKey);
				animation.insertKeyFrame(newKey);
				
				updateSlider();
				applyAnimationState();
				wwd.redraw();
			}
		});

		getView().addPropertyChangeListener(AVKey.VIEW, new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (autokey && !applying)
				{
					addFrame();
				}
			}
		});

		StatusBar statusBar = new StatusBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		bottom.add(statusBar, BorderLayout.SOUTH);
		statusBar.setEventSource(wwd);

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		createMenuBar();

		setTitleBar();
		updateSlider();

		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Sets up the message source used in the application.
	 */
	private void setupMessageSource()
	{
		messageSource = new ResourceBundleMessageSource("au.gov.ga.worldwind.animator.data.messages.animatorMessages",
														"au.gov.ga.worldwind.common.data.messages.commonUIMessages");
		MessageSourceAccessor.set(messageSource);
	}

	private ExtendedElevationModel getEBEM(ElevationModel elevationModel)
	{
		if (elevationModel instanceof ExtendedElevationModel)
		{
			return (ExtendedElevationModel) elevationModel;
		}
		else if (elevationModel instanceof CompoundElevationModel)
		{
			CompoundElevationModel cem = (CompoundElevationModel) elevationModel;
			for (ElevationModel model : cem.getElevationModels())
			{
				ExtendedElevationModel ebem = getEBEM(model);
				if (ebem != null)
				{
					return ebem;
				}
			}
		}
		return null;
	}

	private void setAnimationSize(Dimension animationSize)
	{
		if (!frame.isVisible())
		{
			wwd.setMinimumSize(animationSize);
			wwd.setMaximumSize(animationSize);
			wwd.setPreferredSize(animationSize);
			wwd.setSize(animationSize);
			frame.pack();
		}
		else
		{
			Dimension wwdSize = wwd.getSize();
			Dimension frameSize = frame.getSize();
			int deltaWidth = frameSize.width - wwdSize.width;
			int deltaHeight = frameSize.height - wwdSize.height;

			frameSize = new Dimension(animationSize.width + deltaWidth, animationSize.height + deltaHeight);
			frame.setPreferredSize(frameSize);
			frame.setMinimumSize(frameSize);
			frame.setMaximumSize(frameSize);
			frame.setSize(frameSize);
			frame.pack();

			wwdSize = wwd.getSize();
			if (wwdSize.width != animationSize.width || wwdSize.height != animationSize.height)
			{
				JOptionPane.showMessageDialog(frame, "Could not set animation dimensions to " + animationSize.width
						+ "x" + animationSize.height + " (currently " + wwdSize.width + "x" + wwdSize.height + ")",
						"Could not set dimensions", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menu.setMnemonic('F');
		menuBar.add(menu);

		menuItem = new JMenuItem("New");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('N', ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('N');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newFile();
			}
		});

		menuItem = new JMenuItem("Open...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('O', ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('O');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				open();
			}
		});

		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('S', ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('S');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save();
			}
		});

		menuItem = new JMenuItem("Save As...");
		menuItem.setMnemonic('A');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveAs();
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Exit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		menuItem.setMnemonic('x');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});


		menu = new JMenu("Frame");
		menu.setMnemonic('r');
		menuBar.add(menu);

		menuItem = new JMenuItem("Add key");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		menuItem.setMnemonic('A');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFrame();
			}
		});

		menuItem = new JMenuItem("Delete key");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menuItem.setMnemonic('D');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int frame = slider.getValue();
				if (frame >= 0)
				{
					animation.removeKeyFrame(frame);
				}
				updateSlider();
			}
		});

		menu.addSeparator();

		/*menuItem = new JMenuItem("Set key in percent");
		menuItem.setMnemonic('I');
		menu.add(menuItem);
		menu
		
		menu.addSeparator();*/

		final JCheckBoxMenuItem autoKeyItem = new JCheckBoxMenuItem("Auto key", autokey);
		menuItem.setMnemonic('k');
		menu.add(autoKeyItem);
		autoKeyItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				autokey = autoKeyItem.isSelected();
			}
		});

		menuItem = new JMenuItem("Set frame count...");
		menuItem.setMnemonic('c');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int frames = slider.getLength() - 1;
				Object value =
						JOptionPane.showInputDialog(frame, "Number of frames:", "Set frame count",
								JOptionPane.QUESTION_MESSAGE, null, null, frames);
				try
				{
					frames = Integer.parseInt((String) value);
				}
				catch (Exception ex)
				{
				}
				animation.setFrameCount(frames);
				updateSlider();
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Previous");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
		menuItem.setMnemonic('P');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 1);
			}
		});

		menuItem = new JMenuItem("Next");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
		menuItem.setMnemonic('N');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 1);
			}
		});

		menuItem = new JMenuItem("Previous 10");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 10);
			}
		});

		menuItem = new JMenuItem("Next 10");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 10);
			}
		});

		menuItem = new JMenuItem("First");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('F');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFrameOfFirstKeyFrame());
			}
		});

		menuItem = new JMenuItem("Last");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('L');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFrameOfLastKeyFrame());
			}
		});


		menu = new JMenu("Animation");
		menu.setMnemonic('A');
		menuBar.add(menu);

		useScaledZoomCheck = new JCheckBoxMenuItem("Use scaled zoom");
		useScaledZoomCheck.setSelected(animation.isZoomScalingRequired());
		menu.add(useScaledZoomCheck);
		useScaledZoomCheck.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animation.setZoomScalingRequired(useScaledZoomCheck.isSelected());
			}
		});

		menuItem = new JMenuItem("Scale animation...");
		menuItem.setMnemonic('S');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame, "Scale factor:", "Scale animation", JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
				try
				{
					scale = Double.parseDouble((String) value);
				}
				catch (Exception ex)
				{
				}
				if (scale != 1.0 && scale > 0)
				{
					animation.scale(scale);
				}
				updateSlider();
			}
		});

		menuItem = new JMenuItem("Scale height...");
		menuItem.setMnemonic('h');
		menu.add(menuItem);
		// TODO: Implement height scaling
		//		menuItem.addActionListener(new ActionListener()
		//		{
		//			public void actionPerformed(ActionEvent e)
		//			{
		//				double scale = -1.0;
		//				Object value =
		//						JOptionPane.showInputDialog(frame, "Scale factor:", "Scale height",
		//								JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
		//				try
		//				{
		//					scale = Double.parseDouble((String) value);
		//				}
		//				catch (Exception ex)
		//				{
		//				}
		//				if (scale != 1.0 && scale > 0)
		//				{
		//					animation.scaleHeight(scale);
		//				}
		//			}
		//		});

		menuItem = new JMenuItem("Smooth eye speed");
		menuItem.setMnemonic('m');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showConfirmDialog(frame,
												  "This will redistribute keyframes to attempt to smooth the eye speed.\nDo you wish to continue?",
												  "Smooth eye speed", 
												  JOptionPane.YES_NO_OPTION, 
												  JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					animation.getCamera().smoothEyeSpeed(createAnimationContext());
					updateSlider();
				}
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Preview");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		menuItem.setMnemonic('P');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(1);
			}
		});

		menuItem = new JMenuItem("Preview x2");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(2);
			}
		});

		menuItem = new JMenuItem("Preview x10");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(10);
			}
		});

		menuItem = new JMenuItem("Render (high-res)...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('R', ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('R');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(1);
			}
		});

		menuItem = new JMenuItem("Render (standard-res)...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(0);
			}
		});
		
		menuItem = new JMenuItem("Custom render (TO BE REMOVED!)");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Thread thread = new Thread(new Runnable()
				{
					public void run()
					{
						double detail = 1.0;

						int first = animation.getFrameOfFirstKeyFrame();
						int last = animation.getFrameOfLastKeyFrame();

						first = 590;
						last = 595;

						// last = 600;

						animate(detail, first, last, new File("frames"), false);

						/*joinThread(animate(
								detail,
								0,
								1,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));*/

						/*
						int endFirst = 2000;
						int startSecond = 7000;
						
						alos.setEnabled(true);
						map1.setEnabled(false);
						map2.setEnabled(false);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/landsat")));

						alos.setEnabled(false);
						map1.setEnabled(false);
						map2.setEnabled(false);
						roads.setEnabled(true);

						joinThread(animate(
								detail,
								first,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/roads")));

						alos.setEnabled(false);
						map1.setEnabled(true);
						map2.setEnabled(false);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								endFirst,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));
						joinThread(animate(
								detail,
								startSecond,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map1")));

						alos.setEnabled(false);
						map1.setEnabled(false);
						map2.setEnabled(true);
						roads.setEnabled(false);

						joinThread(animate(
								detail,
								first,
								endFirst,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map2")));
						joinThread(animate(
								detail,
								startSecond,
								last,
								new File(
										"F:/West Macs Imagery/animation_frames_filtered/map2")));

						try
						{
							Runtime.getRuntime().exec(
									"c:/winnt/system32/shutdown.exe -s -t 60");
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}*/
					}
				});
				thread.setDaemon(true);
				thread.start();
			}

			private void joinThread(Thread thread)
			{
				while (thread.isAlive())
				{
					try
					{
						thread.join();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		
		menu = new JMenu("Debug");
		menu.setMnemonic('D');
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Output key frame values");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		menu.setMnemonic('k');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpKeyFrameValues("keyFrames.txt", animation);
			}
		});
		
		menuItem = new JMenuItem("Output parameter values");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		menu.setMnemonic('k');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpParameterValues("parameterValues.txt", animation.getAllParameters(), animation.getFrameOfFirstKeyFrame(), animation.getFrameOfLastKeyFrame(), new AnimationContextImpl(animation));
			}
		});
	}

	/**
	 * @return A new animation context
	 */
	protected AnimationContext createAnimationContext()
	{
		return new AnimationContextImpl(animation);
	}


	private OrbitView getView()
	{
		return (OrbitView) wwd.getView();
	}

	private void addFrame()
	{
		OrbitView view = getView();
		if (view.getPitch().equals(Angle.ZERO))
		{
			view.setPitch(Angle.fromDegrees(0.1));
			wwd.redrawNow();
		}
		updater.addFrame(slider.getValue(), view);
		
	}

	/**
	 * Apply the animation state at the frame selected on the frame slider
	 */
	private void applyAnimationState()
	{
		int frame = slider.getValue();
		if (animation.getFrameOfFirstKeyFrame() <= frame && frame <= animation.getFrameOfLastKeyFrame())
		{
			applying = true;
			animation.applyFrame(frame);
			applying = false;
		}
	}

	/**
	 * Quit the application, prompting the user to save any changes if required.
	 */
	public void quit()
	{
		if (querySave())
		{
			frame.dispose();
			System.exit(0);
		}
	}

	private void setAnimation(Animation animation)
	{
		this.animation = animation;
		if (useScaledZoomCheck != null)
		{
			useScaledZoomCheck.setSelected(animation.isZoomScalingRequired());
		}
	}

	/**
	 * Create a new animation, prompting the user to save any changes if required.
	 */
	private void newFile()
	{
		if (querySave())
		{
			setAnimation(new WorldWindAnimationImpl(wwd));
			resetChanged();
			setFile(null);
			updateSlider();
		}
	}

	/**
	 * Prompt the user to open an animation file.
	 */
	private void open()
	{
		if (querySave())
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new XmlFilter());
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				File animationFile = chooser.getSelectedFile();
				try
				{
					XmlAnimationReader animationReader = new XmlAnimationReader();
					
					// Check the file version and display appropriate messages
					AnimationFileVersion version = animationReader.getFileVersion(animationFile);
					if (version == null)
					{
						JOptionPane.showMessageDialog(frame, "Could not open '" + animationFile.getAbsolutePath() + "'.\nNot a valid file format.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (version == AnimationFileVersion.VERSION010)
					{
						String message = "File '" + animationFile.getAbsolutePath() + "' is a version 1.0 file. Changes will be saved in version " + XmlAnimationWriter.getCurrentFileVersion().getDisplayName() + ".\n";
						message += "Are you sure you want to continue?";
						int response = JOptionPane.showConfirmDialog(frame, message, "Open", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.NO_OPTION)
						{
							return;
						}
					}
					
					// Load the file
					Animation newAnimation = animationReader.readAnimation(animationFile, wwd);
					
					setAnimation(newAnimation);
					resetChanged();
					setFile(animationFile);
					updateSlider();
					setSlider(0);
					animation.applyFrame(0);
				}
				catch (Exception e)
				{
					ExceptionLogger.logException(e);
					JOptionPane.showMessageDialog(frame, "Could not open '" + animationFile.getAbsolutePath() + "'.\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Save the animation, prompting the user to choose a file if necessary.
	 */
	private void save()
	{
		if (file == null)
		{
			saveAs();
		}
		else
		{
			save(file);
		}
	}

	/**
	 * Launch the 'save as' dialog and prompt the user to choose a file.
	 * <p/>
	 * If the user selects an existing file, prompt to overwrite it.
	 */
	private void saveAs()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new XmlFilter());
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File newFile = chooser.getSelectedFile();
			if (!newFile.getName().toLowerCase().endsWith(".xml"))
			{
				newFile = new File(newFile.getParent(), newFile.getName() + ".xml");
			}
			boolean override = true;
			if (newFile.exists())
			{
				override = JOptionPane.showConfirmDialog(frame, newFile.getAbsolutePath() + " already exists.\nDo you want to replace it?", "Save As", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
			}
			if (override)
			{
				setFile(newFile);
				if (file != null)
				{
					save(file);
				}
			}
		}
	}

	/**
	 * Save the animation to the provided file
	 * 
	 * @param file The file to save the animation to
	 */
	private void save(File file)
	{
		if (file != null)
		{
			try
			{
				AnimationWriter writer = new XmlAnimationWriter();
				writer.writeAnimation(file, animation);
				resetChanged();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(frame, "Saving failed.\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
			}
			setTitleBar();
		}
	}

	/**
	 * Set the current file for the animation
	 * 
	 * @param file The current file for the animation
	 */
	private void setFile(File file)
	{
		this.file = file;
		setTitleBar();
	}

	private void resetChanged()
	{
		//		animation.removeChangeListener(animationChangeListener);
		//		animation.addChangeListener(animationChangeListener);
		//		changed = false;
	}

	/**
	 * Prompt the user to save their changes if any exist.
	 * 
	 * @return <code>false</code> if the user cancelled the operation. <code>true</code> otherwise.
	 */
	private boolean querySave()
	{
		if (!changed)
		{
			return true;
		}
		String file = this.file == null ? "Animation" : "'" + this.file.getName() + "'";
		String message = file + " has been modified. Save changes?";
		
		int response = JOptionPane.showConfirmDialog(frame, message, "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (response == JOptionPane.CANCEL_OPTION)
		{
			return false;
		}
		if (response == JOptionPane.YES_OPTION)
		{
			save();
		}
		return true;
	}

	/**
	 * Set the title bar of the application window. 
	 * <p/>
	 * The application title will include the file name of the current animation, and an 'isChanged' indicator. 
	 */
	private void setTitleBar()
	{
		String file;
		String title = "World Wind Animator";
		if (this.file != null)
		{
			file = this.file.getName();
		}
		else
		{
			file = "New animation";
		}
		if (changed)
		{
			file += " *";
		}
		title = file + " - " + title;
		frame.setTitle(title);
	}

	private void updateSlider()
	{
		slider.clearKeys();
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			slider.addKey(keyFrame.getFrame());
		}
		slider.setMin(0);
		slider.setMax(animation.getFrameCount());
	}

	private void setSlider(int frame)
	{
		settingSlider = true;
		slider.setValue(frame);
		settingSlider = false;
	}

	private void preview(final int frameSkip)
	{
		if (animation != null && animation.hasKeyFrames())
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					stop = false;

					int firstFrame = Math.max(slider.getValue(), animation.getFrameOfFirstKeyFrame());
					int lastFrame = animation.getFrameOfLastKeyFrame();

					for (int frame = firstFrame; frame <= lastFrame; frame += frameSkip)
					{
						setSlider(frame);
						applyAnimationState();
						wwd.redrawNow();

						if (stop)
						{
							break;
						}
					}
				}
			});
			thread.start();
		}
	}

	private Thread animate(final double detailHint)
	{
		int firstFrame = Math.max(slider.getValue(), animation.getFrameOfFirstKeyFrame());
		int lastFrame = animation.getFrameOfLastKeyFrame();
		return animate(detailHint, firstFrame, lastFrame, new File(DATA_DRIVE + ":/frames"), true);
	}

	private Thread animate(final double detailHint, final int firstFrame, final int lastFrame, final File outputDir, final boolean alpha)
	{
		if (animation != null && animation.hasKeyFrames())
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					stop = false;

					boolean immediate = ImmediateMode.isImmediate();
					ImmediateMode.setImmediate(true);
					AnimatorSceneController asc = (AnimatorSceneController) wwd.getSceneController();

					setAnimationSize(animation.getRenderParameters().getImageDimension());

					crosshair.setEnabled(false);
					double detailHintBackup = dem.getDetailHint();
					dem.setDetailHint(detailHint);
					frame.setAlwaysOnTop(true);

					View view = wwd.getView();
					OrbitView orbitView = (OrbitView) view;
					boolean detectCollisions = orbitView.isDetectCollisions();
					orbitView.setDetectCollisions(false);

					int filenameLength = String.valueOf(lastFrame).length();

					for (int frame = firstFrame; frame <= lastFrame; frame += 1)
					{
						setSlider(frame);
						applyAnimationState();

						asc.takeScreenshot(new File(outputDir, "frame" + FileUtil.paddedInt(frame, filenameLength)+ ".tga"), alpha);
						wwd.redraw();
						asc.waitForScreenshot();

						if (stop)
						{
							break;
						}
					}

					dem.setDetailHint(detailHintBackup);
					crosshair.setEnabled(true);
					orbitView.setDetectCollisions(detectCollisions);
					frame.setAlwaysOnTop(false);

					ImmediateMode.setImmediate(immediate);
				}
			});
			thread.start();
			return thread;
		}
		return null;
	}

	private class Updater
	{
		private Map<Integer, ViewParameters> toApply = new HashMap<Integer, ViewParameters>();
		private Object waiter = new Object();

		public Updater()
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					while (true)
					{
						Integer key = getNextKey();
						if (key != null)
						{
							animation.recordKeyFrame(key);
							updateSlider();
							removeValue(key);
						}
						else
						{
							synchronized (waiter)
							{
								try
								{
									waiter.wait();
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}

		private synchronized Integer getNextKey()
		{
			Set<Integer> keyset = toApply.keySet();
			if (keyset.isEmpty())
			{
				return null;
			}
			return keyset.iterator().next();
		}

		private synchronized ViewParameters removeValue(Integer key)
		{
			return toApply.remove(key);
		}

		public synchronized void addFrame(int frame, OrbitView view)
		{
			toApply.put(frame, ViewParameters.fromView(view));
			synchronized (waiter)
			{
				waiter.notify();
			}
		}

	}

	private static class ViewParameters
	{
		public Position eye;
		public Position center;

		public static ViewParameters fromView(OrbitView view)
		{
			ViewParameters vp = new ViewParameters();
			vp.eye = view.getEyePosition();
			vp.center = view.getCenterPosition();
			return vp;
		}
	}

	private static class XmlFilter extends javax.swing.filechooser.FileFilter
	{
		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				return true;
			}
			return f.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription()
		{
			return "XML files (*.xml)";
		}
	}
}
