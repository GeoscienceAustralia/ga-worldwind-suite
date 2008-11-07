package panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import layers.geonames.GeoNamesLayer;
import layers.other.LogoLayer;

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
	private Layer compass;
	private Layer map;
	private Layer scale;
	private Layer graticule;
	private Layer logo;

	private Layer[] layers;
	private JCheckBox atmosphereCheck;

	private WorldWindow wwd;

	public StandardPanel(WorldWindow wwd)
	{
		this.wwd = wwd;
		createLayers();
		fillPanel();
	}

	private void createLayers()
	{
		stars = new StarsLayer();
		atmosphere = new SkyGradientLayer();
		fog = new FogLayer();
		bmngone = new BMNGOneImage();
		bmng = new BMNGWMSLayer();
		landsat = new LandsatI3WMSLayer();
		pnl = new EarthNASAPlaceNameLayer();
		geonames = new GeoNamesLayer();
		compass = new CompassLayer();
		map = new WorldMapLayer();
		scale = new ScalebarLayer();
		graticule = new MGRSGraticuleLayer();
		logo = new LogoLayer();

		layers = new Layer[] { stars, atmosphere, fog, bmngone, bmng, landsat,
				pnl, geonames, graticule, compass, map, scale, logo };
		for (Layer layer : layers)
		{
			layer.setEnabled(true);
			wwd.getModel().getLayers().add(layer);
		}

		geonames.setEnabled(false);
		graticule.setEnabled(false);
	}

	private void fillPanel()
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
	
	public void setMapPickingEnabled(boolean enabled)
	{
		map.setPickEnabled(enabled);
	}
}
