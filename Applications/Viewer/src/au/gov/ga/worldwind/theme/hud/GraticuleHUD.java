package au.gov.ga.worldwind.theme.hud;

import javax.swing.Icon;

import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.util.Icons;

public class GraticuleHUD extends AbstractThemeHUD
{
	private LatLonGraticuleLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new LatLonGraticuleLayer();
		return layer;
	}

	@Override
	public void doSetPosition(String position)
	{
	}

	@Override
	public String getPosition()
	{
		return null;
	}
	
	@Override
	public Icon getIcon()
	{
		return Icons.graticule.getIcon();
	}
}
