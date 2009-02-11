package panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.OpenStreetMapLayer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import layers.geonames.GeoNamesLayer;
import layers.mercator.VirtualEarthLayer;
import layers.mercator.VirtualEarthLayer.Dataset;
import layers.metacarta.MetacartaCoastlineLayer;
import layers.metacarta.MetacartaCountryBoundariesLayer;
import layers.metacarta.MetacartaStateBoundariesLayer;

public class StandardPanel extends JPanel
{
	private Layer stars;
	private Layer atmosphere;
	private Layer fog;
	private Layer bmngone;
	private Layer bmng;
	private Layer landsat;
	private VirtualEarthLayer veaerial;
	private VirtualEarthLayer veroad;
	private VirtualEarthLayer vehybrid;
	private Layer pnl;
	private Layer geonames;
	private Layer coastline;
	private Layer country;
	private Layer state;
	private Layer osmmapnik;
	private Layer osmmapniktrans;
	private Layer street;
	private Layer graticule;

	private Layer[] lowerLayers;
	private Layer[] upperLayers;
	private JCheckBox atmosphereCheck;
	private JRadioButton noneRadio, nasaRadio, veRadio, osmRadio, aerialRadio,
			roadRadio, hybridRadio;
	private JCheckBox bmngCheck, landsatCheck;

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
		fog = new FogLayer();
		bmngone = new BMNGOneImage();
		bmng = new BMNGWMSLayer();
		landsat = new LandsatI3WMSLayer();
		veaerial = new VirtualEarthLayer(Dataset.AERIAL);
		veroad = new VirtualEarthLayer(Dataset.ROAD);
		vehybrid = new VirtualEarthLayer(Dataset.HYBRID);
		pnl = new EarthNASAPlaceNameLayer();
		geonames = new GeoNamesLayer();
		coastline = new MetacartaCoastlineLayer();
		country = new MetacartaCountryBoundariesLayer();
		state = new MetacartaStateBoundariesLayer();
		osmmapnik = new layers.mercator.OpenStreetMapLayer();
		osmmapniktrans = new layers.mercator.OpenStreetMapTransparentLayer();
		street = new OpenStreetMapLayer();
		graticule = new MGRSGraticuleLayer();

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

		lowerLayers = new Layer[] { stars, atmosphere, fog, bmngone, bmng,
				landsat, veaerial, veroad, vehybrid, osmmapnik };
		upperLayers = new Layer[] { pnl, geonames, coastline, country, state,
				street, osmmapniktrans, graticule };

		veaerial.setEnabled(false);
		veroad.setEnabled(false);
		vehybrid.setEnabled(false);
		coastline.setEnabled(false);
		country.setEnabled(false);
		state.setEnabled(false);
		osmmapnik.setEnabled(false);
		geonames.setEnabled(false);
		graticule.setEnabled(false);
		street.setEnabled(false);
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

		noneRadio = new JRadioButton("None");
		noneRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		panel.add(noneRadio, c);

		nasaRadio = new JRadioButton(bmngone.getName());
		nasaRadio.addActionListener(al);
		nasaRadio.setSelected(true);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(nasaRadio, c);

		panel2 = new JPanel(new GridLayout(0, 1));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(panel2, c);

		bmngCheck = new JCheckBox(bmng.getName(), bmng.isEnabled());
		bmngCheck.addActionListener(al);
		panel2.add(bmngCheck);

		landsatCheck = new JCheckBox(landsat.getName(), landsat.isEnabled());
		landsatCheck.addActionListener(al);
		panel2.add(landsatCheck);

		veRadio = new JRadioButton("Microsoft Virtual Earth");
		veRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		panel.add(veRadio, c);

		panel2 = new JPanel(new GridLayout(1, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 20, 0, 0);
		panel.add(panel2, c);

		hybridRadio = new JRadioButton(vehybrid.getDataset().label);
		hybridRadio.setSelected(true);
		hybridRadio.addActionListener(al);
		panel2.add(hybridRadio);

		aerialRadio = new JRadioButton(veaerial.getDataset().label);
		aerialRadio.addActionListener(al);
		panel2.add(aerialRadio);

		roadRadio = new JRadioButton(veroad.getDataset().label);
		roadRadio.addActionListener(al);
		panel2.add(roadRadio);

		osmRadio = new JRadioButton(osmmapnik.getName());
		osmRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		panel.add(osmRadio, c);

		ButtonGroup bg = new ButtonGroup();
		bg.add(noneRadio);
		bg.add(nasaRadio);
		bg.add(veRadio);
		bg.add(osmRadio);

		bg = new ButtonGroup();
		bg.add(aerialRadio);
		bg.add(roadRadio);
		bg.add(hybridRadio);


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
		atmosphereCheck = createCheckBox(atmosphere);
		panel.add(atmosphereCheck);
		panel.add(createCheckBox(fog));


		panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createTitledBorder("Others"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		panel.add(createCheckBox(pnl));
		panel.add(createCheckBox(geonames));
		panel.add(createCheckBox(street));
		panel.add(createCheckBox(osmmapniktrans));
		panel.add(createCheckBox(graticule));
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
		aerialRadio.setEnabled(veRadio.isSelected());
		roadRadio.setEnabled(veRadio.isSelected());
		hybridRadio.setEnabled(veRadio.isSelected());

		bmngone.setEnabled(nasaRadio.isSelected());
		bmng.setEnabled(nasaRadio.isSelected() && bmngCheck.isSelected());
		landsat.setEnabled(nasaRadio.isSelected() && landsatCheck.isSelected());
		veaerial.setEnabled(veRadio.isSelected() && aerialRadio.isSelected());
		veroad.setEnabled(veRadio.isSelected() && roadRadio.isSelected());
		vehybrid.setEnabled(veRadio.isSelected() && hybridRadio.isSelected());
		osmmapnik.setEnabled(osmRadio.isSelected());
		wwd.redraw();
	}

	private JCheckBox createCheckBox(final Layer layer)
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
		return check;
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
