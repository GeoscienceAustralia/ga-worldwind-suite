package au.gov.ga.worldwind.panels.oldlayers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer;
import gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer;
import gov.nasa.worldwind.layers.Mercator.examples.VirtualEarthLayer;
import gov.nasa.worldwind.layers.Mercator.examples.VirtualEarthLayer.Dataset;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.application.Application;
import au.gov.ga.worldwind.layers.geonames.GeoNamesLayer;
import au.gov.ga.worldwind.layers.metacarta.MetacartaCoastlineLayer;
import au.gov.ga.worldwind.layers.metacarta.MetacartaCountryBoundariesLayer;
import au.gov.ga.worldwind.layers.metacarta.MetacartaStateBoundariesLayer;

public class StandardPanel extends JPanel
{
	private Layer stars;
	private Layer atmosphere;
	private Layer bmngone;
	private Layer bmng;
	private Layer landsat;
	private Layer veaerial;
	private Layer veroads;
	private Layer vehybrid;
	private Layer pnl;
	private Layer geonames;
	private Layer coastline;
	private Layer country;
	private Layer state;
	private Layer osmmapnik;
	private Layer osmmapniktrans;
	//private Layer street;
	private Layer latlon;

	private Layer[] lowerLayers;
	private Layer[] upperLayers;
	private JCheckBox atmosphereCheck;
	private JRadioButton noneRadio, nasaRadio, veRadio, osmRadio, aerialRadio,
			roadRadio, hybridRadio;
	private JCheckBox veCheck, bmngCheck, landsatCheck;

	private WorldWindow wwd;

	public StandardPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		createLayers();
		createPanel();
		updateImagery();
	}

	private void createLayers()
	{
		stars = new StarsLayer();
		atmosphere = new SkyGradientLayer();
		bmngone = new BMNGOneImage();
		bmng = new BMNGWMSLayer();
		landsat = new LandsatI3WMSLayer();
		if (Application.MERCATOR_VIRTUAL_EARTH)
		{
			veaerial = new VirtualEarthLayer(Dataset.AERIAL);
			veroads = new VirtualEarthLayer(Dataset.ROAD);
			vehybrid = new VirtualEarthLayer(Dataset.HYBRID);
		}
		else
		{
			veaerial = new MSVirtualEarthLayer(MSVirtualEarthLayer.LAYER_AERIAL);
			veroads = new MSVirtualEarthLayer(MSVirtualEarthLayer.LAYER_ROADS);
			vehybrid = new MSVirtualEarthLayer(MSVirtualEarthLayer.LAYER_HYBRID);
		}
		pnl = new NASAWFSPlaceNameLayer();
		geonames = new GeoNamesLayer();
		coastline = new MetacartaCoastlineLayer();
		country = new MetacartaCountryBoundariesLayer();
		state = new MetacartaStateBoundariesLayer();
		osmmapnik = new gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer();
		osmmapniktrans = new gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikTransparentLayer();
		//street = new OpenStreetMapLayer();
		latlon = new LatLonGraticuleLayer();

		/*Layer kmllayer = null;
		try
		{
			KMLFile kmlfile = KMLParser.parseFile("C:/WINNT/Profiles/u97852/Desktop/wave_amplitude_2000.0.kml");
			kmllayer = new KMLLayer(kmlfile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/

		lowerLayers = new Layer[]
		{ stars, atmosphere, bmngone, bmng, landsat, osmmapnik, veaerial, veroads,
				vehybrid };
		upperLayers = new Layer[]
		{ pnl, geonames, coastline, country, state, /*street,*/ osmmapniktrans,
				latlon };

		veaerial.setEnabled(false);
		veroads.setEnabled(false);
		vehybrid.setEnabled(false);
		coastline.setEnabled(false);
		country.setEnabled(false);
		state.setEnabled(false);
		osmmapnik.setEnabled(false);
		geonames.setEnabled(false);
		latlon.setEnabled(false);
		//street.setEnabled(false);
		osmmapniktrans.setEnabled(false);
	}

	private void createPanel()
	{
		GridBagConstraints c;
		JPanel panel, panel2;

		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateImagery();
			}
		};

		setLayout(new GridBagLayout());
		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Imagery"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		add(panel, c);
		int gridy = 0;

		ButtonGroup bg = new ButtonGroup();

		noneRadio = new JRadioButton("None");
		noneRadio.addActionListener(al);
		bg.add(noneRadio);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		panel.add(noneRadio, c);

		nasaRadio = new JRadioButton(bmngone.getName());
		nasaRadio.addActionListener(al);
		bg.add(nasaRadio);
		nasaRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(nasaRadio, c);

		panel2 = new JPanel(new GridLayout(0, 1));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(panel2, c);

		bmngCheck = new JCheckBox(bmng.getName(), bmng.isEnabled());
		bmngCheck.addActionListener(al);
		panel2.add(bmngCheck);

		landsatCheck = new JCheckBox(landsat.getName(), landsat.isEnabled());
		landsatCheck.addActionListener(al);
		panel2.add(landsatCheck);

		osmRadio = new JRadioButton(osmmapnik.getName());
		osmRadio.addActionListener(al);
		bg.add(osmRadio);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(osmRadio, c);
		
		if (Application.MERCATOR_VIRTUAL_EARTH)
		{
			veRadio = new JRadioButton("Microsoft Virtual Earth");
			veRadio.addActionListener(al);
			bg.add(veRadio);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = gridy++;
			c.anchor = GridBagConstraints.WEST;
			panel.add(veRadio, c);
		}
		else
		{
			veCheck = new JCheckBox("Microsoft Virtual Earth");
			veCheck.addActionListener(al);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = gridy++;
			c.anchor = GridBagConstraints.WEST;
			panel.add(veCheck, c); 
		}

		panel2 = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(panel2, c);

		hybridRadio = new JRadioButton("Hybrid");
		hybridRadio.setSelected(true);
		hybridRadio.addActionListener(al);
		panel2.add(hybridRadio);

		aerialRadio = new JRadioButton("Aerial");
		aerialRadio.addActionListener(al);
		panel2.add(aerialRadio);

		roadRadio = new JRadioButton("Roads");
		roadRadio.addActionListener(al);
		panel2.add(roadRadio);

		ButtonGroup vebg = new ButtonGroup();
		vebg.add(aerialRadio);
		vebg.add(roadRadio);
		vebg.add(hybridRadio);
		

		panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Boundaries"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		panel.add(createCheckBox(coastline));
		panel.add(createCheckBox(country));
		panel.add(createCheckBox(state));


		panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Effects"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		panel.add(createCheckBox(stars));
		atmosphereCheck = (JCheckBox) createCheckBox(atmosphere);
		panel.add(atmosphereCheck);


		panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Others"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		panel.add(createCheckBox(pnl));
		panel.add(createCheckBox(geonames));
		//panel.add(createCheckBox(street, true));
		panel.add(createCheckBox(osmmapniktrans, true));
		panel.add(createCheckBox(latlon));
	}

	public void addLowerLayers()
	{
		for (Layer layer : lowerLayers)
		{
			wwd.getModel().getLayers().add(layer);
		}
	}

	public void addUpperLayers()
	{
		for (Layer layer : upperLayers)
		{
			wwd.getModel().getLayers().add(layer);
		}
	}

	private void updateImagery()
	{
		bmngCheck.setEnabled(nasaRadio.isSelected());
		landsatCheck.setEnabled(nasaRadio.isSelected());

		bmngone.setEnabled(nasaRadio.isSelected());
		bmng.setEnabled(nasaRadio.isSelected() && bmngCheck.isSelected());
		landsat.setEnabled(nasaRadio.isSelected() && landsatCheck.isSelected());
		osmmapnik.setEnabled(osmRadio.isSelected());
		
		AbstractButton ve = veRadio != null ? veRadio : veCheck;

		aerialRadio.setEnabled(ve.isSelected());
		roadRadio.setEnabled(ve.isSelected());
		hybridRadio.setEnabled(ve.isSelected());

		veaerial.setEnabled(ve.isSelected() && aerialRadio.isSelected());
		veroads.setEnabled(ve.isSelected() && roadRadio.isSelected());
		vehybrid.setEnabled(ve.isSelected() && hybridRadio.isSelected());

		wwd.redraw();
	}

	private JComponent createCheckBox(final Layer layer)
	{
		return createCheckBox(layer, false);
	}

	private JComponent createCheckBox(final Layer layer, boolean opacitySlider)
	{
		final JCheckBox check = new JCheckBox(layer.getName());
		check.setSelected(layer.isEnabled());
		check.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				layer.setEnabled(check.isSelected());
				wwd.redraw();
			}
		});
		if (!opacitySlider)
			return check;

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(check, c);

		final JSlider slider = new JSlider(1, 100, 100);
		slider.setPaintLabels(false);
		slider.setPaintTicks(false);
		Dimension size = slider.getPreferredSize();
		size.width = 50;
		slider.setPreferredSize(size);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				layer.setOpacity(slider.getValue() / 100d);
				wwd.redraw();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(slider, c);
		return panel;
	}

	public void turnOffAtmosphere()
	{
		if (atmosphere != null && atmosphereCheck != null)
		{
			atmosphere.setEnabled(false);
			atmosphereCheck.setSelected(false);
			wwd.redraw();
		}
	}
}
