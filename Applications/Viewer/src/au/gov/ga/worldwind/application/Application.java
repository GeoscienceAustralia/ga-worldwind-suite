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
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import nasa.worldwind.awt.WorldWindowStereoGLCanvas;
import nasa.worldwind.retrieve.ExtendedRetrievalService;
import au.gov.ga.worldwind.components.HtmlViewer;
import au.gov.ga.worldwind.layers.mouse.MouseLayer;
import au.gov.ga.worldwind.panels.SideBar;
import au.gov.ga.worldwind.panels.layers.ExtendedCompoundElevationModel;
import au.gov.ga.worldwind.panels.layers.ExtendedLayerList;
import au.gov.ga.worldwind.panels.layers.LayerFactory;
import au.gov.ga.worldwind.panels.other.GoToCoordinatePanel;
import au.gov.ga.worldwind.panels.places.PlaceEditor;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.SettingsDialog;
import au.gov.ga.worldwind.stereo.StereoOrbitView;
import au.gov.ga.worldwind.stereo.StereoSceneController;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemeHUD;
import au.gov.ga.worldwind.theme.ThemeOpener;
import au.gov.ga.worldwind.theme.ThemePanel;
import au.gov.ga.worldwind.theme.ThemePiece;
import au.gov.ga.worldwind.theme.ThemeOpener.ThemeOpenDelegate;
import au.gov.ga.worldwind.theme.ThemePiece.ThemePieceAdapter;
import au.gov.ga.worldwind.util.DoubleClickZoomListener;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;

public class Application
{
	//public final static boolean LOCAL_LAYERS_ENABLED = true;

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
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, StereoSceneController.class
				.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ExtendedRetrievalService.class
				.getName());
		//Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, AWTInputHandler.class.getName());
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
	private JSplitPane splitPane;

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
		model.getGlobe().setElevationModel(new ExtendedCompoundElevationModel());
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

		if (theme.hasStatusBar())
		{
			statusBar = new StatusBar();
			panel.add(statusBar, BorderLayout.PAGE_END);
			statusBar.setEventSource(wwd);
			statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		}

		if (!theme.getPanels().isEmpty())
		{
			sideBar = new SideBar(theme);
			splitPane.setLeftComponent(sideBar);
		}

		loadSplitLocation();
		afterSettingsChange();

		// init user layers
		/*if (LOCAL_LAYERS_ENABLED)
		{
			LocalLayers.init(wwd);
			layersPanel.updateLocalLayers();
			LocalLayers.get().addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					layersPanel.updateLocalLayers();
				}
			});
		}*/

		if (theme.hasMenuBar())
		{
			menuBar = createMenuBar();
			frame.setJMenuBar(menuBar);
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

	/*private void createDialogs()
	{
		annotationsDialog = createDialog("Annotations");
		annotationsPanel = new AnnotationsPanel(wwd, frame);
		annotationsDialog.add(annotationsPanel, BorderLayout.CENTER);
		annotationsDialog.setJMenuBar(createAnnotationsMenuBar());
		dialogs.add(annotationsDialog);

		placesearchDialog = createDialog("Place search");
		placesearchDialog.add(new PlaceSearchPanel(wwd), BorderLayout.CENTER);
		dialogs.add(placesearchDialog);


		JVisibleDialog sunPositionDialog = createDialog("Sun position");
		sunPositionDialog.add(new SunPositionPanel(wwd), BorderLayout.CENTER);
		dialogs.add(sunPositionDialog);

		loadDialogBounds();
	}*/

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

	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menuBar.add(menu);

		final JCheckBoxMenuItem offline =
				new JCheckBoxMenuItem("Work offline", Icons.offline.getIcon());
		menu.add(offline);
		offline.setSelected(WorldWind.isOfflineMode());
		offline.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				WorldWind.setOfflineMode(offline.isSelected());
			}
		});

		/*if (LOCAL_LAYERS_ENABLED)
		{
			menuItem = new JMenuItem("Add local tileset...");
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					LocalLayerDefinition def = new LocalLayerDefinition();
					def = LocalLayerEditor.editDefinition(frame, "New local tileset", def);
					if (def != null)
					{
						LocalLayers.get().addLayer(def);
					}
				}
			});
		}*/

		menu.addSeparator();

		menuItem = new JMenuItem("Save image", Icons.screenshot.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveImage();
			}
		});


		/*menuItem = new JMenuItem("Save large image", Icons.screenshot.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				takeScreenshot(4000, 4000, new File("largescreeshot.png"));
			}
		});*/

		menu.addSeparator();

		menuItem = new JMenuItem("Exit", Icons.escape.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});

		menu = new JMenu("View");
		menuBar.add(menu);

		menuItem = new JMenuItem("Default view", Icons.home.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetView();
			}
		});

		menuItem = new JMenuItem("Go to coordinates...", Icons.crosshair45.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				GoToCoordinatePanel.showGotoDialog(frame, wwd, "Go to coordinates",
						Icons.crosshair45.getIcon());
			}
		});

		menu.addSeparator();

		for (ThemePanel panel : theme.getPanels())
		{
			menuItem = createThemePieceMenuItem(panel);
			menu.add(menuItem);
		}

		menu.addSeparator();

		for (ThemeHUD hud : theme.getHUDs())
		{
			menuItem = createThemePieceMenuItem(hud);
			menu.add(menuItem);
		}

		menu.addSeparator();

		menuItem = new JMenuItem("Fullscreen", Icons.monitor.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});

		/*bookmarksMenu = new JMenu("Bookmarks");
		menuBar.add(bookmarksMenu);

		menuItem = new JMenuItem("Add bookmark...");
		bookmarksMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BookmarkManager.addBookmark(frame, wwd);
			}
		});

		menuItem = new JMenuItem("Organise bookmarks...");
		bookmarksMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BookmarkManager bm = new BookmarkManager(frame, "Organise bookmarks");
				bm.setSize(400, 300);
				bm.setLocationRelativeTo(frame);
				bm.setVisible(true);
			}
		});

		bookmarksMenu.addSeparator();
		BookmarkListener bl = new BookmarkListener()
		{
			public void modified()
			{
				updateBookmarksMenu();
			}
		};
		Bookmarks.addBookmarkListener(bl);
		bl.modified();*/

		menu = new JMenu("Options");
		menuBar.add(menu);

		menuItem = new JMenuItem("Preferences...", Icons.settings.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			private boolean visible = false;

			public void actionPerformed(ActionEvent e)
			{
				if (!visible)
				{
					visible = true;
					SettingsDialog settingsDialog = new SettingsDialog(frame);
					settingsDialog.setVisible(true);
					visible = false;
					afterSettingsChange();
				}
			}
		});

		menu = new JMenu("Help");
		menuBar.add(menu);

		menuItem = new JMenuItem("Controls...", Icons.keyboard.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showControls();
			}
		});

		/*menuItem = new JMenuItem("Data sources...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showDataSources();
			}
		});

		menu.addSeparator();*/

		menuItem = new JMenuItem("About", Icons.help.getIcon());
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new AboutDialog(frame);
			}
		});

		return menuBar;
	}

	private JMenuItem createThemePieceMenuItem(final ThemePiece piece)
	{
		final JCheckBoxMenuItem menuItem =
				new JCheckBoxMenuItem(piece.getDisplayName(), piece.isOn());
		menuItem.setIcon(piece.getIcon());
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				piece.setOn(menuItem.isSelected());
			}
		});
		piece.addListener(new ThemePieceAdapter()
		{
			@Override
			public void onToggled(ThemePiece source)
			{
				menuItem.setSelected(source.isOn());
			}
		});
		return menuItem;
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

	/*private void showDataSources()
	{
		JDialog dialog =
				new HtmlViewer(frame, "Data sources", false,
						"/au/gov/ga/worldwind/data/help/datasources.html", false);
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}*/

	/*private void updateBookmarksMenu()
	{
		while (bookmarksMenu.getMenuComponentCount() > 3)
		{
			bookmarksMenu.remove(3);
		}
		for (final Bookmark bookmark : Bookmarks.iterable())
		{
			JMenuItem mi = new JMenuItem(bookmark.getName());
			bookmarksMenu.add(mi);
			mi.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					View view = wwd.getView();
					if (view instanceof OrbitView)
					{
						OrbitView orbitView = (OrbitView) view;
						Position center = orbitView.getCenterPosition();
						Position newCenter =
								Position.fromDegrees(bookmark.getLat(), bookmark.getLon(), bookmark
										.getElevation());
						long lengthMillis = Util.getScaledLengthMillis(center, newCenter);

						orbitView.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(
								orbitView, center, newCenter, orbitView.getHeading(), Angle
										.fromDegrees(bookmark.getHeading()), orbitView.getPitch(),
								Angle.fromDegrees(bookmark.getPitch()), orbitView.getZoom(),
								bookmark.getZoom(), lengthMillis, true));
						wwd.redraw();
					}
				}
			});
		}
	}*/

	private void afterSettingsChange()
	{
		/*if (Settings.get().isStereoEnabled()
				&& Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
		{
			layersPanel.turnOffAtmosphere();
		}
		map.setPickEnabled(!(Settings.get().isStereoEnabled() && Settings.get().isStereoCursor()));*/
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
		Settings.get().setSplitLocation(splitPane.getDividerLocation());
	}

	private void loadSplitLocation()
	{
		splitPane.setDividerLocation(Settings.get().getSplitLocation());
	}

	public void updateElevationUnit(String newValue)
	{
		for (Layer layer : this.wwd.getModel().getLayers())
		{
			if (layer instanceof ScalebarLayer)
			{
				if (StatusBar.UNIT_IMPERIAL.equals(newValue))
					((ScalebarLayer) layer).setUnit(ScalebarLayer.UNIT_IMPERIAL);
				else
					// Default to metric units.
					((ScalebarLayer) layer).setUnit(ScalebarLayer.UNIT_METRIC);
			}
			else if (layer instanceof TerrainProfileLayer)
			{
				if (StatusBar.UNIT_IMPERIAL.equals(newValue))
					((TerrainProfileLayer) layer).setUnit(TerrainProfileLayer.UNIT_IMPERIAL);
				else
					// Default to metric units.
					((TerrainProfileLayer) layer).setUnit(TerrainProfileLayer.UNIT_METRIC);
			}
		}

		if (StatusBar.UNIT_IMPERIAL.equals(newValue))
		{
			this.statusBar.setElevationUnit(StatusBar.UNIT_IMPERIAL);
			PlaceEditor.setUnits(PlaceEditor.IMPERIAL);
		}
		else
		{
			// Default to metric units.
			this.statusBar.setElevationUnit(StatusBar.UNIT_METRIC);
			PlaceEditor.setUnits(PlaceEditor.METRIC);
		}
	}
}
