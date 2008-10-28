package application;

import globes.GAGlobe;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.globes.Earth;

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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import panels.NASAPanel;
import panels.OtherPanel;
import panels.RadiometryPanel;
import settings.Settings;
import settings.SettingsDialog;
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
		Configuration.setValue(AVKey.GLOBE_CLASS_NAME, GAGlobe.class.getName());
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");

		Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.toString(Angle
				.fromDegreesLatitude(-27).degrees));
		Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.toString(Angle
				.fromDegreesLongitude(133.5).degrees));
		Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double
				.toString(1.2 * Earth.WGS84_EQUATORIAL_RADIUS));

		new Application();
	}

	private JFrame frame;
	private WorldWindowGLCanvas wwd;

	public Application()
	{
		//create worldwind stuff
		
		wwd = new WorldWindowGLCanvas();
		wwd.setPreferredSize(new java.awt.Dimension(800, 600));
		
		Model model = new BasicModel();
		wwd.setModel(model);
		
		//create gui stuff

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setMenuBar(createMenuBar());

		JPanel layers = new JPanel(new GridLayout(0, 1, 0, 10));
		layers.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9,
				9, 9, 9), new TitledBorder("Layers")));
		layers.add(createTabs());

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, layers, wwd);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(8);
		frame.add(splitPane, BorderLayout.CENTER);

		Dimension minimumSize = new Dimension(250, 0);
		wwd.setMinimumSize(minimumSize);
		layers.setMinimumSize(minimumSize);

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
				}
			}
		});

		return menuBar;
	}

	private JTabbedPane createTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("NASA", createNASA());
		tabbedPane.addTab("Radiometrics", createRadiometry());
		tabbedPane.addTab("Other", createOther());
		tabbedPane.doLayout();
		return tabbedPane;
	}

	private JComponent createNASA()
	{
		NASAPanel np = new NASAPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(np, BorderLayout.NORTH);
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

	public void quit()
	{
		frame.dispose();
		System.exit(0);
	}
}
