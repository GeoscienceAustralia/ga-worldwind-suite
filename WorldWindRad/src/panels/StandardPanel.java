package panels;

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

	private Layer[] layers;

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

		layers = new Layer[] { stars, atmosphere, fog, bmngone, bmng, landsat,
				pnl, geonames, compass, map, scale, graticule };
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
			final JCheckBox check = new JCheckBox(layer.toString());
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
		}
	}
}
