package au.gov.ga.worldwind.theme.hud;

import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.theme.AbstractThemeHUD;

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
}
