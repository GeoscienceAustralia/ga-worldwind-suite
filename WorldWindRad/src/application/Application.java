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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import layers.mouse.MouseLayer;
import nasa.worldwind.awt.stereo.WorldWindowStereoGLCanvas;
import panels.OtherPanel;
import panels.RadiometryPanel;
import panels.StandardPanel;
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
	private StandardPanel standardPanel;
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
		layers.add(createTabs());
		left.add(layers, BorderLayout.CENTER);

		JPanel exaggeration = new JPanel(new GridLayout(0, 1));
		exaggeration.setBorder(new CompoundBorder(BorderFactory
				.createEmptyBorder(0, 9, 9, 9),
				new TitledBorder("Exaggeration")));
		exaggeration.add(createExaggeration());
		left.add(exaggeration, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, left, wwd);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(8);
		frame.add(splitPane, BorderLayout.CENTER);

		Dimension minimumSize = new Dimension(100, 0);
		wwd.setMinimumSize(minimumSize);
		layers.setMinimumSize(minimumSize);

		wwd.setPreferredSize(new Dimension(800, 600));
		layers.setPreferredSize(new Dimension(220, 0));

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
			standardPanel.turnOffAtmosphere();
		}
		enableMouseLayer();
	}

	private JTabbedPane createTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Standard", createStandard());
		tabbedPane.addTab("Radiometrics", createRadiometry());
		tabbedPane.addTab("Other", createOther());
		tabbedPane.doLayout();
		return tabbedPane;
	}

	private JComponent createStandard()
	{
		standardPanel = new StandardPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(standardPanel, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createRadiometry()
	{
		RadiometryPanel rp = new RadiometryPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(rp, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createOther()
	{
		OtherPanel op = new OtherPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(op, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	//logarithmic vertical exaggeration slider

	private int exaggerationToSlider(double exaggeration)
	{
		double y = exaggeration;
		double x = Math.log10(y + (100 - y) / 100);
		return (int) (x * 100);
	}

	private double sliderToExaggeration(int slider)
	{
		double x = slider / 100d;
		double y = Math.pow(10d, x) - (2 - x) / 2;
		return y;
	}

	private JComponent createExaggeration()
	{
		GridBagConstraints c;
		JPanel panel = new JPanel(new GridBagLayout());

		final JSlider slider = new JSlider(0, 200,
				exaggerationToSlider(Settings.get().getVerticalExaggeration()));
		Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		panel.add(slider, c);

		final JLabel label = new JLabel();
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		panel.add(label, c);

		class Setter
		{
			public void set(double exaggeration)
			{
				label.setText(String
						.valueOf(Math.round(exaggeration * 10d) / 10d)
						+ " " + slider.getValue());
				Settings.get().setVerticalExaggeration(exaggeration);
				wwd.getSceneController().setVerticalExaggeration(exaggeration);
				wwd.redraw();
			}
		}
		final Setter setter = new Setter();

		final ChangeListener listener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				double exaggeration = sliderToExaggeration(slider.getValue());
				setter.set(exaggeration);
			}
		};
		slider.addChangeListener(listener);
		listener.stateChanged(null);

		JPanel buttons = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		panel.add(buttons, c);

		class ScaleListener implements ActionListener
		{
			private double exaggeration;

			public ScaleListener(double exaggeration)
			{
				this.exaggeration = exaggeration;
			}

			public void actionPerformed(ActionEvent e)
			{
				slider.setValue(exaggerationToSlider(exaggeration));
				setter.set(exaggeration);
			}
		}

		JButton button = new JButton("1:1");
		button.addActionListener(new ScaleListener(2d));
		size = button.getMinimumSize();
		size.width = 0;
		button.setMinimumSize(size);
		buttons.add(button);
		button = new JButton("2:1");
		button.addActionListener(new ScaleListener(2d));
		button.setMinimumSize(size);
		buttons.add(button);
		button = new JButton("10:1");
		button.addActionListener(new ScaleListener(10d));
		button.setMinimumSize(size);
		buttons.add(button);
		/*button = new JButton("100:1");
		button.addActionListener(new ScaleListener(100d));
		button.setMinimumSize(size);
		buttons.add(button);*/

		/*final JCheckBox useTerrain = new JCheckBox("Use GA terrain");
		useTerrain.setSelected(Settings.get().isUseTerrain());
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		panel.add(useTerrain, c);

		useTerrain.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.get().setUseTerrain(useTerrain.isSelected());
				LayerList layers = wwd.getModel().getLayers();
				Model model = new BasicModel();
				model.setLayers(layers);
				wwd.setModel(model);
				listener.stateChanged(null);
				listener.stateChanged(null);
			}
		});*/

		return panel;
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
