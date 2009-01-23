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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import layers.geonames.GeoNamesLayer;
import layers.other.MetacartaCoastlineLayer;
import layers.other.MetacartaCountryBoundariesLayer;
import layers.other.MetacartaStateBoundariesLayer;

public class StandardPanel extends JPanel
{
	private Layer stars;
	private Layer atmosphere;
	private Layer fog;
	private Layer bmngone;
	private Layer bmng;
	private Layer landsat;
	private Layer pnl;
	private Layer geonames;
	private Layer coastline;
	private Layer country;
	private Layer state;
	private Layer street;
	private Layer graticule;

	private Layer[] lowerLayers;
	private Layer[] upperLayers;
	private JCheckBox atmosphereCheck;

	private WorldWindow wwd;

	public StandardPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		createLayers();
		fillPanel(lowerLayers);
		fillPanel(upperLayers);
	}

	private void createLayers()
	{
		stars = new StarsLayer();
		atmosphere = new SkyGradientLayer();
		fog = new FogLayer();
		bmngone = new BMNGOneImage();
		//bmng = new ShaderBMNGLayer();
		bmng = new BMNGWMSLayer();
		landsat = new LandsatI3WMSLayer();
		pnl = new EarthNASAPlaceNameLayer();
		geonames = new GeoNamesLayer();
		coastline = new MetacartaCoastlineLayer();
		country = new MetacartaCountryBoundariesLayer();
		state = new MetacartaStateBoundariesLayer();
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

		//Layer nightlights = new NightLightsLayer();

		lowerLayers = new Layer[] { stars, atmosphere, fog, bmngone, bmng,
				landsat/*, nightlights*/ };
		upperLayers = new Layer[] { pnl, geonames, coastline, country, state,
				street, graticule };

		coastline.setEnabled(false);
		country.setEnabled(false);
		state.setEnabled(false);
		street.setEnabled(false);
		geonames.setEnabled(false);
		graticule.setEnabled(false);

		//nightlights.setEnabled(false);
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

	private void fillPanel(Layer[] layers)
	{
		setLayout(new GridLayout(0, 1));

		for (final Layer layer : layers)
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
			add(check);

			if (layer == atmosphere)
			{
				atmosphereCheck = check;
			}
		}
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
