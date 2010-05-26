package au.gov.ga.worldwind.theme.hud;

import javax.swing.Icon;

import au.gov.ga.worldwind.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.util.Icons;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.WorldMapLayer;

public class WorldMapHUD extends AbstractThemeHUD
{
	private WorldMapLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new WorldMapLayer();
		return layer;
	}

	@Override
	public String getPosition()
	{
		return layer.getPosition();
	}

	@Override
	public void doSetPosition(String position)
	{
		layer.setPosition(position);
	}

	@Override
	public Icon getIcon()
	{
		return Icons.world.getIcon();
	}
}
