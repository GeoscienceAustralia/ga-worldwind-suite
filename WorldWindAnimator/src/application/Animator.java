package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import layers.depth.DepthLayer;
import layers.file.FileLayer;
import nasa.worldwind.terrain.BasicElevationModel;
import nasa.worldwind.terrain.ConfigurableTessellator;
import nasa.worldwind.view.BasicRollOrbitView;
import util.ChangeFrameListener;
import util.FrameSlider;
import util.TGAScreenshot;
import animation.SimpleAnimation;

public class Animator
{
	static
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes",
					"false");
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
	private boolean takingScreenshot = false;
	private FrameSlider slider;
	private SimpleAnimation animation = null;
	private File file = null;
	private boolean autokey = false;
	private boolean applying = false;
	private Updater updater;
	private boolean changed = false;
	private boolean stop = false;
	private boolean settingSlider = false;
	private ChangeListener animationChangeListener;
	private Layer crosshair;

	public Animator()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicRollOrbitView.class
				.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME,
				ConfigurableTessellator.class.getName());

		animationChangeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				changed = true;
				setTitleBar();
			}
		};

		animation = new SimpleAnimation();
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
		wwd = new WorldWindowGLCanvas();
		Model model = new BasicModel();
		wwd.setModel(model);
		setAnimationSize(1024, 576);
		frame.add(wwd, BorderLayout.CENTER);

		CompoundElevationModel cem = new CompoundElevationModel();
		model.getGlobe().setElevationModel(cem);

		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		ConfigurableTessellator tesselator = (ConfigurableTessellator) model
				.getGlobe().getTessellator();
		//tesselator.setMakeTileSkirts(false);
		//model.setShowWireframeInterior(true);
		wwd.getSceneController().setVerticalExaggeration(1.5);
		tesselator.setElevationOffset(200);

		LayerList layers = model.getLayers();
		layers.add(new DepthLayer());

		//layers.add(new StarsLayer());
		//layers.add(new SkyGradientLayer());
		//layers.add(new FogLayer());
		//layers.add(new BMNGOneImage());
		//layers.add(new BMNGWMSLayer());
		//layers.add(new LandsatI3WMSLayer());
		//layers.add(new EarthNASAPlaceNameLayer());
		//layers.add(new CompassLayer());
		//layers.add(new WorldMapLayer());
		//layers.add(new ScalebarLayer());
		//layers.add(new MGRSGraticuleLayer());

		BasicElevationModel bem = FileLayer.createElevationModel("WestMac DEM",
				"GA/WestMac DEM", new File("F:/West Macs Imagery/wwtiles/dem150"),
				11, 150, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(
						-25.0001389, -23.0001389, 131.9998611, 133.9998611),
				308d, 1515d);
		cem.addElevationModel(bem);
		//bem.setDetailHint(1.0);

		Layer page1 = FileLayer.createLayer("WestMac Map Page 1",
				"GA/WestMac Map Page 1", ".dds", new File(
						"F:/West Macs Imagery/Rectified Map/5 Tiles/page1"),
				"png", 13, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(
						-24.0536281, -23.4102781, 132.0746805, 133.9779805));
		layers.add(page1);

		Layer page2 = FileLayer.createLayer("WestMac Map Page 2",
				"GA/WestMac Map Page 2", ".dds", new File(
						"F:/West Macs Imagery/Rectified Map/5 Tiles/page2"),
				"png", 13, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(
						-24.0544889, -23.4081639, 132.0708833, 133.9771083));
		layers.add(page2);

		Layer alos = FileLayer.createLayer("WestMac ALOS", "GA/WestMac ALOS",
				".dds", new File("F:/West Macs Imagery/wwtiles/alosnp_4326"),
				"jpg", new File("F:/West Macs Imagery/wwtiles/mapmask"), "png",
				13, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(-24.0,
						-23.433333, 132.25, 133.95));
		layers.add(alos);

		Layer roads = FileLayer.createLayer("WestMac Roads",
				"GA/WestMac Roads", ".dds", new File(
						"C:/WINNT/Profiles/u97852/Desktop/Roads/Mapnik/tiled"),
				"png", 12, LatLon.fromDegrees(36d, 36d), Sector.fromDegrees(
						-24.0, -23.433333, 132.25, 133.95));
		layers.add(roads);

		page1.setEnabled(false);
		//page2.setEnabled(false);
		alos.setEnabled(false);
		roads.setEnabled(false);

		/*Layer roads = new ShapefileLayer(new File(
				"C:/WINNT/Profiles/u97852/Desktop/Roads/Shapefile/Roads.shp"));
		layers.add(roads);*/

		/*int overlay = 220;
		Layer overlayLayer = new ImageOverlay(
				"C:/WINNT/Profiles/u97852/Desktop/TEMP FOLDING AS PNG/FoldingMap0"
						+ overlay + ".png");
		layers.add(overlayLayer);
		overlayLayer.setOpacity(0.4);*/

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
					if (animation.size() > 0)
					{
						applyView();
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
				animation.setFrame(index, newFrame);
				updateSlider();
				applyView();
				wwd.redraw();
			}
		});

		/*JScrollPane scrollPane = new JScrollPane(slider,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(null);
		bottom.add(scrollPane, BorderLayout.CENTER);
		
		Dimension sps = slider.getMinimumSize();
		sps.width *= 2;
		slider.setMinimumSize(sps);*/

		getView().addPropertyChangeListener(AVKey.VIEW,
				new PropertyChangeListener()
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

	private void setAnimationSize(int width, int height)
	{
		Dimension size = new Dimension(width, height);
		wwd.setPreferredSize(size);
		wwd.setMinimumSize(size);
		wwd.setMaximumSize(size);
		frame.pack();
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke('N',
				ActionEvent.CTRL_MASK));
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke('O',
				ActionEvent.CTRL_MASK));
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke('S',
				ActionEvent.CTRL_MASK));
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
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
				int index = animation.indexOf(slider.getValue());
				if (index >= 0)
				{
					animation.removeFrame(index);
				}
				updateSlider();
			}
		});

		menu.addSeparator();

		final JCheckBoxMenuItem autoKeyItem = new JCheckBoxMenuItem("Auto key",
				autokey);
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
				Object value = JOptionPane.showInputDialog(frame,
						"Number of frames:", "Set frame count",
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
				KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 10);
			}
		});

		menuItem = new JMenuItem("Next 10");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				KeyEvent.SHIFT_DOWN_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 10);
			}
		});

		menuItem = new JMenuItem("First");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('F');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFirstFrame());
			}
		});

		menuItem = new JMenuItem("Last");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('L');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getLastFrame());
			}
		});


		menu = new JMenu("Animation");
		menu.setMnemonic('A');
		menuBar.add(menu);

		menuItem = new JMenuItem("Scale animation...");
		menuItem.setMnemonic('S');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame,
						"Scale factor:", "Scale animation",
						JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
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
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame,
						"Scale factor:", "Scale height",
						JOptionPane.QUESTION_MESSAGE, null, null, 1.0);
				try
				{
					scale = Double.parseDouble((String) value);
				}
				catch (Exception ex)
				{
				}
				if (scale != 1.0 && scale > 0)
				{
					animation.scaleHeight(scale);
				}
			}
		});

		menuItem = new JMenuItem("Smooth eye speed");
		menuItem.setMnemonic('m');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane
						.showConfirmDialog(
								frame,
								"This will redistribute keyframes to attempt to smooth the eye speed.\nDo you wish to continue?",
								"Smooth eye speed", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					animation.smoothEyeSpeed();
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
				animate(false, 1);
			}
		});

		menuItem = new JMenuItem("Preview x2");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(false, 2);
			}
		});

		menuItem = new JMenuItem("Preview x10");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(false, 10);
			}
		});

		menuItem = new JMenuItem("Render...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('R',
				ActionEvent.CTRL_MASK));
		menuItem.setMnemonic('R');
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(true, 1);
			}
		});
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
		//animation.addFrame(slider.getValue(), view);
		//updateAnimation();
	}

	private void applyView()
	{
		int frame = slider.getValue();
		if (animation.getFirstFrame() <= frame
				&& frame <= animation.getLastFrame())
		{
			applying = true;
			animation.applyFrame(getView(), frame);
			applying = false;
		}
	}

	public void quit()
	{
		if (querySave())
		{
			frame.dispose();
			System.exit(0);
		}
	}

	private void newFile()
	{
		if (querySave())
		{
			animation = new SimpleAnimation();
			resetChanged();
			setFile(null);
			updateSlider();
		}
	}

	private void open()
	{
		if (querySave())
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new XmlFilter());
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				File newFile = chooser.getSelectedFile();
				SimpleAnimation newAnimation = new SimpleAnimation();
				try
				{
					newAnimation.load(newFile);
					animation = newAnimation;
					resetChanged();
					setFile(newFile);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "Could not open '"
							+ newFile.getAbsolutePath() + "'.\n" + e, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			updateSlider();
		}
	}

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

	private void saveAs()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new XmlFilter());
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File newFile = chooser.getSelectedFile();
			if (!newFile.getName().toLowerCase().endsWith(".xml"))
			{
				newFile = new File(newFile.getParent(), newFile.getName()
						+ ".xml");
			}
			boolean override = true;
			if (newFile.exists())
			{
				override = JOptionPane.showConfirmDialog(frame, newFile
						.getAbsolutePath()
						+ " already exists.\nDo you want to replace it?",
						"Save As", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
			}
			if (override)
			{
				setFile(newFile);
				if (file != null)
					save(file);
			}
		}
	}

	private void save(File file)
	{
		if (file != null)
		{
			try
			{
				animation.save(file);
				resetChanged();
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(frame, "Saving failed.\n" + e,
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			setTitleBar();
		}
	}

	private void setFile(File file)
	{
		this.file = file;
		setTitleBar();
	}

	private void resetChanged()
	{
		animation.removeChangeListener(animationChangeListener);
		animation.addChangeListener(animationChangeListener);
		changed = false;
	}

	private boolean querySave()
	{
		if (!changed)
			return true;
		String file = this.file == null ? "Animation" : "'"
				+ this.file.getName() + "'";
		String message = file + " has been modified. Save changes?";
		int value = JOptionPane.showConfirmDialog(frame, message, "Save",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (value == JOptionPane.CANCEL_OPTION)
			return false;
		if (value == JOptionPane.YES_OPTION)
			save();
		return true;
	}

	private void setTitleBar()
	{
		String file;
		String title = "World Wind Animator";
		if (this.file != null)
			file = this.file.getName();
		else
			file = "New animation";
		if (changed)
			file += " *";
		title = file + " - " + title;
		frame.setTitle(title);
	}

	private void updateSlider()
	{
		slider.clearKeys();
		for (int i = 0; i < animation.size(); i++)
		{
			slider.addKey(animation.getFrame(i));
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

	private void animate(final boolean savingFrames, final int frameSkip)
	{
		if (animation != null && animation.size() > 0)
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					crosshair.setEnabled(!savingFrames);
					stop = false;
					frame.setAlwaysOnTop(savingFrames);

					if (savingFrames)
					{
						Toolkit tk = Toolkit.getDefaultToolkit();
						BufferedImage image = new BufferedImage(1, 1,
								BufferedImage.TYPE_INT_ARGB);
						Cursor blankCursor = tk.createCustomCursor(image,
								new Point(0, 0), "BlackCursor");
						wwd.setCursor(blankCursor);

						setAnimationSize(animation.getWidth(), animation
								.getHeight());
						frame.setResizable(false);
					}

					View view = getView();
					boolean detectCollisions = view.isDetectCollisions();
					view.setDetectCollisions(false);

					//TEMP
					/*GraphicsEnvironment ge = GraphicsEnvironment
							.getLocalGraphicsEnvironment();
					DisplayMode dm = ge.getDefaultScreenDevice()
							.getDisplayMode();
					int screenWidth = dm.getWidth();
					int screenHeight = dm.getHeight();
					Random random = new Random();
					Robot robot = null;
					try
					{
						robot = new Robot();
					}
					catch (AWTException e)
					{
						e.printStackTrace();
					}*/
					//TEMP
					int firstFrame = Math.max(slider.getValue(), animation
							.getFirstFrame());
					int lastFrame = animation.getLastFrame();

					for (int frame = firstFrame; frame <= lastFrame; frame += frameSkip)
					{
						setSlider(frame);
						applyView();
						wwd.redrawNow();

						if (stop)
							break;

						if (savingFrames)
						{
							takeScreenshot("F:/West Macs Imagery/animation_frames/frame"
									+ frame + ".tga");
						}

						//TEMP
						/*robot.mouseMove(screenWidth - random.nextInt(10) - 1,
								screenHeight - random.nextInt(10) - 1);*/
						//TEMP
					}

					view.setDetectCollisions(detectCollisions);

					frame.setResizable(true);
					frame.setAlwaysOnTop(false);
					wwd.setCursor(null);
					crosshair.setEnabled(true);
				}
			});
			thread.start();
		}
	}

	private void takeScreenshot(String filename)
	{
		if (!EventQueue.isDispatchThread())
		{
			takingScreenshot = true;
		}

		wwd.addRenderingListener(new Screenshotter(filename));
		wwd.redraw();

		if (!EventQueue.isDispatchThread())
		{
			while (takingScreenshot)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private class Screenshotter implements RenderingListener
	{
		private final String filename;

		public Screenshotter(String filename)
		{
			this.filename = filename;
		}

		public void stageChanged(RenderingEvent event)
		{
			if (WorldWind.getTaskService().hasActiveTasks()
					|| WorldWind.getRetrievalService().hasActiveTasks())
			{
				sleep();
				wwd.redraw();
			}
			else if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
			{
				wwd.removeRenderingListener(this);

				File out = new File(filename);
				if (!out.getParentFile().exists())
				{
					out.getParentFile().mkdirs();
				}
				try
				{
					if (out.getName().toLowerCase().endsWith(".tga"))
					{
						/*com.sun.opengl.util.Screenshot.writeToTargaFile(out,
								wwd.getWidth(), wwd.getHeight(), true);*/
						TGAScreenshot.writeToTargaFile(out, wwd.getWidth(), wwd
								.getHeight(), true);
					}
					else
					{
						com.sun.opengl.util.Screenshot.writeToFile(out, wwd
								.getWidth(), wwd.getHeight(), true);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				takingScreenshot = false;
			}
		}

		private void sleep()
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	};

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
							ViewParameters vp = removeValue(key);
							animation.addFrame(key, vp.eye, vp.center);
							updateSlider();
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
				return null;
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
				return true;
			return f.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription()
		{
			return "XML files (*.xml)";
		}
	}
}
