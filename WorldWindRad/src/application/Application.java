package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.applications.sar.SAR2;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.util.StatusBar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import layers.mouse.MouseLayer;
import nasa.worldwind.awt.stereo.WorldWindowStereoGLCanvas;
import panels.layers.LayersPanel;
import panels.other.ExaggerationPanel;
import panels.places.PlaceSearchPanel;
import settings.Settings;
import settings.SettingsDialog;
import settings.Settings.ProjectionMode;
import stereo.StereoOrbitView;
import stereo.StereoSceneController;

public class Application
{
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
		Settings.initialize("WorldWindRad"); //TODO fix node name

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
	private WorldWindowStereoGLCanvas wwd;
	private StatusBar statusBar;
	private LayersPanel layersPanel;
	private MouseLayer mouseLayer;

	public Application()
	{
		//create worldwind stuff

		wwd = new WorldWindowStereoGLCanvas();
		Model model = new BasicModel();
		wwd.setModel(model);
		wwd.addPropertyChangeListener(propertyChangeListener);
		create3DMouse();

		//create gui stuff

		frame = new JFrame("Radiometrics");
		frame.setLayout(new BorderLayout());
		frame.setMenuBar(createMenuBar());

		JPanel left = new JPanel(new BorderLayout());

		JPanel layers = new JPanel(new GridLayout(0, 1, 0, 10));
		layers.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9,
				9, 9, 9), new TitledBorder("Layers")));
		layersPanel = new LayersPanel(wwd);
		layers.add(layersPanel);
		left.add(layers, BorderLayout.CENTER);

		JPanel exaggeration = new JPanel(new GridLayout(0, 1));
		exaggeration.setBorder(new CompoundBorder(BorderFactory
				.createEmptyBorder(0, 9, 9, 9),
				new TitledBorder("Exaggeration")));
		exaggeration.add(new ExaggerationPanel(wwd));
		left.add(exaggeration, BorderLayout.SOUTH);
		
		/*JPanel gotoCoordinates = new GoToCoordinatePanel(wwd);
		left.add(gotoCoordinates, BorderLayout.PAGE_END);*/

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, left, wwd);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(8);
		frame.add(splitPane, BorderLayout.CENTER);

		Dimension minimumSize = new Dimension(100, 0);
		wwd.setMinimumSize(minimumSize);
		layers.setMinimumSize(minimumSize);

		wwd.setPreferredSize(new Dimension(600, 600));
		layers.setPreferredSize(new Dimension(220, 0));
		
		JPanel searchPanel = new JPanel(new GridLayout(0, 1));
		searchPanel.setBorder(new CompoundBorder(BorderFactory
				.createEmptyBorder(0, 9, 9, 9),
				new TitledBorder("Place search")));
		searchPanel.add(new PlaceSearchPanel(wwd));
		frame.add(searchPanel, BorderLayout.EAST);
		
		searchPanel.setPreferredSize(new Dimension(200, 0));

		statusBar = new StatusBar();
		frame.add(statusBar, BorderLayout.PAGE_END);
		statusBar.setEventSource(wwd);

		afterSettingsChange();

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				quit();
			}
		});

		frame.pack();

		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				frame.setVisible(true);
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

	private void enableMouseLayer()
	{
		mouseLayer.setEnabled(Settings.get().isStereoEnabled()
				&& Settings.get().isStereoCursor());
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

		/*menu = new Menu("View");
		menuBar.add(menu);

		menuItem = new MenuItem("Fullscreen");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFullscreen(!isFullscreen());
			}
		});*/

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
