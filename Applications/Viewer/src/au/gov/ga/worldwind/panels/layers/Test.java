package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.ClickAndGoSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import au.gov.ga.worldwind.panels.SideBar;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.Settings.ProxyType;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemeFactory;

public class Test extends JPanel
{
	public static void main(String[] args) throws Exception
	{
		Settings.get().setProxyEnabled(true);
		Settings.get().setProxyHost("proxy.agso.gov.au");
		Settings.get().setProxyPort(8080);
		Settings.get().setProxyType(ProxyType.HTTP);
		Settings.save();

		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final JFrame frame = new JFrame("Demo");
		frame.setLayout(new BorderLayout());

		WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
		Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		wwd.setModel(m);
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));
		frame.add(wwd, BorderLayout.CENTER);

		m.getLayers().clear();
		m.getGlobe().setElevationModel(new ExtendedCompoundElevationModel());


		final Test test = new Test(wwd);
		frame.add(test, BorderLayout.WEST);
		test.setPreferredSize(new Dimension(300, 200));


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 1024);
		frame.setLocation(100, 100);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				test.theme.dispose();
				frame.dispose();
			}
		});
	}

	private Theme theme;

	public Test(WorldWindow wwd) throws Exception
	{
		super(new BorderLayout());

		InputStream is =
				Test.class.getResourceAsStream("/config/DefaultTheme.xml");
		theme = ThemeFactory.createFromXML(is, null);
		theme.setup(wwd);
		SideBar sidebar = new SideBar(theme);
		add(sidebar, BorderLayout.CENTER);
	}
}
