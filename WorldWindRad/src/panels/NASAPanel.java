package panels;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGSurfaceLayer;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class NASAPanel extends JPanel
{
	private Layer stars;
	private Layer atmosphere;
	private Layer fog;
	private Layer bmng;
	private Layer landsat;
	private Layer pnl;
	private Layer compass;
	private Layer map;
	private Layer scale;

	private Layer[] layers;

	private WorldWindow wwd;

	public NASAPanel(WorldWindow wwd)
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
		bmng = new BMNGSurfaceLayer();
		landsat = new LandsatI3();
		pnl = new EarthNASAPlaceNameLayer();
		compass = new CompassLayer();
		map = new WorldMapLayer();
		scale = new ScalebarLayer();

		layers = new Layer[] { stars, atmosphere, fog, bmng, landsat, pnl,
				compass, map, scale };
		for (Layer layer : layers)
		{
			layer.setEnabled(true);
			wwd.getModel().getLayers().add(layer);
		}
	}

	private void fillPanel()
	{
		setLayout(new GridLayout(0, 1));

		for(final Layer layer : layers)
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
