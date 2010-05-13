package au.gov.ga.worldwind.panels.layers;

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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import au.gov.ga.worldwind.components.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.components.collapsiblesplit.l2fprod.CollapsibleGroup;
import au.gov.ga.worldwind.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.panels.dataset.LazyDataset;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.Settings.ProxyType;
import au.gov.ga.worldwind.util.Icons;

public class Test extends JPanel
{
	public static void main(String[] args) throws Exception
	{
		Settings.get().setProxyEnabled(true);
		Settings.get().setProxyHost("proxy.agso.gov.au");
		Settings.get().setProxyPort(8080);
		Settings.get().setProxyType(ProxyType.HTTP);
		Settings.save();

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final JFrame frame = new JFrame("Demo");
		frame.setLayout(new BorderLayout());

		WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
		Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		wwd.setModel(m);
		wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));
		frame.add(wwd, BorderLayout.CENTER);


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
				test.layersPanel.dispose();
				frame.dispose();
			}
		});
	}

	private LayersPanel layersPanel;

	public Test(WorldWindow wwd) throws Exception
	{
		final CollapsibleSplitPane pane = new CollapsibleSplitPane();
		pane.getLayout().addPlaceholder("panel0", 1);
		pane.getLayout().addPlaceholder("panel1", 1);
		pane.getLayout().addPlaceholder("panel2", 1);
		pane.getLayout().setVertical(true);
		pane.getLayout().setDividerSize(5);

		CollapsibleGroup layersGroup = new CollapsibleGroup();
		layersGroup.setTitle("Layers");
		layersGroup.setScrollOnExpand(true);
		pane.add(layersGroup, "panel0");
		layersGroup.setLayout(new BorderLayout());

		CollapsibleGroup datasetsGroup = new CollapsibleGroup();
		datasetsGroup.setTitle("Datasets");
		datasetsGroup.setScrollOnExpand(true);
		pane.add(datasetsGroup, "panel1");
		datasetsGroup.setLayout(new BorderLayout());

		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);


		layersPanel = new LayersPanel(wwd);
		layersGroup.add(layersPanel, BorderLayout.CENTER);

		DatasetPanel datasetPanel = new DatasetPanel();
		datasetsGroup.add(datasetPanel, BorderLayout.CENTER);

		layersPanel.linkWithDatasetPanel(datasetPanel);
		layersPanel.setupDrag();


		URL url = null;
		try
		{
			url = new URL("file:bin/au/gov/ga/worldwind/panels/dataset/test/dataset.xml");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		LazyDataset dataset = new LazyDataset("Datasets", url, null, Icons.earth.getURL());
		datasetPanel.addDataset(dataset);
	}
}
