package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.applications.sar.SAR2;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import layers.mouse.MouseLayer;
import nasa.worldwind.awt.stereo.WorldWindowStereoGLCanvas;

import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.perspective.PerspectiveManager;

import panels.layers.LayersPanel;
import panels.other.ExaggerationPanel;
import panels.other.GoToCoordinatePanel;
import panels.places.PlaceSearchPanel;
import settings.Settings;
import settings.SettingsDialog;
import settings.Settings.ProjectionMode;
import stereo.StereoOrbitView;
import stereo.StereoSceneController;
import util.DockingAdapter;
import util.EyePositionViewStateIterator;
import util.Icons;

public class Application
{
	private final static String SETTINGS_KEY = "WorldWindRad";

	static
	{
		if (Configuration.isWindowsOS())
		{
			System.setProperty("sun.java2d.noddraw", "true");
		}
		else if (Configuration.isMacOS())
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
		Settings.initialize(SETTINGS_KEY);

		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME,
				StereoSceneController.class.getName());
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, StereoOrbitView.class
				.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");

		Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.toString(Angle
				.fromDegreesLatitude(-27).degrees));
		Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.toString(Angle
				.fromDegreesLongitude(133.5).degrees));
		/*Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double
				.toString(1.2 * Earth.WGS84_EQUATORIAL_RADIUS));*/

		new Application();
	}

	private JFrame frame;
	private JFrame fullscreenFrame;
	private GraphicsDevice fullscreenDevice;
	private WorldWindowStereoGLCanvas wwd;
	private StatusBar statusBar;
	private LayersPanel layersPanel;
	private MouseLayer mouseLayer;

	private DefaultDockingPort dockingPort;
	private DockablePanel globeDockable;
	private DockablePanel layersDockable;
	private DockablePanel exaggerationDockable;
	private DockablePanel gotoDockable;
	private DockablePanel placeSearchDockable;

	public Application()
	{
		//create worldwind stuff

		wwd = new WorldWindowStereoGLCanvas();
		wwd.setMinimumSize(new Dimension(10, 10));
		Model model = new BasicModel();
		wwd.setModel(model);
		wwd.addPropertyChangeListener(propertyChangeListener);
		create3DMouse();
		createDoubleClickListener();

		//create gui stuff

		System.setProperty(DockingConstants.HEAVYWEIGHT_DOCKABLES, "true");
		DockingManager.setDragPreview(new AlphaPreview(Color.black, new Color(
				SystemColor.activeCaption.getRGB()), 0.5f));

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame("Radiometrics");
		frame.setLayout(new BorderLayout());
		frame.setBounds(Settings.get().getWindowBounds());
		if (Settings.get().isWindowMaximized())
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		//panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		statusBar = new StatusBar();
		panel.add(statusBar, BorderLayout.PAGE_END);
		statusBar.setEventSource(wwd);
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

		dockingPort = new DefaultDockingPort();
		panel.add(dockingPort, BorderLayout.CENTER);

		globeDockable = new DockablePanel("globe", "Globe", null, wwd, false,
				false);
		globeDockable.addMaximiseButton();
		JButton fullscreen = new JButton(Icons.monitor);
		fullscreen.setToolTipText("Fullscreen");
		fullscreen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});
		globeDockable.addButton(fullscreen);

		layersPanel = new LayersPanel(wwd);
		layersDockable = new DockablePanel("layers", "Layers", null,
				layersPanel, true, true);

		panel = new JPanel(new BorderLayout());
		ExaggerationPanel exaggerationPanel = new ExaggerationPanel(wwd);
		panel.add(exaggerationPanel, BorderLayout.NORTH);
		exaggerationDockable = new DockablePanel("exaggeration",
				"Exaggeration", null, panel, true, true);

		PlaceSearchPanel placeSearchPanel = new PlaceSearchPanel(wwd);
		placeSearchDockable = new DockablePanel("placesearch", "Place search",
				null, placeSearchPanel, true, true);

		panel = new JPanel(new BorderLayout());
		GoToCoordinatePanel gotoPanel = new GoToCoordinatePanel(wwd);
		panel.add(gotoPanel, BorderLayout.NORTH);
		gotoDockable = new DockablePanel("gotocoordinate", "Go to coordinates",
				null, panel, true, true);

		PerspectiveManager perspectiveManager = PerspectiveManager
				.getInstance();
		perspectiveManager.setDefaultPersistenceKey(SETTINGS_KEY);
		DockingManager.setLayoutManager(perspectiveManager);
		boolean loaded = false;
		try
		{
			loaded = DockingManager.loadLayoutModel(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (!loaded)
		{
			dockingPort.dock(globeDockable, DockingConstants.CENTER_REGION);
			globeDockable.dock(layersDockable, DockingConstants.WEST_REGION);
			layersDockable.dock(exaggerationDockable,
					DockingConstants.SOUTH_REGION);
			globeDockable.dock(placeSearchDockable,
					DockingConstants.EAST_REGION);

			DockingManager.setSplitProportion((DockingPort) dockingPort, 0.25f);
			DockingManager.setSplitProportion(placeSearchDockable, 0.8f);
			DockingManager.setSplitProportion(exaggerationDockable, 0.8f);
		}

		afterSettingsChange();

		frame.setMenuBar(createMenuBar());
		addWindowListeners();

		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				frame.setVisible(true);
			}
		});
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
		wwd.addMouseListener(new MouseAdapter()
		{
			private LatLon latlon;

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (!(wwd.getView() instanceof OrbitView))
					return;
				OrbitView view = (OrbitView) wwd.getView();

				if (e.getClickCount() % 2 == 1)
				{
					//single click
					latlon = null;

					PickedObjectList pickedObjects = wwd
							.getObjectsAtCurrentPosition();
					if (pickedObjects == null)
						return;

					PickedObject top = pickedObjects.getTopPickedObject();
					if (top == null || !top.isTerrain())
						return;

					latlon = top.getPosition().getLatLon();
				}
				else
				{
					//double click
					Position eyePosition = view.getEyePosition();
					if (eyePosition.getElevation() > 5000 && latlon != null)
					{
						ViewStateIterator vsi = EyePositionViewStateIterator
								.createIterator(eyePosition,
										new Position(latlon, eyePosition
												.getElevation() / 2), 1000,
										true);
						view.applyStateIterator(vsi);
						latlon = null;
					}
				}
			}
		});
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
						frame.setBounds(fullBounds);
					}
					else if (id != null)
					{
						for (GraphicsDevice g : gds)
						{
							if (id.equals(g.getIDstring()))
							{
								GraphicsConfiguration gc = g
										.getDefaultConfiguration();
								frame.setBounds(gc.getBounds());
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
					globeDockable.setComponent(wwd);
					fullscreenDevice.setFullScreenWindow(null);
					fullscreenFrame.dispose();
					fullscreenFrame = null;
					frame.setVisible(true);
				}
			}
		}
	}

	private MenuBar createMenuBar()
	{
		MenuBar menuBar = new MenuBar();

		Menu menu;
		MenuItem menuItem;

		menu = new Menu("File");
		menuBar.add(menu);

		menuItem = new MenuItem("Exit");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quit();
			}
		});

		menu = new Menu("View");
		menuBar.add(menu);

		menuItem = createDockableMenuItem(layersDockable);
		menu.add(menuItem);

		menuItem = createDockableMenuItem(exaggerationDockable);
		menu.add(menuItem);

		menuItem = createDockableMenuItem(placeSearchDockable);
		menu.add(menuItem);

		menuItem = createDockableMenuItem(gotoDockable);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new MenuItem("Fullscreen");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});

		menu = new Menu("Options");
		menuBar.add(menu);

		menuItem = new MenuItem("Preferences...");
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

		return menuBar;
	}

	private MenuItem createDockableMenuItem(final DockablePanel dockablePanel)
	{
		final CheckboxMenuItem menuItem = new CheckboxMenuItem(dockablePanel
				.getTitle(), DockingManager.isDocked(dockablePanel));
		dockablePanel.addDockingListener(new DockingAdapter()
		{
			public void dockingComplete(DockingEvent evt)
			{
				menuItem.setState(DockingManager.isDocked(dockablePanel));
			}

			public void undockingComplete(DockingEvent evt)
			{
				menuItem.setState(DockingManager.isDocked(dockablePanel));
			}
		});
		menuItem.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (DockingManager.isDocked(dockablePanel))
				{
					DockingManager.close(dockablePanel);
				}
				else
				{
					DockingManager.display(dockablePanel);
				}
			}
		});
		return menuItem;
	}

	private void afterSettingsChange()
	{
		if (Settings.get().isStereoEnabled()
				&& Settings.get().getProjectionMode() == ProjectionMode.ASYMMETRIC_FRUSTUM)
		{
			layersPanel.turnOffAtmosphere();
		}
		layersPanel
				.setMapPickingEnabled(!(Settings.get().isStereoEnabled() && Settings
						.get().isStereoCursor()));
		enableMouseLayer();
	}

	public void quit()
	{
		try
		{
			DockingManager.storeLayoutModel();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		frame.dispose();
		System.exit(0);
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
			this.statusBar.setElevationUnit(StatusBar.UNIT_IMPERIAL);
		else
			// Default to metric units.
			this.statusBar.setElevationUnit(StatusBar.UNIT_METRIC);
	}
}
