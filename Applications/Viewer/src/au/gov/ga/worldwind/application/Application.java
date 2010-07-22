package au.gov.ga.worldwind.application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.examples.ClickAndGoSelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import nasa.worldwind.awt.WorldWindowStereoGLCanvas;
import nasa.worldwind.retrieve.ExtendedRetrievalService;
import au.gov.ga.worldwind.components.HtmlViewer;
import au.gov.ga.worldwind.layers.LayerFactory;
import au.gov.ga.worldwind.layers.file.FileLayerCreator;
import au.gov.ga.worldwind.layers.mouse.MouseLayer;
import au.gov.ga.worldwind.panels.SideBar;
import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.panels.layers.AbstractLayersPanel;
import au.gov.ga.worldwind.panels.layers.ExtendedLayerList;
import au.gov.ga.worldwind.panels.layers.ILayerNode;
import au.gov.ga.worldwind.panels.layers.LayerEnabler;
import au.gov.ga.worldwind.panels.layers.LayerNode;
import au.gov.ga.worldwind.panels.layers.LayersPanel;
import au.gov.ga.worldwind.panels.layers.QueryClickListener;
import au.gov.ga.worldwind.panels.other.GoToCoordinatePanel;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.SettingsDialog;
import au.gov.ga.worldwind.stereo.StereoOrbitView;
import au.gov.ga.worldwind.stereo.StereoSceneController;
import au.gov.ga.worldwind.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.terrain.SectionListCompoundElevationModel;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemeHUD;
import au.gov.ga.worldwind.theme.ThemeLayer;
import au.gov.ga.worldwind.theme.ThemeOpener;
import au.gov.ga.worldwind.theme.ThemePanel;
import au.gov.ga.worldwind.theme.ThemePiece;
import au.gov.ga.worldwind.theme.ThemeOpener.ThemeOpenDelegate;
import au.gov.ga.worldwind.theme.ThemePiece.ThemePieceAdapter;
import au.gov.ga.worldwind.theme.hud.WorldMapHUD;
import au.gov.ga.worldwind.util.BasicAction;
import au.gov.ga.worldwind.util.DoubleClickZoomListener;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.SelectableAction;
import au.gov.ga.worldwind.util.Util;

public class Application
{
	public final static boolean SANDPIT = true;

	static
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name",
					"World Wind Application");
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

		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		Configuration
				.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactory.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, StereoSceneController.class
				.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ExtendedRetrievalService.class
				.getName());
		//Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, NormalTessellator.class.getName());
	}

	public static void main(String[] args)
	{
		//Settings need to be initialised before Theme is opened, so that proxy values are set
		Settings.get();

		URL themeUrl = null;
		if (args.length > 0)
		{
			try
			{
				themeUrl = new URL(args[0]);
			}
			catch (MalformedURLException e)
			{
			}
		}
		ThemeOpenDelegate delegate = new ThemeOpenDelegate()
		{
			@Override
			public void opened(Theme theme)
			{
				start(theme);
			}
		};
		if (themeUrl == null)
			ThemeOpener.openDefault(delegate);
		else
			ThemeOpener.openTheme(themeUrl, delegate);
	}

	private static Application start(Theme theme)
	{
		if (theme.getInitialLatitude() != null)
			Configuration.setValue(AVKey.INITIAL_LATITUDE, theme.getInitialLatitude());
		if (theme.getInitialLongitude() != null)
			Configuration.setValue(AVKey.INITIAL_LONGITUDE, theme.getInitialLongitude());
		if (theme.getInitialAltitude() != null)
			Configuration.setValue(AVKey.INITIAL_ALTITUDE, theme.getInitialAltitude());
		if (theme.getInitialHeading() != null)
			Configuration.setValue(AVKey.INITIAL_HEADING, theme.getInitialHeading());
		if (theme.getInitialPitch() != null)
			Configuration.setValue(AVKey.INITIAL_PITCH, theme.getInitialPitch());

		WorldWind.getDataFileStore().addLocation("cache", false);

		return new Application(theme);
	}

	private Theme theme;
	private JFrame frame;
	private JFrame fullscreenFrame;

	private WorldWindowStereoGLCanvas wwd;
	private MouseLayer mouseLayer;

	private SideBar sideBar;
	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private JSplitPane splitPane;

	private BasicAction openLayerAction;
	private BasicAction createLayerFromDirectoryAction;
	private SelectableAction offlineAction;
	private BasicAction screenshotAction;
	private BasicAction exitAction;
	private BasicAction defaultViewAction;
	private BasicAction gotoAction;
	private BasicAction fullscreenAction;
	private List<SelectableAction> hudActions = new ArrayList<SelectableAction>();
	private List<SelectableAction> panelActions = new ArrayList<SelectableAction>();
	private BasicAction settingsAction;
	private BasicAction controlsAction;
	private BasicAction aboutAction;

	private Application(Theme theme)
	{
		this.theme = theme;
		Settings.get().loadThemeProperties(theme);

		//initialize frame
		String title = "Geoscience Australia – World Wind";
		if (theme.getName() != null && theme.getName().length() > 0)
			title += " - " + theme.getName();
		frame = new JFrame(title);
		frame.setIconImage(Icons.earth32.getIcon().getImage());

		// show splashscreen
		final SplashScreen splashScreen = new SplashScreen(frame);

		// create worldwind stuff
		if (Settings.get().isHardwareStereoEnabled())
			wwd = new WorldWindowStereoGLCanvas(WorldWindowStereoGLCanvas.stereoCaps);
		else
			wwd = new WorldWindowStereoGLCanvas(WorldWindowStereoGLCanvas.defaultCaps);
		Model model = new BasicModel();
		model.setLayers(new ExtendedLayerList());
		model.getGlobe().setElevationModel(new SectionListCompoundElevationModel());
		wwd.setModel(model);
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));
		create3DMouse();
		createDoubleClickListener();

		//hide splash screen when first frame is rendered
		wwd.addRenderingListener(new RenderingListener()
		{
			public void stageChanged(RenderingEvent event)
			{
				if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
				{
					splashScreen.dispose();
					wwd.removeRenderingListener(this);
				}
			}
		});

		//setup retrieval service layer
		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ExtendedRetrievalService)
		{
			model.getLayers().add(((ExtendedRetrievalService) rs).getLayer());
		}

		//link theme to WorldWindow
		theme.setup(wwd);

		//ensure menu bar and popups appear over the heavyweight WW canvas
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame.setLayout(new BorderLayout());
		frame.setBounds(Settings.get().getWindowBounds());
		if (Settings.get().isWindowMaximized())
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		// panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		panel.add(splitPane, BorderLayout.CENTER);
		splitPane.setRightComponent(wwd);
		splitPane.setOneTouchExpandable(true);
		wwd.setMinimumSize(new Dimension(1, 1));
		loadSplitLocation();

		if (theme.hasStatusBar())
		{
			statusBar = new StatusBar();
			panel.add(statusBar, BorderLayout.PAGE_END);
			statusBar.setEventSource(wwd);
			statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		}

		if (!theme.getPanels().isEmpty())
		{
			sideBar = new SideBar(theme, splitPane);
			splitPane.setLeftComponent(sideBar);
		}
		else
		{
			splitPane.setDividerSize(0);
		}

		//if the theme has some theme layers defined, and the ThemeLayersPanel has not been
		//added, then we need to create a local LayerEnabler and enable the layers manually
		if (!theme.getLayers().isEmpty() && !theme.hasThemeLayersPanel())
		{
			LayerEnabler enabler = new LayerEnabler();
			enabler.setWwd(wwd);
			List<ILayerNode> nodes = new ArrayList<ILayerNode>();
			for (ThemeLayer layer : theme.getLayers())
			{
				nodes.add(LayerNode.createFromLayerDefinition(layer));
			}
			enabler.enable(nodes);
		}

		afterSettingsChange();
		createActions();
		createThemeListeners();

		if (theme.hasMenuBar())
		{
			menuBar = createMenuBar();
			frame.setJMenuBar(menuBar);
		}

		if (theme.hasToolBar())
		{
			toolBar = createToolBar();
			panel.add(toolBar, BorderLayout.PAGE_START);
		}

		addWindowListeners();

		try
		{
			java.awt.EventQueue.invokeAndWait(new Runnable()
			{
				public void run()
				{
					frame.setVisible(true);
				}
			});
		}
		catch (Exception e)
		{
		}
	}

	private void createActions()
	{
		openLayerAction = new BasicAction("Open layer", Icons.folder.getIcon());
		openLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openLayer();
			}
		});

		createLayerFromDirectoryAction =
				new BasicAction("From tileset directory", "Create layer from tileset directory",
						Icons.newfolder.getIcon());
		createLayerFromDirectoryAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createLayerFromDirectory();
			}
		});

		offlineAction =
				new SelectableAction("Work offline", Icons.offline.getIcon(), WorldWind
						.isOfflineMode());
		offlineAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				WorldWind.setOfflineMode(offlineAction.isSelected());
			}
		});

		screenshotAction = new BasicAction("Save image", Icons.screenshot.getIcon());
		screenshotAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveImage();
			}
		});

		exitAction = new BasicAction("Exit", Icons.escape.getIcon());
		exitAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});

		defaultViewAction = new BasicAction("Default view", Icons.home.getIcon());
		defaultViewAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetView();
			}
		});

		gotoAction = new BasicAction("Go to coordinates...", Icons.crosshair45.getIcon());
		gotoAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				GoToCoordinatePanel.showGotoDialog(frame, wwd, "Go to coordinates",
						Icons.crosshair45.getIcon());
			}
		});

		for (ThemePanel panel : theme.getPanels())
		{
			panelActions.add(createThemePieceAction(panel));
		}

		for (ThemeHUD hud : theme.getHUDs())
		{
			hudActions.add(createThemePieceAction(hud));
		}

		fullscreenAction = new BasicAction("Fullscreen", Icons.monitor.getIcon());
		fullscreenAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});

		settingsAction = new BasicAction("Preferences...", Icons.settings.getIcon());
		settingsAction.addActionListener(new ActionListener()
		{
			private boolean visible = false;

			public void actionPerformed(ActionEvent e)
			{
				if (!visible)
				{
					visible = true;
					SettingsDialog settingsDialog =
							new SettingsDialog(frame, "Preferences", Icons.settings.getIcon());
					settingsDialog.setVisible(true);
					visible = false;
					afterSettingsChange();
				}
			}
		});

		controlsAction = new BasicAction("Controls...", Icons.keyboard.getIcon());
		controlsAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showControls();
			}
		});

		aboutAction = new BasicAction("About", Icons.help.getIcon());
		aboutAction.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new AboutDialog(frame);
			}
		});
	}

	private void openLayer()
	{
		LayersPanel panel = theme.getLayersPanel();
		if (panel != null)
			panel.openLayerFile();
	}

	private void createLayerFromDirectory()
	{
		LayersPanel panel = theme.getLayersPanel();
		if (panel != null)
		{
			ILayerDefinition layer =
					FileLayerCreator.createDefinition(frame, createLayerFromDirectoryAction
							.getToolTipText(), panel.getIcon());
			if (layer != null)
			{
				panel.addLayer(layer);
			}
		}
	}

	private void saveImage()
	{
		String[] formats = ImageIO.getWriterFormatNames();
		final Set<String> imageFormats = new HashSet<String>();
		for (String format : formats)
		{
			imageFormats.add(format.toLowerCase());
		}

		JFileChooser chooser = new JFileChooser();
		List<FileFilter> filters = new ArrayList<FileFilter>();
		FileFilter jpgFilter = null;
		for (final String format : imageFormats)
		{
			FileFilter filter = new FileFilter()
			{
				@Override
				public boolean accept(File f)
				{
					if (f.isDirectory())
						return true;
					int index = f.getName().lastIndexOf('.');
					if (index < 0)
						return false;
					String ext = f.getName().substring(index + 1);
					return format.equals(ext.toLowerCase());
				}

				@Override
				public String getDescription()
				{
					return format.toUpperCase() + " image";
				}

				@Override
				public String toString()
				{
					return format;
				}
			};
			filters.add(filter);
			chooser.addChoosableFileFilter(filter);
			if (format.equals("jpg"))
				jpgFilter = filter;
		}
		if (jpgFilter != null)
			chooser.setFileFilter(jpgFilter);

		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			// get filter extension
			String newExt;
			FileFilter filter = chooser.getFileFilter();
			if (filters.contains(filter))
				newExt = filter.toString();
			else
				newExt = "jpg";
			// find file extension
			int index = file.getName().lastIndexOf('.');
			String ext = null;
			if (index > 0)
				ext = file.getName().substring(index + 1);
			// fix/add file extension
			if (ext == null || !newExt.equals(ext.toLowerCase()))
			{
				ext = newExt;
				file = new File(file.getParent(), file.getName() + "." + ext);
			}
			// ask user if they want to overwrite
			if (file.exists())
			{
				int answer =
						JOptionPane.showConfirmDialog(frame, file.getAbsolutePath()
								+ " already exists.\nDo you want to replace it?", "Save image",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer != JOptionPane.YES_OPTION)
					file = null;
			}
			if (file != null)
			{
				Screenshotter.takeScreenshot(wwd, wwd, file);
			}
		}
	}

	@SuppressWarnings("unused")
	private void takeScreenshot(int width, int height, final File file)
	{
		Screenshotter.takeScreenshot(wwd, width, height, file);
	}

	private void addWindowListeners()
	{
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				quit();
			}
		});

		frame.addWindowStateListener(new WindowStateListener()
		{
			public void windowStateChanged(WindowEvent e)
			{
				Settings.get().setWindowMaximized(isMaximized());
			}
		});

		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				if (!isMaximized() && !isFullscreen())
				{
					Settings.get().setWindowBounds(frame.getBounds());
				}
			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				if (!isMaximized() && !isFullscreen())
				{
					Settings.get().setWindowBounds(frame.getBounds());
				}
			}
		});
	}

	private void resetView()
	{
		if (!(wwd.getView() instanceof OrbitView))
			return;

		OrbitView view = (OrbitView) wwd.getView();
		Position beginCenter = view.getCenterPosition();

		Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		Double initAltitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		Double initHeading = Configuration.getDoubleValue(AVKey.INITIAL_HEADING);
		Double initPitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);

		if (initLat == null)
			initLat = 0d;
		if (initLon == null)
			initLon = 0d;
		if (initAltitude == null)
			initAltitude = 3d * Earth.WGS84_EQUATORIAL_RADIUS;
		if (initHeading == null)
			initHeading = 0d;
		if (initPitch == null)
			initPitch = 0d;

		Position endCenter = Position.fromDegrees(initLat, initLon, beginCenter.getElevation());
		long lengthMillis = Util.getScaledLengthMillis(beginCenter, endCenter);

		view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter,
				endCenter, view.getHeading(), Angle.fromDegrees(initHeading), view.getPitch(),
				Angle.fromDegrees(initPitch), view.getZoom(), initAltitude, lengthMillis, true));
		wwd.redraw();
	}

	private void create3DMouse()
	{
		final UserFacingIcon icon =
				new UserFacingIcon("au/gov/ga/worldwind/data/images/cursor.png", new Position(
						Angle.ZERO, Angle.ZERO, 0));
		icon.setSize(new Dimension(16, 32));
		icon.setAlwaysOnTop(true);

		LayerList layers = wwd.getModel().getLayers();
		mouseLayer = new MouseLayer(wwd, icon);
		layers.add(mouseLayer);

		enableMouseLayer();
	}

	private void createDoubleClickListener()
	{
		wwd.getInputHandler().addMouseListener(new DoubleClickZoomListener(wwd, 5000d));
	}

	private void enableMouseLayer()
	{
		mouseLayer.setEnabled(Settings.get().isStereoEnabled() && Settings.get().isStereoCursor());
	}

	public boolean isMaximized()
	{
		return (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
	}

	public boolean isFullscreen()
	{
		return fullscreenFrame != null;
	}

	public void setFullscreen(boolean fullscreen)
	{
		if (fullscreen != isFullscreen())
		{
			if (fullscreen)
			{
				boolean span = Settings.get().isSpanDisplays();
				String id = Settings.get().getDisplayId();

				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gds = ge.getScreenDevices();

				saveSplitLocation();
				fullscreenFrame = new JFrame(frame.getTitle());
				JPanel panel = new JPanel(new BorderLayout());
				fullscreenFrame.setContentPane(panel);
				fullscreenFrame.setUndecorated(true);
				fullscreenFrame.add(wwd);
				fullscreenFrame.setAlwaysOnTop(true);

				fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				fullscreenFrame.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent e)
					{
						setFullscreen(false);
					}
				});

				Action action = new AbstractAction()
				{
					public void actionPerformed(ActionEvent e)
					{
						setFullscreen(false);
					}
				};
				panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), action);
				panel.getActionMap().put(action, action);

				if (span)
				{
					Rectangle fullBounds = new Rectangle();
					for (GraphicsDevice g : gds)
					{
						GraphicsConfiguration gc = g.getDefaultConfiguration();
						fullBounds = fullBounds.union(gc.getBounds());
					}
					fullscreenFrame.setBounds(fullBounds);
				}
				else if (id != null)
				{
					for (GraphicsDevice g : gds)
					{
						if (id.equals(g.getIDstring()))
						{
							GraphicsConfiguration gc = g.getDefaultConfiguration();
							fullscreenFrame.setBounds(gc.getBounds());
							break;
						}
					}
				}
				fullscreenFrame.setVisible(true);
				frame.setVisible(false);
			}
			else
			{
				if (fullscreenFrame != null)
				{
					splitPane.setRightComponent(wwd);
					fullscreenFrame.dispose();
					fullscreenFrame = null;
					loadSplitLocation();
					frame.setVisible(true);
				}
			}
		}
	}

	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();

		if (theme.hasLayersPanel())
		{
			toolBar.add(openLayerAction);
			toolBar.addSeparator();
		}

		toolBar.add(screenshotAction);

		toolBar.addSeparator();
		toolBar.add(defaultViewAction);
		toolBar.add(gotoAction);
		toolBar.add(fullscreenAction);

		toolBar.addSeparator();
		for (SelectableAction action : panelActions)
			action.addToToolBar(toolBar);

		toolBar.addSeparator();
		for (SelectableAction action : hudActions)
			action.addToToolBar(toolBar);

		toolBar.addSeparator();
		toolBar.add(settingsAction);

		return toolBar;
	}

	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu, submenu;

		menu = new JMenu("File");
		menuBar.add(menu);

		if (theme.hasLayersPanel())
		{
			menu.add(openLayerAction);

			submenu = new JMenu("Create layer");
			submenu.setIcon(Icons.newfile.getIcon());
			menu.add(submenu);

			submenu.add(createLayerFromDirectoryAction);

			menu.addSeparator();
		}

		offlineAction.addToMenu(menu);

		menu.addSeparator();
		menu.add(screenshotAction);

		menu.addSeparator();
		menu.add(exitAction);

		menu = new JMenu("View");
		menuBar.add(menu);

		menu.add(defaultViewAction);
		menu.add(gotoAction);
		menu.add(fullscreenAction);

		menu.addSeparator();
		for (SelectableAction action : panelActions)
		{
			action.addToMenu(menu);
		}

		menu.addSeparator();
		for (SelectableAction action : hudActions)
		{
			action.addToMenu(menu);
		}

		menu = new JMenu("Options");
		menuBar.add(menu);

		menu.add(settingsAction);

		menu = new JMenu("Help");
		menuBar.add(menu);

		menu.add(controlsAction);
		menu.add(aboutAction);

		return menuBar;
	}

	private SelectableAction createThemePieceAction(final ThemePiece piece)
	{
		final SelectableAction action =
				new SelectableAction(piece.getDisplayName(), piece.getIcon(), piece.isOn());
		action.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				piece.setOn(action.isSelected());
			}
		});
		piece.addListener(new ThemePieceAdapter()
		{
			@Override
			public void onToggled(ThemePiece source)
			{
				action.setSelected(source.isOn());
			}
		});
		return action;
	}

	private void showControls()
	{
		JDialog dialog =
				new HtmlViewer(frame, "Controls", false,
						"/au/gov/ga/worldwind/data/help/controls.html", true);
		dialog.setResizable(false);
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private void createThemeListeners()
	{
		for (ThemePanel panel : theme.getPanels())
		{
			if (panel instanceof AbstractLayersPanel)
			{
				AbstractLayersPanel layersPanel = (AbstractLayersPanel) panel;
				layersPanel.addQueryClickListener(new QueryClickListener()
				{
					@Override
					public void queryURLClicked(URL url)
					{
						initDataQuery(url);
					}
				});
			}
		}
	}

	private void initDataQuery(URL queryURL)
	{
		Position pos = ((OrbitView) wwd.getView()).getCenterPosition();
		double small = 1e-5;
		String bbox =
				(pos.getLongitude().degrees - small) + "," + (pos.getLatitude().degrees - small)
						+ "," + (pos.getLongitude().degrees + small) + ","
						+ (pos.getLatitude().degrees + small);
		String external = queryURL.toExternalForm();
		String placeholder = "#bbox#";
		int index = external.indexOf(placeholder);
		if (index >= 0)
		{
			external =
					external.substring(0, index) + bbox
							+ external.substring(index + placeholder.length());
		}

		System.out.println(external);
		try
		{
			URL url = new URL(external);
			HtmlViewer viewer = new HtmlViewer(frame, "Data", url, null);
			viewer.setSize(640, 480);
			viewer.setVisible(true);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	/*private void showDataSources()
	{
		JDialog dialog =
				new HtmlViewer(frame, "Data sources", false,
						"/au/gov/ga/worldwind/data/help/datasources.html", false);
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}*/

	private void afterSettingsChange()
	{
		/*if (Settings.get().isStereoEnabled()
				&& Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
		{
			layersPanel.turnOffAtmosphere();
		}*/
		for (ThemeHUD hud : theme.getHUDs())
		{
			if (hud instanceof WorldMapHUD)
			{
				((WorldMapHUD) hud).setPickEnabled(!(Settings.get().isStereoEnabled() && Settings
						.get().isStereoCursor()));
			}
		}
		enableMouseLayer();
	}

	public void quit()
	{
		saveSplitLocation();
		Settings.get().saveThemeProperties(theme);
		Settings.get().save();
		theme.dispose();
		frame.dispose();
		System.exit(0);
	}

	private void saveSplitLocation()
	{
		if (sideBar != null)
		{
			if (sideBar.isVisible())
				Settings.get().setSplitLocation(splitPane.getDividerLocation());
			else
				Settings.get().setSplitLocation(sideBar.getSavedDividerLocation());
		}
	}

	private void loadSplitLocation()
	{
		splitPane.setDividerLocation(Settings.get().getSplitLocation());
	}
}
