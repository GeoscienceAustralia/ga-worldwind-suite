package au.gov.ga.worldwind.animator.application;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
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
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.media.opengl.GLCapabilities;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import nasa.worldwind.awt.WorldWindowGLCanvas;
import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.CurrentlySelectedObject;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationWriter;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationReader;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.settings.RecentlyUsedFilesMenuList;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.layers.camerapath.CameraPathLayer;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateRetrievalService;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateTaskService;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanel;
import au.gov.ga.worldwind.animator.panels.SideBar;
import au.gov.ga.worldwind.animator.panels.animationbrowser.AnimationBrowserPanel;
import au.gov.ga.worldwind.animator.panels.layerpalette.LayerPalettePanel;
import au.gov.ga.worldwind.animator.panels.objectproperties.ObjectPropertiesPanel;
import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;
import au.gov.ga.worldwind.animator.terrain.VerticalExaggerationTessellator;
import au.gov.ga.worldwind.animator.ui.frameslider.ChangeFrameListener;
import au.gov.ga.worldwind.animator.ui.frameslider.FrameSlider;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.animator.view.orbit.BasicOrbitView;
import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.SelectableAction;
import au.gov.ga.worldwind.common.ui.SplashScreen;
import au.gov.ga.worldwind.common.util.GASandpit;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.MetersStatusBar;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * The primary application class for the Animator application.
 * <p/>
 * Sets up the GUI and initialises animations.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Animator
{
	private static final GLCapabilities caps = new GLCapabilities();
	static
	{
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(24);
		caps.setDoubleBuffered(true);
		caps.setNumSamples(4);
	
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
			ExceptionLogger.logException(e);
		}

		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");

	}
	
	public static void main(String[] args)
	{
		new Animator();
	}

	/** The primary application window */
	private JFrame frame;

	/** The split pane that contains the actual components */
	private JSplitPane splitPane;
	
	/** The main panel that contains the 3D view */
	private JPanel mainPanel;
	
	/** The side bar panel */
	private SideBar sideBar;
	private AnimationBrowserPanel animationBrowserPanel;
	private ObjectPropertiesPanel objectPropertiesPanel;
	private LayerPalettePanel layerPalettePanel;
	
	/** The bottom panel. Holds the status bar. */
	private JPanel bottomPanel;
	
	/** The primary world wind canvas */
	private WorldWindowGLCanvas wwd;
	
	/** The model backing the world wind display */
	private Model model;
	
	/** The key frame slider component */
	private FrameSlider slider;
	
	/** The current animation being viewed */
	private Animation animation = null;
	
	/** The file the current animation is written to */
	private File file = null;
	
	/** Used to update the animation outside of the EDT */
	private Updater updater;
	
	// Status flags
	private boolean autokey = false;
	private boolean applying = false;
	private boolean changed = false;
	private boolean stop = false;
	private boolean settingSlider = false;
	
	/** A listener that updates the 'changed' status when animation change is detected */
	private AnimationEventListener animationChangeListener;

	/** A listener that listens for layer addition/removals and updates the world wind model */
	private AnimationEventListener layerUpdateListener;
	
	/** A listener that updates the frame slider when frames have been added or removed programmatically */
	private AnimationEventListener framesChangedListener;
	
	/** A listener that updates the highlighted frames when the currently selected object changes */
	private CurrentlySelectedObject.ChangeListener highlightedFramesListener;
	
	// The layers used in the application
	private DetailedElevationModel dem;
	private Layer crosshair;
	private CameraPathLayer cameraPathLayer;
	
	/** The file chooser used for open and save. Instance variable so it will remember last used folders. */
	private JFileChooser fileChooser = new JFileChooser();

	/** The menu list of recently used files */
	private RecentlyUsedFilesMenuList mruFileMenu;
	
	// Actions
	private BasicAction newAnimationAction;
	private BasicAction openAnimationAction;
	private BasicAction saveAnimationAction;
	private BasicAction saveAnimationAsAction;
	private BasicAction exitAction;
	
	private BasicAction addKeyAction;
	private BasicAction deleteKeyAction;
	private SelectableAction autoKeyAction;
	private BasicAction setFrameCountAction;
	private BasicAction previousFrameAction;
	private BasicAction nextFrameAction;
	private BasicAction previous10FramesAction;
	private BasicAction next10FramesAction;
	private BasicAction firstFrameAction;
	private BasicAction lastFrameAction;
	
	private SelectableAction useScaledZoomAction;
	private BasicAction scaleAnimationAction;
	private BasicAction smoothEyeSpeedAction;
	private BasicAction previewAction;
	private BasicAction previewX2Action;
	private BasicAction previewX10Action;
	private BasicAction renderHiResAction;
	private BasicAction renderLowResAction;
	private BasicAction resizeToRenderDimensionsAction;
	
	private BasicAction debugKeyFramesAction;
	private BasicAction debugParameterValuesAction;

	/**
	 * Constructor.
	 * <p/>
	 * Performs initialisation of GUI and animation.
	 */
	public Animator()
	{
		Logging.logger().setLevel(Level.FINER);
		initialiseMessageSource();
		initialiseConfiguration();
		
		initialiseApplicationWindow();
		initialiseWorldWindow();
		
		showSplashScreen();
		
		initialiseAnimation();
		
		initialiseElevationModels();
		initialiseUtilityLayers();
		updateLayersInModel();
		
		initialiseFrameSlider();
		initialiseSideBar();
		initialiseStatusBar();
		
		initialiseActions();
		initialiseMenuBar();
		
		setTitleBar();
		
		initialiseAnimationListeners();
		
		updateSlider();
		resetChanged();
		
		showApplicationWindow();
	}

	/**
	 * Initialise the animation listeners
	 */
	private void initialiseAnimationListeners()
	{
		initialiseAutoKeyListener();
		initialiseChangeListener();
		initialiseLayerUpdateListener();
		initialiseFramesChangedListener();
		initialiseHighlightedFramesListener();
	}

	/**
	 * Re-attach the animation listeners to the current animation. Used when the animation changes (open, new file etc.)
	 */
	private void updateAnimationListeners()
	{
		updateAnimationListener(layerUpdateListener);
		updateAnimationListener(framesChangedListener);
		// TODO: Add more listeners here as they are added
		
	}
	private void updateAnimationListener(AnimationEventListener listener)
	{
		animation.removeChangeListener(listener);
		animation.addChangeListener(listener);
	}

	private void initialiseHighlightedFramesListener()
	{
		highlightedFramesListener = new CurrentlySelectedObject.ChangeListener()
		{
			@Override
			public void selectedObjectChanged(AnimationObject currentlySelectedObject, AnimationObject previouslySelectedObject)
			{
				if (currentlySelectedObject == null)
				{
					slider.clearHighlightedKeys();
					return;
				}
				
				Collection<Integer> keysToHighlight = getKeysToHighlight(currentlySelectedObject);
				slider.highlightKeys(keysToHighlight);
			}

			private Collection<Integer> getKeysToHighlight(AnimationObject object)
			{
				Set<Integer> keysToHighlight = new HashSet<Integer>();
				if (object instanceof Parameter)
				{
					keysToHighlight.addAll(getKeysFramesFromParameter((Parameter)object));
				}
				else if (object instanceof Animatable)
				{
					for (Parameter parameter : ((Animatable)object).getParameters())
					{
						keysToHighlight.addAll(getKeysFramesFromParameter(parameter));
					}
				}
				return keysToHighlight;
			}

			private Collection<Integer> getKeysFramesFromParameter(Parameter parameter)
			{
				Set<Integer> result = new HashSet<Integer>();
				for (KeyFrame keyFrame : animation.getKeyFrames(parameter))
				{
					result.add(keyFrame.getFrame());
				}
				return result;
			}
		};
		CurrentlySelectedObject.addChangeListener(highlightedFramesListener);
	}
	
	/**
	 * Initialise the layer update listener that listens for changes to the layers present in the animation
	 */
	private void initialiseLayerUpdateListener()
	{
		layerUpdateListener = new AnimationEventListener()
		{
			@Override
			public void receiveAnimationEvent(AnimationEvent event)
			{
				if (isLayerChangeEvent(event))
				{
					updateLayersInModel();
				}
				
			}

			private boolean isLayerChangeEvent(AnimationEvent event)
			{
				if (event == null)
				{
					return false;
				}
				AnimationEvent rootCause = event.getRootCause();
				return ((rootCause.isOfType(Type.ADD) || rootCause.isOfType(Type.REMOVE) || rootCause.isOfType(Type.CHANGE)) && rootCause.getValue() instanceof AnimatableLayer);
			}
		};
		animation.addChangeListener(layerUpdateListener);
	}

	private void initialiseFramesChangedListener()
	{
		framesChangedListener = new AnimationEventListener()
		{
			@Override
			public void receiveAnimationEvent(AnimationEvent event)
			{
				if (isFrameChangeEvent(event))
				{
					updateSlider();
				}
			}

			private boolean isFrameChangeEvent(AnimationEvent event)
			{
				if (event == null)
				{
					return false;
				}
				AnimationEvent rootCause = event.getRootCause();
				return (rootCause.isOfType(Type.ADD) || rootCause.isOfType(Type.REMOVE)) && rootCause.getValue() instanceof KeyFrame;
			}
		};
		animation.addChangeListener(framesChangedListener);
	}
	
	/**
	 * Initialise the change listener that listens for changes to the animation state
	 */
	private void initialiseChangeListener()
	{
		animationChangeListener = new AnimationEventListener()
		{
			@Override
			public void receiveAnimationEvent(AnimationEvent event)
			{
				changed = true;
				setTitleBar();
				
			}
		};
	}
	
	/**
	 * Attach a property change listener to the World Wind view to automatically generate key frames
	 * when a change is detected.
	 */
	private void initialiseAutoKeyListener()
	{
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
	}
	
	/**
	 * Initialise the side bar, which contains a group of collapsible panels
	 */
	private void initialiseSideBar()
	{
		animationBrowserPanel = new AnimationBrowserPanel(animation);
		objectPropertiesPanel = new ObjectPropertiesPanel(animation);
		layerPalettePanel = new LayerPalettePanel(animation);
		
		List<CollapsiblePanel> collapsiblePanels = new ArrayList<CollapsiblePanel>(3);
		collapsiblePanels.add(animationBrowserPanel);
		collapsiblePanels.add(objectPropertiesPanel);
		collapsiblePanels.add(layerPalettePanel);
		
		sideBar = new SideBar(splitPane, collapsiblePanels);
		splitPane.setLeftComponent(sideBar);
		
		slider.addChangeFrameListener(objectPropertiesPanel);
		slider.addChangeListener(objectPropertiesPanel);
		getView().addPropertyChangeListener(AVKey.VIEW, objectPropertiesPanel);
	}

	/**
	 * Initialise the animation
	 */
	private void initialiseAnimation()
	{
		animation = new WorldWindAnimationImpl(wwd);
		addDefaultLayersToAnimation(animation);
		updater = new Updater();
	}

	/**
	 * Show a splash screen on load
	 */
	private void showSplashScreen()
	{
		SplashScreen splashScreen = new SplashScreen(frame);
		splashScreen.addRenderingListener(wwd);
	}

	/**
	 * Initialise the utility layers used inside the animator application
	 */
	private void initialiseUtilityLayers()
	{
		if (cameraPathLayer == null)
		{
			cameraPathLayer = new CameraPathLayer(animation);
		}
		
		if (crosshair == null)
		{
			crosshair = new CrosshairLayer();
		}
	}
	
	/**
	 * Updates the layers in the current world wind model.
	 */
	private void updateLayersInModel()
	{
		initialiseUtilityLayers();
		
		model.getLayers().clear();
		
		addAnimationLayersToModel();
		addUtilityLayersToModel();
	}
	
	/**
	 * Add the layers associated with the current animation to the WorldWind model.
	 * <p/>
	 * Will append the animation layers to the current layers list. 
	 */
	private void addAnimationLayersToModel()
	{
		LayerList layers = model.getLayers();
		for (Layer layer : animation.getLayers())
		{
			layers.add(layer);
		}
	}

	/**
	 * Add the utility layers used in the animator application to the current world wind model.
	 * <p/>
	 * Will append the animation layers to the current layers list. 
	 */
	private void addUtilityLayersToModel()
	{
		LayerList layers = model.getLayers();
		layers.add(cameraPathLayer);
		layers.add(crosshair);
	}
	
	/**
	 * Initialise the elevation models used in the application
	 */
	private void initialiseElevationModels()
	{
		CompoundElevationModel cem = new CompoundElevationModel();
		dem = new DetailedElevationModel(cem);
		model.getGlobe().setElevationModel(dem);

		ElevationModel earthem = (ElevationModel) new ElevationModelFactory().createFromConfigSource("config/Earth/EarthElevationModelAsBil16.xml", null);
		cem.addElevationModel(earthem);
	}

	/**
	 * Packs the main frame and makes it visible.
	 * <p/>
	 * Executes on the EDT.
	 */
	private void showApplicationWindow()
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable(){

				@Override
				public void run()
				{
					resizeWindowToAnimationSize(animation.getRenderParameters().getImageDimension());
					frame.pack();
					frame.setVisible(true);
				}
				
			});
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
	}

	/**
	 * Create a status bar and put it in bottom panel
	 */
	private void initialiseStatusBar()
	{
		MetersStatusBar statusBar = new MetersStatusBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar.setEventSource(wwd);
		
		bottomPanel.add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * Initialise the World Wind {@link WorldWindow} used by the application
	 */
	private void initialiseWorldWindow()
	{
		wwd = new WorldWindowGLCanvas(caps);
		model = new BasicModel();
		wwd.setModel(model);
		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(Angle.ZERO, Angle.POS180);
		wwd.setMinimumSize(new Dimension(1, 1));
		
		// The buffer holds the world wind canvas and gives us a handle to resize it against
		final JPanel wwdBufferPanel = new JPanel();
		wwdBufferPanel.setLayout(null);
		wwdBufferPanel.setPreferredSize(RenderParameters.DEFAULT_DIMENSIONS);
		wwdBufferPanel.setBackground(LAFConstants.getHighlightColor());
		wwdBufferPanel.add(wwd);
		// On resize, adjust the world wind canvas to maintain the correct aspect ratio
		wwdBufferPanel.addComponentListener(new ComponentAdapter(){

			@Override
			public void componentResized(ComponentEvent e)
			{
				Dimension bufferSize = wwdBufferPanel.getSize();
				double newWidth = bufferSize.width;
			    double newHeight = bufferSize.height;
			    
			    double targetRatio = animation.getRenderParameters().getImageAspectRatio();
			    double currentRatio = (double)bufferSize.width / bufferSize.height;
			    
			    if (currentRatio > targetRatio) 
			    {
			        newWidth = newHeight * targetRatio;
			    } 
			    else if (currentRatio < targetRatio) 
			    {
			        newHeight = newWidth / targetRatio;
			    }
			    
			    Dimension canvasSize = new Dimension((int)newWidth, (int)newHeight);
				wwd.setSize(canvasSize);
			    wwd.setPreferredSize(canvasSize);
			    wwd.setMinimumSize(canvasSize);
			    wwd.setMaximumSize(canvasSize);
			    
			    int newX = (int)(bufferSize.getWidth() - canvasSize.getWidth()) / 2;
			    int newY = (int)(bufferSize.getHeight() - canvasSize.getHeight()) / 2;
			    wwd.setLocation(newX, newY);
			    
			    wwdBufferPanel.revalidate();
			    wwdBufferPanel.repaint(100);
			}
		});
		
		mainPanel.add(wwdBufferPanel, BorderLayout.CENTER);
	}

	/**
	 * Initialise the frame slider and add it to the content panel
	 */
	private void initialiseFrameSlider()
	{
		slider = new FrameSlider(0, 0, animation.getFrameCount());
		mainPanel.add(slider, BorderLayout.SOUTH);
		
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
	}

	/**
	 * Initialise the primary application window 
	 */
	private void initialiseApplicationWindow()
	{
		// Setup the application frame
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
		
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		
		// Setup the split pane
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(Settings.get().getSplitLocation());
		panel.add(splitPane, BorderLayout.CENTER);
		
		// Add the main panel for the 3D view and frame slider
		mainPanel = new JPanel(new BorderLayout());
		splitPane.setRightComponent(mainPanel);
		
		// Add the bottom panel for the status bar
		bottomPanel = new JPanel(new BorderLayout());
		frame.add(bottomPanel, BorderLayout.SOUTH);
		
		//ensure menu bar and popups appear over the heavyweight WW canvas
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		
		// Set the last used location
		fileChooser.setCurrentDirectory(Settings.get().getLastUsedLocation());
	}

	/**
	 * Initialise the configuration settings for the application
	 */
	private void initialiseConfiguration()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicOrbitView.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnimatorSceneController.class.getName());

		Configuration.setValue(AVKey.TASK_SERVICE_CLASS_NAME, ImmediateTaskService.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ImmediateRetrievalService.class.getName());

		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, VerticalExaggerationTessellator.class.getName());

		Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, 16777216L * 8); // 128 mb
		
		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		
		Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactory.class.getName());
		
		GASandpit.setSandpitMode(true);
	}

	/**
	 * Initialise the message source used in the application
	 */
	private void initialiseMessageSource()
	{
		// Initialise the message source
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.animator.data.messages.animatorMessages");
	}

	/**
	 * Resize the animation window such that the render window is at the specified animation size.
	 */
	private void resizeWindowToAnimationSize(Dimension animationSize)
	{
			// Set the world window to the correct size
			setWwdSize(animationSize);

			if (!frame.isVisible())
			{
				frame.pack();
			}
			
			// Set the frame to the correct size
			int deltaWidth = calculateTotalWidthOfNonWWDElements();
			int deltaHeight = calculateTotalHeightOfNonWWDElements();

			Dimension frameSize = new Dimension(animationSize.width + deltaWidth, animationSize.height + deltaHeight);
			setFrameSize(frameSize);
			
			frame.pack();

			// Check the resize was successful
			Dimension wwdSize = wwd.getSize();
			if (wwdSize.width != animationSize.width || wwdSize.height != animationSize.height)
			{
				JOptionPane.showMessageDialog(frame, 
											  getMessage(getSetDimensionFailedMessageKey(), animationSize.width, animationSize.height, wwdSize.width, wwdSize.height),
											  getMessage(getSetDimensionFailedCaptionKey()),
											  JOptionPane.ERROR_MESSAGE);
			}
	}

	private void setFrameSize(Dimension frameSize)
	{
		frame.setPreferredSize(frameSize);
		frame.setMinimumSize(frameSize);
		frame.setMaximumSize(frameSize);
		frame.setSize(frameSize);
	}

	private void setWwdSize(Dimension animationSize)
	{
		wwd.setMinimumSize(animationSize);
		wwd.setMaximumSize(animationSize);
		wwd.setPreferredSize(animationSize);
		wwd.setSize(animationSize);
	}

	private int calculateTotalHeightOfNonWWDElements()
	{
		return bottomPanel.getSize().height + slider.getSize().height + frame.getJMenuBar().getSize().height + frame.getInsets().top + frame.getInsets().bottom;
	}

	private int calculateTotalWidthOfNonWWDElements()
	{
		return sideBar.getSize().width + frame.getInsets().left + frame.getInsets().right;
	}

	/**
	 * Initialise the actions used in the application
	 */
	private void initialiseActions()
	{
		// New
		newAnimationAction = new BasicAction(getMessage(getNewMenuLabelKey()), null);
		newAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		newAnimationAction.setIcon(Icons.newfile.getIcon());
		newAnimationAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newFile();
			}
		});
		
		// Open
		openAnimationAction = new BasicAction(getMessage(getOpenMenuLabelKey()), null);
		openAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		openAnimationAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				open();
			}
		});
		
		// Save
		saveAnimationAction = new BasicAction(getMessage(getSaveMenuLabelKey()), null);
		saveAnimationAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		saveAnimationAction.setIcon(Icons.save.getIcon());
		saveAnimationAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save();
			}
		});
		
		// Save as
		saveAnimationAsAction = new BasicAction(getMessage(getSaveAsMenuLabelKey()), null);
		saveAnimationAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		saveAnimationAsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		saveAnimationAsAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveAs();
			}
		});
		
		// Exit
		exitAction = new BasicAction(getMessage(getExitMenuLabelKey()), null);
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		exitAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});
		
		// Add key
		addKeyAction = new BasicAction(getMessage(getAddKeyMenuLabelKey()), null);
		addKeyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		addKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		addKeyAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFrame();
			}
		});
		
		// Delete key
		deleteKeyAction = new BasicAction(getMessage(getDeleteKeyMenuLabelKey()), null);
		deleteKeyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		deleteKeyAction.addActionListener(new ActionListener()
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
		
		// Auto key
		autoKeyAction = new SelectableAction(getMessage(getAutoKeyMenuLabelKey()), null, false);
		autoKeyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		autoKeyAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				autokey = autoKeyAction.isSelected();
			}
		});
		
		// Set frame count
		setFrameCountAction = new BasicAction(getMessage(getSetFrameCountMenuLabelKey()), null);
		setFrameCountAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		setFrameCountAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int frames = slider.getLength() - 1;
				Object value = JOptionPane.showInputDialog(frame, 
														   getMessage(getSetFrameCountMessageKey()),
														   getMessage(getSetFrameCountCaptionKey()),
														   JOptionPane.QUESTION_MESSAGE,
														   null,
														   null,
														   frames);
				try
				{
					frames = Integer.parseInt((String) value);
				}
				catch (Exception ex)
				{
					ExceptionLogger.logException(ex);
				}
				animation.setFrameCount(frames);
				updateSlider();
			}
		});
		
		// Previous frame
		previousFrameAction = new BasicAction(getMessage(getPreviousFrameMenuLabelKey()), null);
		previousFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
		previousFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		previousFrameAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 1);
			}
		});
		
		// Next frame
		nextFrameAction = new BasicAction(getMessage(getNextFrameMenuLabelKey()), null);
		nextFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
		nextFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		nextFrameAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 1);
			}
		});
		
		// Previous 10 frame
		previous10FramesAction = new BasicAction(getMessage(getPrevious10FrameMenuLabelKey()), null);
		previous10FramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_DOWN_MASK));
		previous10FramesAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() - 10);
			}
		});
		
		// Next 10 frame
		next10FramesAction = new BasicAction(getMessage(getNext10FrameMenuLabelKey()), null);
		next10FramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_DOWN_MASK));
		next10FramesAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(slider.getValue() + 10);
			}
		});
		
		// First frame
		firstFrameAction = new BasicAction(getMessage(getFirstFrameMenuLabelKey()), null);
		firstFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ActionEvent.CTRL_MASK));
		firstFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		firstFrameAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFrameOfFirstKeyFrame());
			}
		});
		
		// Last frame
		lastFrameAction = new BasicAction(getMessage(getLastFrameMenuLabelKey()), null);
		lastFrameAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, ActionEvent.CTRL_MASK));
		lastFrameAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		lastFrameAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(animation.getFrameOfLastKeyFrame());
			}
		});
		
		// Use scaled zoom
		useScaledZoomAction = new SelectableAction(getMessage(getUseZoomScalingMenuLabelKey()), null, animation.isZoomScalingRequired());
		useScaledZoomAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
		useScaledZoomAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animation.setZoomScalingRequired(useScaledZoomAction.isSelected());
			}
		});
		
		// Scale animation
		scaleAnimationAction = new BasicAction(getMessage(getScaleAnimationMenuLabelKey()), null);
		scaleAnimationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		scaleAnimationAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				double scale = -1.0;
				Object value = JOptionPane.showInputDialog(frame, 
														   getMessage(getScaleAnimationMessageKey()),
														   getMessage(getScaleAnimationCaptionKey()),
														   JOptionPane.QUESTION_MESSAGE,
														   null,
														   null,
														   1.0);
				try
				{
					scale = Double.parseDouble((String) value);
				}
				catch (Exception ex)
				{
					ExceptionLogger.logException(ex);
				}
				if (scale != 1.0 && scale > 0)
				{
					animation.scale(scale);
				}
				updateSlider();
			}
		});
		
		// Smooth eye speed
		smoothEyeSpeedAction = new BasicAction(getMessage(getSmoothEyeSpeedMenuLabelKey()), null);
		smoothEyeSpeedAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		smoothEyeSpeedAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showConfirmDialog(frame,
												  getMessage(getQuerySmoothEyeSpeedMessageKey()),
												  getMessage(getQuerySmoothEyeSpeedCaptionKey()), 
												  JOptionPane.YES_NO_OPTION, 
												  JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					animation.getCamera().smoothEyeSpeed(createAnimationContext());
					updateSlider();
				}
			}
		});
		
		// Preview
		previewAction = new BasicAction(getMessage(getPreviewMenuLabelKey()), null);
		previewAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		previewAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		previewAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(1);
			}
		});
		
		// Preview x2
		previewX2Action = new BasicAction(getMessage(getPreviewX2MenuLabelKey()), null);
		previewX2Action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.SHIFT_MASK));
		previewX2Action.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(2);
			}
		});
		
		// Preview x10
		previewX10Action = new BasicAction(getMessage(getPreviewX10MenuLabelKey()), null);
		previewX10Action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.CTRL_MASK));
		previewX10Action.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				preview(10);
			}
		});
		
		// Render hi-res
		renderHiResAction = new BasicAction(getMessage(getRenderHighResMenuLabelKey()), null);
		renderHiResAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		renderHiResAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		renderHiResAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				renderAnimation(1);
			}
		});
		
		// Render low-res
		renderLowResAction = new BasicAction(getMessage(getRenderStandardResMenuLabelKey()), null);
		renderLowResAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				renderAnimation(0);
			}
		});
		
		// Resize to render dimensions
		resizeToRenderDimensionsAction = new BasicAction(getMessage(getResizeToRenderDimensionsLabelKey()), null);
		resizeToRenderDimensionsAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resizeWindowToAnimationSize(animation.getRenderParameters().getImageDimension());
			}
		});
		
		// Debug key frames
		debugKeyFramesAction = new BasicAction(getMessage(getKeyValuesMenuLabelKey()), null);
		debugKeyFramesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		debugKeyFramesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		debugKeyFramesAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpKeyFrameValues("keyFrames.txt", animation);
			}
		});
		
		// Debug parameter values
		debugParameterValuesAction = new BasicAction(getMessage(getParameterValuesMenuLabelKey()), null);
		debugParameterValuesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		debugParameterValuesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		debugParameterValuesAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DebugWriter.dumpParameterValues("parameterValues.txt", 
												animation.getAllParameters(), 
												animation.getFrameOfFirstKeyFrame(), 
												animation.getFrameOfLastKeyFrame(), 
												new AnimationContextImpl(animation));
			}
		});
	}
	
	/**
	 * Create the application menu bar
	 */
	private void initialiseMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu;

		// File menu
		menu = new JMenu(getMessage(getFileMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.add(newAnimationAction);
		menu.add(openAnimationAction);
		menu.add(saveAnimationAction);
		menu.add(saveAnimationAsAction);
		menu.addSeparator();
		this.mruFileMenu = new RecentlyUsedFilesMenuList(this);
		this.mruFileMenu.addToMenu(menu);
		menu.addSeparator();
		menu.add(exitAction);

		// Frame menu
		menu = new JMenu(getMessage(getFrameMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_R);
		menuBar.add(menu);
		menu.add(addKeyAction);
		menu.add(deleteKeyAction);
		menu.addSeparator();
		autoKeyAction.addToMenu(menu);
		menu.add(setFrameCountAction);
		menu.addSeparator();
		menu.add(previousFrameAction);
		menu.add(nextFrameAction);
		menu.add(previous10FramesAction);
		menu.add(next10FramesAction);
		menu.add(firstFrameAction);
		menu.add(lastFrameAction);

		// Animation menu
		menu = new JMenu(getMessage(getAnimationMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);
		useScaledZoomAction.addToMenu(menu);
		menu.add(scaleAnimationAction);
		menu.add(smoothEyeSpeedAction);
		menu.addSeparator();
		menu.add(previewAction);
		menu.add(previewX2Action);
		menu.add(previewX10Action);
		menu.addSeparator();
		menu.add(renderHiResAction);
		menu.add(renderLowResAction);
		menu.addSeparator();
		menu.add(resizeToRenderDimensionsAction);
		
		// Debug
		menu = new JMenu(getMessage(getDebugMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(menu);
		menu.add(debugKeyFramesAction);
		menu.add(debugParameterValuesAction);
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
			Settings.save();
			frame.dispose();
			System.exit(0);
		}
	}

	/**
	 * Set the current animation on the application
	 * 
	 * @param animation The current animation
	 */
	private void setAnimation(Animation animation)
	{
		this.animation = animation;
		if (useScaledZoomAction != null)
		{
			useScaledZoomAction.setSelected(animation.isZoomScalingRequired());
		}
		updateAnimationListeners();
		updateLayersInModel();
		updateSideBar();
	}

	/**
	 * Update the sidebar panels to reflect any changes in the animation structure.
	 */
	private void updateSideBar()
	{
		sideBar.refreshPanels(new ChangeEvent(animation));
	}

	/**
	 * Create a new animation, prompting the user to save any changes if required.
	 */
	private void newFile()
	{
		if (querySave())
		{
			WorldWindAnimationImpl newAnimation = new WorldWindAnimationImpl(wwd);
			addDefaultLayersToAnimation(newAnimation);
			setAnimation(newAnimation);
			resetChanged();
			setFile(null);
			updateSlider();
			slider.setValue(0);
		}
	}

	/**
	 * Add the default layers to the provided animation.
	 * <p/>
	 * Default layers are defined in {@link Settings#getDefaultAnimationLayerUrls()}.
	 * <p/>
	 * Layers will be added with opacity parameters.
	 */
	private void addDefaultLayersToAnimation(Animation animation)
	{
		for (LayerIdentifier layerIdentifier : Settings.get().getDefaultAnimationLayers())
		{
			if (layerIdentifier == null)
			{
				continue;
			}
			
			animation.addLayer(layerIdentifier);
		}
	}

	/**
	 * Prompt the user to open an animation file.
	 */
	private void open()
	{
		if (querySave())
		{
			setupFileChooser(getMessage(getOpenDialogTitleKey()), new XmlFilter());
			if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				File animationFile = fileChooser.getSelectedFile();
				open(animationFile, false);
			}
		}
	}

	/**
	 * Open the provided animation file
	 * 
	 * @param animationFile The file to open
	 * @param promptForSave Whether or not to prompt the user to save their changes, if changes are detected
	 */
	public void open(File animationFile, boolean promptForSave)
	{
		if (promptForSave)
		{
			boolean continueWithOpen = querySave();
			if (!continueWithOpen)
			{
				return;
			}
		}
		
		Animation oldAnimation = animation;
		try
		{
			XmlAnimationReader animationReader = new XmlAnimationReader();
			
			// Check the file version and display appropriate messages
			AnimationFileVersion version = animationReader.getFileVersion(animationFile);
			if (version == null)
			{
				promptUserOpenFailed(animationFile);
				return;
			}
			if (version == AnimationFileVersion.VERSION010)
			{
				int response = promptUserConfirmV1Load(animationFile);
				if (response == JOptionPane.NO_OPTION)
				{
					return;
				}
			}
			
			// Load the file
			Animation newAnimation = animationReader.readAnimation(animationFile, wwd);
			if (version == AnimationFileVersion.VERSION010)
			{
				addDefaultLayersToAnimation(newAnimation);
			}
			
			setAnimation(newAnimation);
			setFile(animationFile);
			
			animation.applyFrame(0);
			setSlider(0);
			
			resetChanged();
			updateSlider();
			
			updateRecentFiles(animationFile);
		}
		catch (Exception e)
		{
			setAnimation(oldAnimation);
			updateSlider();
			
			ExceptionLogger.logException(e);
			promptUserOpenFailed(animationFile);
		}
	}

	private int promptUserConfirmV1Load(File animationFile)
	{
		int response = JOptionPane.showConfirmDialog(frame, 
													 getMessage(getOpenV1FileMessageKey(), animationFile.getAbsolutePath(), XmlAnimationWriter.getCurrentFileVersion().getDisplayName()),
													 getMessage(getOpenV1FileCaptionKey()), 
													 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return response;
	}

	private void promptUserOpenFailed(File animationFile)
	{
		JOptionPane.showMessageDialog(frame, 
									  getMessage(getOpenFailedMessageKey(), animationFile.getAbsolutePath()),
									  getMessage(getOpenFailedCaptionKey()),
									  JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Update the recently used files list with the provided animation file
	 */
	private void updateRecentFiles(File animationFile)
	{
		Settings.get().addRecentFile(animationFile);
		Settings.get().setLastUsedLocation(animationFile);
		mruFileMenu.updateMenuItems();
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
		setupFileChooser(getMessage(getSaveAsDialogTitleKey()), new XmlFilter());
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File newFile = fileChooser.getSelectedFile();
			if (!newFile.getName().toLowerCase().endsWith(XmlFilter.getFileExtension()))
			{
				newFile = new File(newFile.getParent(), newFile.getName() + XmlFilter.getFileExtension());
			}
			boolean override = true;
			if (newFile.exists())
			{
				int response = JOptionPane.showConfirmDialog(frame, 
														 	 getMessage(getConfirmOverwriteMessageKey(), newFile.getAbsolutePath()),
														 	 getMessage(getConfirmOverwriteCaptionKey()),
														 	 JOptionPane.YES_NO_OPTION,
														 	 JOptionPane.WARNING_MESSAGE);
				override = response == JOptionPane.YES_OPTION;
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
	 * Setup the file chooser for use
	 * 
	 * @param title The title to display in the dialog title bar
	 * @param fileFilter The file filter to use for the chooser
	 */
	private void setupFileChooser(String title, FileFilter fileFilter)
	{
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setDialogTitle(title);
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
				ExceptionLogger.logException(e);
				JOptionPane.showMessageDialog(frame, 
											  getMessage(getSaveFailedMessageKey(), e),
											  getMessage(getSaveFailedCaptionKey()),
											  JOptionPane.ERROR_MESSAGE);
			}
			setTitleBar();
			
			updateRecentFiles(file);
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

	/**
	 * Reset the changed flag for the application
	 */
	private void resetChanged()
	{
		animation.removeChangeListener(animationChangeListener);
		animation.addChangeListener(animationChangeListener);
		changed = false;
		setTitleBar();
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
		int response = JOptionPane.showConfirmDialog(frame, 
													 getMessage(getQuerySaveMessageKey(), file), 
													 getMessage(getQuerySaveCaptionKey()),
													 JOptionPane.YES_NO_CANCEL_OPTION,
													 JOptionPane.QUESTION_MESSAGE);
		
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
		String title = getMessage(getAnimatorApplicationTitleKey());
		if (this.file != null)
		{
			file = this.file.getName();
		}
		else
		{
			file = getMessage(getNewAnimationNameKey());
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
		slider.repaint();
	}

	private void setSlider(int frame)
	{
		settingSlider = true;
		slider.setValue(frame);
		settingSlider = false;
	}

	/**
	 * Preview the animation, skipping every <code>frameSkip</code> frames
	 * <p/>
	 * Preview speed can be increased by increasing the number of frames skipped.
	 * 
	 * @param frameSkip The number of frames to skip during preview playback
	 * 
	 * @return the thread in which the preview render is occurring. Can be used to stop the preview.
	 */
	private Thread preview(final int frameSkip)
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
					int frame;
					for (frame = firstFrame; frame <= lastFrame; frame += frameSkip)
					{
						setSlider(frame);
						applyAnimationState();
						wwd.redrawNow();

						if (stop)
						{
							break;
						}
					}
					if (frame != lastFrame && !stop)
					{
						setSlider(lastFrame);
						applyAnimationState();
						wwd.redrawNow();
					}
				}
			});
			thread.start();
			return thread;
		}
		return null;
	}

	/**
	 * Render the current animation, from frame 0 through to <code>frameCount</code>
	 * <p/>
	 * Prompts the user to choose a location to save the rendered image sequence to.
	 * 
	 * @param detailHint The level of detail to use when rendering, on the interval <code>[0,1]</code>
	 * 
	 * @return The thread performing the animation
	 */
	private Thread renderAnimation(final double detailHint)
	{
		int firstFrame = Math.max(slider.getValue(), animation.getFrameOfFirstKeyFrame());
		int lastFrame = animation.getFrameOfLastKeyFrame();
		
		File destination = promptForImageSequenceLocation();
		if (destination == null)
		{
			return null;
		}
		
		return renderAnimation(detailHint, firstFrame, lastFrame, destination.getParentFile(), destination.getName(), true);
	}

	/**
	 * Prompt the user for a location to save a rendered TGA image sequence to.
	 * <p/>
	 * Returns the file in format <code>{destinationFolder}\{sequencePrefixe}</code> (e.g. <code>c:\data\myAnimation\myAnimation</code>)
	 * 
	 * @return The location to save a rendered TGA image sequence to, or <code>null</code> if the user cancelled the operation
	 */
	private File promptForImageSequenceLocation()
	{
		// Prompt for a location to save the image sequence to
		setupFileChooser(getMessage(getSaveRenderDialogTitleKey()), new FileNameExtensionFilter("TGA Image Sequence", "tga"));
		if (fileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
		{
			return null;
		}
		
		File destinationFile = fileChooser.getSelectedFile();
		if (destinationFile == null)
		{
			return null;
		}
		
		// Get the selected name for the image sequence, if one was provided
		String fileName = "frame";
		if (!destinationFile.isDirectory())
		{
			fileName = FileUtil.stripExtension(destinationFile.getName());
			destinationFile = destinationFile.getParentFile();
		}
		
		// Check for existing files and prompt for confirmation if they exist
		int firstFrame = Math.max(slider.getValue(), animation.getFrameOfFirstKeyFrame());
		int lastFrame = animation.getFrameOfLastKeyFrame();
		int filenameLength = String.valueOf(lastFrame).length();
		boolean promptForOverwrite = false;
		for (int i = firstFrame; i <= lastFrame; i++)
		{
			if (new File(destinationFile, createImageSequenceName(fileName, i, filenameLength)).exists())
			{
				promptForOverwrite = true;
				break;
			}
		}
		if (promptForOverwrite)
		{
			int response = JOptionPane.showConfirmDialog(frame, 
					 									 getMessage(getConfirmRenderOverwriteMessageKey(), createImageSequenceName(fileName, firstFrame, filenameLength), createImageSequenceName(fileName, lastFrame, filenameLength)), 
					 									 getMessage(getConfirmRenderOverwriteCaptionKey()),
					 									 JOptionPane.YES_NO_OPTION,
					 									 JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION)
			{
				return null;
			}
		}
		
		return new File(destinationFile, fileName);
	}
	
	/**
	 * @return The name of a file in an image sequence, of the form <code>{prefix}{padded sequence number}.tga</code>
	 */
	private String createImageSequenceName(String prefix, int sequenceNumber, int padTo)
	{
		return prefix + FileUtil.paddedInt(sequenceNumber, padTo) + ".tga";
	}
	
	/**
	 * Render the current animation, between the provided first and last frames, storing the resulting frame images in the provided
	 * output directory.
	 * 
	 * @param detailHint The level of detail to use when rendering, on the interval <code>[0,1]</code>
	 * @param firstFrame The frame to start the animation at
	 * @param lastFrame The frame to finish the animation at
	 * @param outputDir The directory to output the animation files to
	 * @param frameName The name to use as the prefix for the image sequence files (e.g. "myAnimation" results in "myAnimation001.tga", "myAnimation002.tga"...)
	 * @param alpha Whether to include an alpha channel in the rendered image sequence
	 * 
	 * @return The thread performing the rendering
	 */
	private Thread renderAnimation(final double detailHint, final int firstFrame, final int lastFrame, final File outputDir, final String frameName, final boolean alpha)
	{
		
		if (animation != null && animation.hasKeyFrames())
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					
					stop = false;

					boolean wasImmediate = ImmediateMode.isImmediate();
					
					ImmediateMode.setImmediate(true);

					resizeWindowToAnimationSize(animation.getRenderParameters().getImageDimension());

					crosshair.setEnabled(false);
					double detailHintBackup = dem.getDetailHint();
					dem.setDetailHint(detailHint);
					frame.setAlwaysOnTop(true);

					View view = wwd.getView();
					OrbitView orbitView = (OrbitView) view;
					boolean detectCollisions = orbitView.isDetectCollisions();
					orbitView.setDetectCollisions(false);

					int filenameLength = String.valueOf(lastFrame).length();

					AnimatorSceneController asc = (AnimatorSceneController) wwd.getSceneController();
					
					for (int frame = firstFrame; frame <= lastFrame; frame ++)
					{
						setSlider(frame);
						applyAnimationState();
						
						asc.takeScreenshot(new File(outputDir, createImageSequenceName(frameName, frame, filenameLength)), alpha);
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

					ImmediateMode.setImmediate(wasImmediate);
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
									ExceptionLogger.logException(e);
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

	/**
	 * A simple file filter that matches XML files with extension <code>.xml</code> 
	 */
	private static class XmlFilter extends FileFilter
	{
		/**
		 * @return The file extension associated with this filter
		 */
		public static String getFileExtension() { return ".xml";}
		
		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				return true;
			}
			return f.getName().toLowerCase().endsWith(getFileExtension());
		}

		@Override
		public String getDescription()
		{
			return "XML files (*.xml)";
		}
	}
}
