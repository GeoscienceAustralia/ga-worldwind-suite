package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.applications.sar.SAR2;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.ClickAndGoSelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.view.OrbitView;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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

import layers.ga.GALayer;
import layers.mouse.MouseLayer;
import layers.shader.NormalTessellator;
import nasa.worldwind.awt.AWTInputHandler;
import nasa.worldwind.awt.stereo.WorldWindowStereoGLCanvas;
import nasa.worldwind.cache.FixedBasicDataFileCache;
import nasa.worldwind.view.FlyToOrbitViewStateIterator;
import panels.layers.LayersPanel;
import panels.other.GoToCoordinatePanel;
import panels.places.PlaceSearchPanel;
import retrieve.ExtendedRetrievalService;
import settings.Settings;
import settings.SettingsDialog;
import settings.Settings.ProjectionMode;
import stereo.StereoOrbitView;
import stereo.StereoSceneController;
import util.DoubleClickZoomListener;
import util.HtmlViewer;
import util.JVisibleDialog;
import util.Util;
import annotations.AnnotationEditor;
import annotations.AnnotationsPanel;
import bookmarks.Bookmark;
import bookmarks.BookmarkListener;
import bookmarks.BookmarkManager;
import bookmarks.Bookmarks;

public class Application
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
	}

	public static void main(String[] args)
	{
		Settings.get();

		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME,
				StereoSceneController.class.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class
				.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.DATA_FILE_CACHE_CLASS_NAME,
				FixedBasicDataFileCache.class.getName());

		Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.toString(Angle
				.fromDegreesLatitude(-27).degrees));
		Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.toString(Angle
				.fromDegreesLongitude(133.5).degrees));
		/*Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double
				.toString(1.2 * Earth.WGS84_EQUATORIAL_RADIUS));*/

		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME,
				NormalTessellator.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME,
				ExtendedRetrievalService.class.getName());
		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME,
				AWTInputHandler.class.getName());

		WorldWind.getDataFileCache().addCacheLocation("cache");

		new Application();
	}

	private JFrame frame;
	private JFrame fullscreenFrame;
	private GraphicsDevice fullscreenDevice;
	private WorldWindowStereoGLCanvas wwd;
	private StatusBar statusBar;
	private LayersPanel layersPanel;
	private MouseLayer mouseLayer;
	private JSplitPane splitPane;
	private JMenu bookmarksMenu;

	private WorldMapLayer map;
	private Layer logo, scalebar, compass;

	private JVisibleDialog annotationsDialog;
	private AnnotationsPanel annotationsPanel;
	private JVisibleDialog placesearchDialog;
	private List<JVisibleDialog> dialogs = new ArrayList<JVisibleDialog>();

	public Application()
	{
		//create worldwind stuff

		wwd = new WorldWindowStereoGLCanvas();
		Model model = new BasicModel();
		wwd.setModel(model);
		wwd.addPropertyChangeListener(propertyChangeListener);
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd,
				WorldMapLayer.class));
		create3DMouse();
		createDoubleClickListener();

		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ExtendedRetrievalService)
		{
			((ExtendedRetrievalService) rs).addLayer(wwd);
		}

		map = new WorldMapLayer();
		scalebar = new ScalebarLayer();
		compass = new CompassLayer();
		logo = GALayer.getLogoLayer();
		model.getLayers().add(map);
		model.getLayers().add(scalebar);
		model.getLayers().add(compass);
		model.getLayers().add(logo);

		//create gui stuff

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame("Geoscience Australia – World Wind");
		frame.setLayout(new BorderLayout());
		frame.setBounds(Settings.get().getWindowBounds());
		if (Settings.get().isWindowMaximized())
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		//panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		panel.add(splitPane, BorderLayout.CENTER);
		splitPane.setRightComponent(wwd);
		splitPane.setOneTouchExpandable(true);
		wwd.setMinimumSize(new Dimension(1, 1));

		statusBar = new StatusBar();
		panel.add(statusBar, BorderLayout.PAGE_END);
		statusBar.setEventSource(wwd);
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

		layersPanel = new LayersPanel(wwd, frame);
		splitPane.setLeftComponent(layersPanel);

		loadSplitLocations();
		afterSettingsChange();

		//create dialogs
		createDialogs();


		frame.setJMenuBar(createMenuBar());
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

	private void createDialogs()
	{
		annotationsDialog = createDialog("Annotations");
		annotationsPanel = new AnnotationsPanel(wwd, frame);
		annotationsDialog.add(annotationsPanel, BorderLayout.CENTER);
		annotationsDialog.setJMenuBar(createAnnotationsMenuBar());
		dialogs.add(annotationsDialog);

		placesearchDialog = createDialog("Place search");
		placesearchDialog.add(new PlaceSearchPanel(wwd), BorderLayout.CENTER);
		dialogs.add(placesearchDialog);

		loadDialogBounds();
	}

	private JMenuBar createAnnotationsMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menuBar.add(menu);

		final JFileChooser chooser = new JFileChooser();
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
				return ext.toLowerCase().equals("xml");
			}

			@Override
			public String getDescription()
			{
				return "XML files (*.xml)";
			}
		};
		chooser.setFileFilter(filter);

		menuItem = new JMenuItem("Import...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				chooser.setMultiSelectionEnabled(true);
				if (chooser.showOpenDialog(annotationsDialog) == JFileChooser.APPROVE_OPTION)
				{
					for (File file : chooser.getSelectedFiles())
					{
						try
						{
							annotationsPanel.importAnnotations(file);
						}
						catch (Exception e)
						{
							JOptionPane.showMessageDialog(annotationsDialog,
									"Could not import " + file.getName(),
									"Import error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				chooser.setMultiSelectionEnabled(false);
			}
		});

		menuItem = new JMenuItem("Export...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if (chooser.showSaveDialog(annotationsDialog) == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					if (!file.getName().toLowerCase().endsWith(".xml"))
					{
						file = new File(file.getAbsolutePath() + ".xml");
						if (file.exists())
						{
							int answer = JOptionPane
									.showConfirmDialog(
											annotationsDialog,
											file.getAbsolutePath()
													+ " already exists.\nDo you want to replace it?",
											"Export",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE);
							if (answer != JOptionPane.YES_OPTION)
								file = null;
						}
					}
					if (file != null)
					{
						try
						{
							annotationsPanel.exportAnnotations(file);
						}
						catch (Exception e)
						{
							JOptionPane.showMessageDialog(annotationsDialog,
									"Error: " + e, "Export error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Close");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				annotationsDialog.setVisible(false);
			}
		});

		return menuBar;
	}

	private void saveImage()
	{
		String[] formats = ImageIO.getWriterFormatNames();
		final Set<String> imageFormats = new HashSet<String>();
		for (String format : formats)
		{
			imageFormats.add(format.toUpperCase());
		}

		JFileChooser chooser = new JFileChooser();
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
				return imageFormats.contains(ext.toUpperCase());
			}

			@Override
			public String getDescription()
			{
				String desc = "";
				int i = 0;
				for (String format : imageFormats)
				{
					desc += format;
					if (i++ < imageFormats.size())
						desc += ",";
					desc += " ";
				}
				desc += " images";
				return desc;
			}
		};
		chooser.setFileFilter(filter);
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			Screenshotter.takeScreenshot(wwd, wwd, chooser.getSelectedFile());
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
		Double initAltitude = Configuration
				.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		Double initHeading = Configuration
				.getDoubleValue(AVKey.INITIAL_HEADING);
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

		Position endCenter = Position.fromDegrees(initLat, initLon, beginCenter
				.getElevation());
		long lengthMillis = Util.getScaledLengthMillis(beginCenter.getLatLon(),
				endCenter.getLatLon());

		ViewStateIterator vsi = FlyToOrbitViewStateIterator
				.createPanToIterator(wwd.getModel().getGlobe(), beginCenter,
						endCenter, view.getHeading(), Angle
								.fromDegrees(initHeading), view.getPitch(),
						Angle.fromDegrees(initPitch), view.getZoom(),
						initAltitude, lengthMillis, true);
		wwd.getView().applyStateIterator(vsi);
	}

	private void create3DMouse()
	{
		final UserFacingIcon icon = new UserFacingIcon(
				"data/images/cursor.png", new Position(Angle.ZERO, Angle.ZERO,
						0));
		icon.setSize(new Dimension(16, 32));
		icon.setAlwaysOnTop(true);

		LayerList layers = wwd.getModel().getLayers();
		mouseLayer = new MouseLayer(wwd, icon);
		layers.add(mouseLayer);

		enableMouseLayer();
	}

	private void createDoubleClickListener()
	{
		wwd.addMouseListener(new DoubleClickZoomListener(wwd, 5000d));
	}

	private void enableMouseLayer()
	{
		mouseLayer.setEnabled(Settings.get().isStereoEnabled()
				&& Settings.get().isStereoCursor());
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

				GraphicsEnvironment ge = GraphicsEnvironment
						.getLocalGraphicsEnvironment();
				GraphicsDevice[] gds = ge.getScreenDevices();
				fullscreenDevice = ge.getDefaultScreenDevice();

				if (!fullscreenDevice.isFullScreenSupported())
				{
					JOptionPane
							.showMessageDialog(
									frame,
									"Graphics device does not support fullscreen mode.",
									"Not supported", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					saveSplitLocations();
					fullscreenFrame = new JFrame(frame.getTitle());
					JPanel panel = new JPanel(new BorderLayout());
					fullscreenFrame.setContentPane(panel);
					fullscreenFrame.setUndecorated(true);
					fullscreenFrame.add(wwd);
					fullscreenDevice.setFullScreenWindow(fullscreenFrame);

					fullscreenFrame
							.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
							KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
							action);
					panel.getActionMap().put(action, action);

					if (span)
					{
						Rectangle fullBounds = new Rectangle();
						for (GraphicsDevice g : gds)
						{
							GraphicsConfiguration gc = g
									.getDefaultConfiguration();
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
								GraphicsConfiguration gc = g
										.getDefaultConfiguration();
								fullscreenFrame.setBounds(gc.getBounds());
								break;
							}
						}
					}
					fullscreenFrame.setVisible(true);
					frame.setVisible(false);
				}
			}
			else
			{
				if (fullscreenFrame != null)
				{
					splitPane.setRightComponent(wwd);
					fullscreenDevice.setFullScreenWindow(null);
					fullscreenFrame.dispose();
					fullscreenFrame = null;
					loadSplitLocations();
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

		final JCheckBoxMenuItem offline = new JCheckBoxMenuItem("Work offline");
		menu.add(offline);
		offline.setSelected(WorldWind.isOfflineMode());
		offline.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				WorldWind.setOfflineMode(offline.isSelected());
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("Save image");
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveImage();
			}
		});
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Exit");
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

		menuItem = new JMenuItem("Default view");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetView();
			}
		});

		menuItem = new JMenuItem("Go to coordinates...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				GoToCoordinatePanel.showGotoDialog(frame, wwd,
						"Go to coordinates");
			}
		});

		menu.addSeparator();

		for (JVisibleDialog dialog : dialogs)
		{
			menuItem = createDialogMenuItem(dialog);
			menu.add(menuItem);
		}

		menu.addSeparator();

		menuItem = createLayerMenuItem(map);
		menu.add(menuItem);

		menuItem = createLayerMenuItem(compass);
		menu.add(menuItem);

		menuItem = createLayerMenuItem(scalebar);
		menu.add(menuItem);

		menuItem = createLayerMenuItem(logo);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Fullscreen");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});

		bookmarksMenu = new JMenu("Bookmarks");
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
				BookmarkManager bm = new BookmarkManager(frame,
						"Organise bookmarks");
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
		bl.modified();

		menu = new JMenu("Options");
		menuBar.add(menu);

		menuItem = new JMenuItem("Preferences...");
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

		menuItem = new JMenuItem("Controls...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showControls();
			}
		});

		menu.addSeparator();

		menuItem = new JMenuItem("About");
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

	private JMenuItem createLayerMenuItem(final Layer layer)
	{
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(layer
				.getName(), layer.isEnabled());
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				layer.setEnabled(menuItem.isSelected());
				wwd.redraw();
			}
		});
		return menuItem;
	}

	private JMenuItem createDialogMenuItem(final JVisibleDialog dialog)
	{
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dialog
				.getTitle()
				+ " dialog", dialog.isVisible());
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.setVisible(menuItem.isSelected());
			}
		});
		dialog.addVisibilityListener(new JVisibleDialog.VisibilityListener()
		{
			public void visibleChanged(boolean visible)
			{
				menuItem.setSelected(visible);
			}
		});
		return menuItem;
	}

	private void showControls()
	{
		JDialog dialog = new HtmlViewer(frame, "Controls", false,
				"/data/help/controls.html", true);
		dialog.setResizable(false);
		dialog.setSize(640, 480);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private void updateBookmarksMenu()
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
						Position newCenter = Position.fromDegrees(bookmark
								.getLat(), bookmark.getLon(), bookmark
								.getElevation());
						long lengthMillis = Util.getScaledLengthMillis(center
								.getLatLon(), newCenter.getLatLon());

						ViewStateIterator vsi = FlyToOrbitViewStateIterator
								.createPanToIterator(wwd.getModel().getGlobe(),
										center, newCenter, orbitView
												.getHeading(), Angle
												.fromDegrees(bookmark
														.getHeading()),
										orbitView.getPitch(), Angle
												.fromDegrees(bookmark
														.getPitch()), orbitView
												.getZoom(), bookmark.getZoom(),
										lengthMillis, true);

						view.applyStateIterator(vsi);
					}
				}
			});
		}
	}

	private void afterSettingsChange()
	{
		if (Settings.get().isStereoEnabled()
				&& Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
		{
			layersPanel.turnOffAtmosphere();
		}
		map.setPickEnabled(!(Settings.get().isStereoEnabled() && Settings.get()
				.isStereoCursor()));
		enableMouseLayer();
	}

	public void quit()
	{
		saveSplitLocations();
		saveDialogBounds();
		Settings.save();
		frame.dispose();
		System.exit(0);
	}

	private void saveSplitLocations()
	{
		int[] splits = new int[1];
		splits[0] = splitPane.getDividerLocation();
		Settings.get().setSplitLocations(splits);
	}

	private void loadSplitLocations()
	{
		int[] splits = Settings.get().getSplitLocations();
		if (splits != null && splits.length == 1)
		{
			if (splits[0] >= 0)
				splitPane.setDividerLocation(splits[0]);
		}
	}

	private void loadDialogBounds()
	{
		boolean[] dialogsOpen = Settings.get().getDialogsOpen();
		Rectangle[] dialogBounds = Settings.get().getDialogBounds();
		boolean useOpen = dialogsOpen != null
				&& dialogsOpen.length == dialogs.size();
		boolean useBounds = dialogBounds != null
				&& dialogBounds.length == dialogs.size();
		for (int i = 0; i < dialogs.size(); i++)
		{
			JVisibleDialog dialog = dialogs.get(i);
			if (useBounds && useOpen && dialogsOpen[i])
			{
				dialog.setBounds(dialogBounds[i]);
				dialog.setVisible(dialogsOpen[i]);
			}
			else
			{
				dialog.pack();
				dialog.centerInOwnerWhenShown();
			}
		}
	}

	private void saveDialogBounds()
	{
		boolean[] dialogsOpen = new boolean[dialogs.size()];
		Rectangle[] dialogBounds = new Rectangle[dialogs.size()];
		for (int i = 0; i < dialogs.size(); i++)
		{
			JVisibleDialog dialog = dialogs.get(i);
			dialogsOpen[i] = dialog.isVisible();
			dialogBounds[i] = dialog.getBounds();
		}
		Settings.get().setDialogsOpen(dialogsOpen);
		Settings.get().setDialogBounds(dialogBounds);
	}

	private JVisibleDialog createDialog(String title)
	{
		final JVisibleDialog dialog = new JVisibleDialog(frame, title);
		dialog.setLayout(new BorderLayout());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dialog.setVisible(false);
			}
		});
		return dialog;
	}

	private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
	{
		public void propertyChange(PropertyChangeEvent propertyChangeEvent)
		{
			if (propertyChangeEvent.getPropertyName() == SAR2.ELEVATION_UNIT)
				updateElevationUnit(propertyChangeEvent.getNewValue());
		}
	};

	private void updateElevationUnit(Object newValue)
	{
		for (Layer layer : this.wwd.getModel().getLayers())
		{
			if (layer instanceof ScalebarLayer)
			{
				if (SAR2.UNIT_IMPERIAL.equals(newValue))
					((ScalebarLayer) layer)
							.setUnit(ScalebarLayer.UNIT_IMPERIAL);
				else
					// Default to metric units.
					((ScalebarLayer) layer).setUnit(ScalebarLayer.UNIT_METRIC);
			}
			else if (layer instanceof TerrainProfileLayer)
			{
				if (SAR2.UNIT_IMPERIAL.equals(newValue))
					((TerrainProfileLayer) layer)
							.setUnit(TerrainProfileLayer.UNIT_IMPERIAL);
				else
					// Default to metric units.
					((TerrainProfileLayer) layer)
							.setUnit(TerrainProfileLayer.UNIT_METRIC);
			}
		}

		if (SAR2.UNIT_IMPERIAL.equals(newValue))
		{
			this.statusBar.setElevationUnit(StatusBar.UNIT_IMPERIAL);
			AnnotationEditor.setUnits(AnnotationEditor.IMPERIAL);
		}
		else
		{
			// Default to metric units.
			this.statusBar.setElevationUnit(StatusBar.UNIT_METRIC);
			AnnotationEditor.setUnits(AnnotationEditor.METRIC);
		}
	}
}
