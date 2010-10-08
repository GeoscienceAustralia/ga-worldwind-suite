package au.gov.ga.worldwind.animator.application;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
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
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.settings.RecentlyUsedFilesMenuList;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.camerapath.CameraPathLayer;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.panels.CollapsiblePanel;
import au.gov.ga.worldwind.animator.panels.SideBar;
import au.gov.ga.worldwind.animator.panels.animationbrowser.AnimationBrowserPanel;
import au.gov.ga.worldwind.animator.panels.layerpalette.LayerPalettePanel;
import au.gov.ga.worldwind.animator.panels.objectproperties.ObjectPropertiesPanel;
import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierFactory;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.ui.ExaggeratorDialog;
import au.gov.ga.worldwind.animator.ui.frameslider.ChangeFrameListener;
import au.gov.ga.worldwind.animator.ui.frameslider.CurrentFrameChangeListener;
import au.gov.ga.worldwind.animator.ui.frameslider.FrameSlider;
import au.gov.ga.worldwind.animator.util.ExaggerationAwareStatusBar;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.FileFilters;
import au.gov.ga.worldwind.animator.util.FileFilters.XmlFilter;
import au.gov.ga.worldwind.animator.util.FileUtil;
import au.gov.ga.worldwind.common.ui.SplashScreen;

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
	public static void main(String[] args)
	{
		launchAnimatorApplication();
	}

	static
	{
		AnimatorConfiguration.initialiseConfiguration();
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
	private Layer crosshair;
	private CameraPathLayer cameraPathLayer;
	
	/** The file chooser used for open and save. Instance variable so it will remember last used folders. */
	private JFileChooser fileChooser;

	/** The menu list of recently used files */
	private RecentlyUsedFilesMenuList mruFileMenu;

	/** The action factory used to obtain application actions */
	private AnimatorActionFactory actionFactory;

	/** A clipboard used to perform cut-copy-paste operations on key frames */
	private KeyFrameClipboard keyFrameClipboard;
	
	/**
	 * Launch an instance of the Animator Application
	 */
	public static final void launchAnimatorApplication()
	{
		new Animator();
	}
	
	public Animator()
	{
		initialiseApplicationWindow();
		initialiseWorldWindow();
		
		showSplashScreen();
		
		initialiseAnimation();
		
		initialiseUtilityLayers();
		updateLayersInModel();
		updateElevationModelOnGlobe();
		
		initialiseFrameSlider();
		initialiseKeyFrameClipboard();
		initialiseSideBar();
		initialiseStatusBar();
		initialiseMenuBar();
		
		initialiseAnimationListeners();
		
		updateSlider();
		resetChanged();
		
		showApplicationWindow();
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
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(Settings.get().getLastUsedLocation());
	}

	/**
	 * Initialise the World Wind {@link WorldWindow} used by the application
	 */
	private void initialiseWorldWindow()
	{
		wwd = new WorldWindowGLCanvas(AnimatorConfiguration.getGLCapabilities());
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
			    
			    double targetRatio = getCurrentAnimation().getRenderParameters().getImageAspectRatio();
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
	 * Show a splash screen on load
	 */
	private void showSplashScreen()
	{
		SplashScreen splashScreen = new SplashScreen(frame);
		splashScreen.addRenderingListener(wwd);
	}
	
	/**
	 * Initialise the animation
	 */
	private void initialiseAnimation()
	{
		animation = new WorldWindAnimationImpl(wwd);
		addDefaultLayersToAnimation(getCurrentAnimation());
		addDefaultElevationModelsToAnimation(getCurrentAnimation());
		updater = new Updater(this);
		
		updateTitleBar();
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
	 * Add the default elevation models to the provided animation
	 */
	private void addDefaultElevationModelsToAnimation(Animation animation)
	{
		for (ElevationModelIdentifier modelIdentifier : Settings.get().getDefaultElevationModels())
		{
			if (modelIdentifier == null)
			{
				continue;
			}
			
			animation.addElevationModel(modelIdentifier);
		}
	}
	
	/**
	 * Set the title bar of the application window. 
	 * <p/>
	 * The application title will include the file name of the current animation, and an 'isChanged' indicator. 
	 */
	private void updateTitleBar()
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
	
	/**
	 * Initialise the utility layers used inside the animator application
	 */
	private void initialiseUtilityLayers()
	{
		if (cameraPathLayer == null)
		{
			cameraPathLayer = new CameraPathLayer(getCurrentAnimation());
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
		for (Layer layer : getCurrentAnimation().getLayers())
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
	 * Apply the elevation model associated with the current animation to the world wind glob.
	 */
	private void updateElevationModelOnGlobe()
	{
		model.getGlobe().setElevationModel(getCurrentAnimation().getRootElevationModel());
	}
	
	/**
	 * Initialise the frame slider and add it to the content panel
	 */
	private void initialiseFrameSlider()
	{
		slider = new FrameSlider(0, 0, getCurrentAnimation().getFrameCount());
		mainPanel.add(slider, BorderLayout.SOUTH);
		
		slider.addChangeListener(new CurrentFrameChangeListener()
		{
			@Override
			public void currentFrameChanged(int newCurrentFrame)
			{
				if (!settingSlider)
				{
					getCurrentAnimation().setCurrentFrame(newCurrentFrame);
					if (getCurrentAnimation().getKeyFrameCount() > 0)
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
				KeyFrame oldKey = getCurrentAnimation().getKeyFrame(oldFrame);
				KeyFrame newKey = new KeyFrameImpl(newFrame, oldKey.getParameterValues());

				getCurrentAnimation().removeKeyFrame(oldKey);
				getCurrentAnimation().insertKeyFrame(newKey);
				
				updateSlider();
				applyAnimationState();
				wwd.redraw();
			}
		});
	}
	
	private void initialiseKeyFrameClipboard()
	{
		keyFrameClipboard = new KeyFrameClipboard(getCurrentAnimation());
		slider.addChangeListener(keyFrameClipboard);
		slider.addChangeFrameListener(keyFrameClipboard);
		animation.addChangeListener(keyFrameClipboard);
	}
	
	/**
	 * Initialise the side bar, which contains a group of collapsible panels
	 */
	private void initialiseSideBar()
	{
		animationBrowserPanel = new AnimationBrowserPanel(getCurrentAnimation());
		objectPropertiesPanel = new ObjectPropertiesPanel(getCurrentAnimation());
		layerPalettePanel = new LayerPalettePanel(getCurrentAnimation());
		
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
	 * Create a status bar and put it in bottom panel
	 */
	private void initialiseStatusBar()
	{
		ExaggerationAwareStatusBar statusBar = new ExaggerationAwareStatusBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar.setEventSource(wwd);
		
		bottomPanel.add(statusBar, BorderLayout.SOUTH);
	}
	
	/**
	 * Create the application menu bar
	 */
	private void initialiseMenuBar()
	{
		actionFactory = new AnimatorActionFactory(this);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu;

		// File menu
		menu = new JMenu(getMessage(getFileMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		menu.add(actionFactory.getNewAnimationAction());
		menu.add(actionFactory.getOpenAnimationAction());
		menu.add(actionFactory.getSaveAnimationAction());
		menu.add(actionFactory.getSaveAnimationAsAction());
		menu.addSeparator();
		this.mruFileMenu = new RecentlyUsedFilesMenuList(this);
		this.mruFileMenu.addToMenu(menu);
		menu.addSeparator();
		menu.add(actionFactory.getExitAction());

		// Frame menu
		menu = new JMenu(getMessage(getFrameMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_R);
		menuBar.add(menu);
		menu.add(actionFactory.getAddKeyAction());
		menu.add(actionFactory.getDeleteKeyAction());
		menu.addSeparator();
		menu.add(keyFrameClipboard.getCutAction());
		menu.add(keyFrameClipboard.getCopyAction());
		menu.add(keyFrameClipboard.getPasteAction());
		menu.addSeparator();
		actionFactory.getAutoKeyAction().addToMenu(menu);
		menu.add(actionFactory.getSetFrameCountAction());
		menu.addSeparator();
		menu.add(actionFactory.getPreviousFrameAction());
		menu.add(actionFactory.getNextFrameAction());
		menu.add(actionFactory.getPrevious10FramesAction());
		menu.add(actionFactory.getNext10FramesAction());
		menu.add(actionFactory.getFirstFrameAction());
		menu.add(actionFactory.getLastFrameAction());

		// Animation menu
		menu = new JMenu(getMessage(getAnimationMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);
		actionFactory.getUseScaledZoomAction().addToMenu(menu);
		menu.add(actionFactory.getScaleAnimationAction());
		menu.add(actionFactory.getSmoothEyeSpeedAction());
		menu.addSeparator();
		menu.add(actionFactory.getPreviewAction());
		menu.add(actionFactory.getPreviewX2Action());
		menu.add(actionFactory.getPreviewX10Action());
		menu.addSeparator();
		menu.add(actionFactory.getRenderHiResAction());
		menu.add(actionFactory.getRenderLowResAction());
		menu.addSeparator();
		menu.add(actionFactory.getResizeToRenderDimensionsAction());
		menu.addSeparator();
		menu.add(actionFactory.getAddElevationModelAction());
		menu.add(actionFactory.getAddExaggeratorAction());
		
		// Debug
		menu = new JMenu(getMessage(getDebugMenuLabelKey()));
		menu.setMnemonic(KeyEvent.VK_D);
		menuBar.add(menu);
		menu.add(actionFactory.getDebugKeyFramesAction());
		menu.add(actionFactory.getDebugParameterValuesAction());
	}
	
	/**
	 * Initialise the animation listeners
	 */
	private void initialiseAnimationListeners()
	{
		initialiseAutoKeyListener();
		initialiseAnimationChangeListener();
		initialiseLayerUpdateListener();
		initialiseFramesChangedListener();
		initialiseHighlightedFramesListener();
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
	 * Initialise the change listener that listens for changes to the animation state
	 */
	private void initialiseAnimationChangeListener()
	{
		animationChangeListener = new AnimationEventListener()
		{
			@Override
			public void receiveAnimationEvent(AnimationEvent event)
			{
				changed = true;
				updateTitleBar();
			}
		};
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
		getCurrentAnimation().addChangeListener(layerUpdateListener);
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
		getCurrentAnimation().addChangeListener(framesChangedListener);
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
				for (KeyFrame keyFrame : getCurrentAnimation().getKeyFrames(parameter))
				{
					result.add(keyFrame.getFrame());
				}
				return result;
			}
		};
		CurrentlySelectedObject.addChangeListener(highlightedFramesListener);
	}
	
	/**
	 * Re-attach the animation listeners to the current animation. Used when the animation changes (open, new file etc.)
	 */
	private void updateAnimationListeners()
	{
		updateAnimationListener(layerUpdateListener);
		updateAnimationListener(framesChangedListener);
		updateAnimationListener(keyFrameClipboard);
		// TODO: Add more listeners here as they are added
		
	}
	
	private void updateAnimationListener(AnimationEventListener listener)
	{
		getCurrentAnimation().removeChangeListener(listener);
		getCurrentAnimation().addChangeListener(listener);
	}
	
	void updateSlider()
	{
		slider.clearKeys();
		for (KeyFrame keyFrame : getCurrentAnimation().getKeyFrames())
		{
			slider.addKey(keyFrame.getFrame());
		}
		slider.setMin(0);
		slider.setMax(getCurrentAnimation().getFrameCount());
		slider.repaint();
	}
	
	/**
	 * Reset the changed flag for the application
	 */
	private void resetChanged()
	{
		getCurrentAnimation().removeChangeListener(animationChangeListener);
		getCurrentAnimation().addChangeListener(animationChangeListener);
		changed = false;
		updateTitleBar();
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
					resizeWindowToRenderDimensions();
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
	 * @return A new animation context
	 */
	protected AnimationContext createAnimationContext()
	{
		return new AnimationContextImpl(getCurrentAnimation());
	}

	private OrbitView getView()
	{
		return (OrbitView) wwd.getView();
	}

	void addFrame()
	{
		OrbitView view = getView();
		if (view.getPitch().equals(Angle.ZERO))
		{
			view.setPitch(Angle.fromDegrees(0.1));
			wwd.redrawNow();
		}
		updater.addFrame(slider.getValue());
	}

	/**
	 * Apply the animation state at the frame selected on the frame slider
	 */
	private void applyAnimationState()
	{
		int frame = slider.getValue();
		
		applying = true;
		getCurrentAnimation().applyFrame(frame);
		applying = false;
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
	 * Update the sidebar panels to reflect any changes in the animation structure.
	 */
	private void updateSideBar()
	{
		sideBar.refreshPanels(new ChangeEvent(getCurrentAnimation()));
	}

	/**
	 * Create a new animation, prompting the user to save any changes if required.
	 */
	void newFile()
	{
		if (querySave())
		{
			WorldWindAnimationImpl newAnimation = new WorldWindAnimationImpl(wwd);
			addDefaultLayersToAnimation(newAnimation);
			addDefaultElevationModelsToAnimation(newAnimation);
			setCurrentAnimation(newAnimation);
			resetChanged();
			setFile(null);
			updateSlider();
			slider.setValue(0);
		}
	}

	/**
	 * Prompt the user to open an animation file.
	 */
	void open()
	{
		if (querySave())
		{
			setupFileChooser(getMessage(getOpenDialogTitleKey()), FileFilters.getXmlFilter());
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
		
		Animation oldAnimation = getCurrentAnimation();
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
				addDefaultElevationModelsToAnimation(newAnimation);
			}
			
			setCurrentAnimation(newAnimation);
			setFile(animationFile);
			
			getCurrentAnimation().applyFrame(0);
			setSlider(0);
			
			resetChanged();
			updateSlider();
			
			updateRecentFiles(animationFile);
		}
		catch (Exception e)
		{
			setCurrentAnimation(oldAnimation);
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
	void save()
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
	void saveAs()
	{
		setupFileChooser(getMessage(getSaveAsDialogTitleKey()), FileFilters.getXmlFilter());
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
				writer.writeAnimation(file, getCurrentAnimation());
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
			updateTitleBar();
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
		updateTitleBar();
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
	Thread preview(final int frameSkip)
	{
		if (getCurrentAnimation() != null && getCurrentAnimation().hasKeyFrames())
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					stop = false;

					int firstFrame = Math.max(slider.getValue(), 0);
					int lastFrame = getCurrentAnimation().getFrameOfLastKeyFrame();
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
	Thread renderAnimation(final double detailHint)
	{
		int firstFrame = Math.max(slider.getValue(), getCurrentAnimation().getFrameOfFirstKeyFrame());
		int lastFrame = getCurrentAnimation().getFrameOfLastKeyFrame();
		
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
		setupFileChooser(getMessage(getSaveRenderDialogTitleKey()), FileFilters.getTgaFilter());
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
		int firstFrame = Math.max(slider.getValue(), getCurrentAnimation().getFrameOfFirstKeyFrame());
		int lastFrame = getCurrentAnimation().getFrameOfLastKeyFrame();
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
		
		if (getCurrentAnimation() != null && getCurrentAnimation().hasKeyFrames())
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					
					stop = false;

					boolean wasImmediate = ImmediateMode.isImmediate();
					
					ImmediateMode.setImmediate(true);

					resizeWindowToRenderDimensions();

					crosshair.setEnabled(false);
					double detailHintBackup = getDetailedElevationModel().getDetailHint();
					getDetailedElevationModel().setDetailHint(detailHint);
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

					getDetailedElevationModel().setDetailHint(detailHintBackup);
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

	private DetailedElevationModel getDetailedElevationModel()
	{
		return getCurrentAnimation().getAnimatableElevation().getRootElevationModel();
	}
	
	/**
	 * Prompt the user to add an elevation model to the animation
	 */
	void promptToAddElevationModel()
	{
		File selectedDefinitionFile = promptUserForElevationModelDefinition();
		if (selectedDefinitionFile == null)
		{
			return;
		}
		
		try
		{
			addElevationModelFromDefinitionFile(selectedDefinitionFile.toURI().toURL());
			Settings.get().setLastUsedLocation(selectedDefinitionFile);
		}
		catch (MalformedURLException e)
		{
			// URL came from a file, should never be malformed
		}
	}
	
	private void addElevationModelFromDefinitionFile(URL fileUrl)
	{
		if (fileUrl == null)
		{
			return;
		}
		
		ElevationModelIdentifier modelIdentifier = ElevationModelIdentifierFactory.createFromDefinition(fileUrl);
		if (modelIdentifier == null)
		{
			promptUserInvalidModelIdentifier(fileUrl);
			promptToAddElevationModel();
		}
		
		getCurrentAnimation().addElevationModel(modelIdentifier);
	}

	private void promptUserInvalidModelIdentifier(URL fileUrl)
	{
		JOptionPane.showMessageDialog(frame, 
									  getMessage(getOpenElevationModelFailedMessageKey(), fileUrl.getFile()),
									  getMessage(getOpenElevationModelFailedCaptionKey()),
									  JOptionPane.ERROR_MESSAGE);
		
	}

	private File promptUserForElevationModelDefinition()
	{
		setupFileChooser(getMessage(getSaveRenderDialogTitleKey()), new FileNameExtensionFilter("Elevation  model definition", "xml"));
		fileChooser.setMultiSelectionEnabled(false);
		int userAction = fileChooser.showOpenDialog(frame);
		if (userAction == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * Prompt the user to add a new elevation exaggerator to the animation
	 */
	void promptToAddElevationExaggerator()
	{
		ElevationExaggeration exaggerator = ExaggeratorDialog.collectExaggeration(frame);
		if (exaggerator == null)
		{
			return;
		}
		
		getCurrentAnimation().getAnimatableElevation().addElevationExaggerator(exaggerator);
	}
	
	void moveToPreviousFrame()
	{
		slider.setValue(slider.getValue() - 1);
	}
	
	void deleteSelectedKey()
	{
		int frame = slider.getValue();
		if (frame >= 0)
		{
			getCurrentAnimation().removeKeyFrame(frame);
		}
		updateSlider();
	}

	void moveToNextFrame()
	{
		slider.setValue(slider.getValue() + 1);
	}

	void moveToPrevious10Frame()
	{
		slider.setValue(slider.getValue() - 10);
	}

	void moveToNext10Frame()
	{
		slider.setValue(slider.getValue() + 10);
	}

	void moveToFirstFrame()
	{
		slider.setValue(getCurrentAnimation().getFrameOfFirstKeyFrame());
	}

	void moveToLastFrame()
	{
		slider.setValue(getCurrentAnimation().getFrameOfLastKeyFrame());
	}

	void scaleAnimation()
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
			getCurrentAnimation().scale(scale);
		}
		updateSlider();
	}

	void smoothEyeSpeed()
	{
		if (JOptionPane.showConfirmDialog(frame,
										  getMessage(getQuerySmoothEyeSpeedMessageKey()),
										  getMessage(getQuerySmoothEyeSpeedCaptionKey()), 
										  JOptionPane.YES_NO_OPTION, 
										  JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		{
			getCurrentAnimation().getCamera().smoothEyeSpeed(createAnimationContext());
			updateSlider();
		}
	}

	void resizeWindowToRenderDimensions()
	{
		resizeWindowToAnimationSize(getCurrentAnimation().getRenderParameters().getImageDimension());
	}

	void promptToSetFrameCount()
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
		getCurrentAnimation().setFrameCount(frames);
		updateSlider();
	}

	void setAutokey(boolean autokey)
	{
		this.autokey = autokey;
	}
	
	void setZoomScalingRequired(boolean zoomScalingRequired)
	{
		getCurrentAnimation().setZoomScalingRequired(zoomScalingRequired);
	}
	
	/**
	 * Set the current animation on the application
	 * 
	 * @param animation The current animation
	 */
	private void setCurrentAnimation(Animation animation)
	{
		this.animation = animation;
		if (actionFactory != null)
		{
			actionFactory.getUseScaledZoomAction().setSelected(animation.isZoomScalingRequired());
		}
		keyFrameClipboard.updateAnimation(animation);
		updateAnimationListeners();
		updateLayersInModel();
		updateElevationModelOnGlobe();
		updateSideBar();
	}
	
	public Animation getCurrentAnimation()
	{
		return animation;
	}
}
