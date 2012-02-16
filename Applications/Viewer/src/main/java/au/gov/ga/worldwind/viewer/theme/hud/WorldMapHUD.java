package au.gov.ga.worldwind.viewer.theme.hud;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.WorldMapLayer;

import javax.swing.Icon;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;

/**
 * {@link ThemeHUD} implementation that displays the {@link WorldMapLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	public void setPickEnabled(boolean pickable)
	{
		layer.setPickEnabled(pickable);
	}
}
