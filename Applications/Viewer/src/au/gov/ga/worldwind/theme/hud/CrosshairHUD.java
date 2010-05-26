package au.gov.ga.worldwind.theme.hud;

import javax.swing.Icon;

import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.util.Icons;

public class CrosshairHUD extends AbstractThemeHUD
{
	private CrosshairLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new CrosshairLayer();
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
		return Icons.crosshair.getIcon();
	}
}
