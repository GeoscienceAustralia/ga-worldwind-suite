package au.gov.ga.worldwind.viewer.theme.hud;

import javax.swing.Icon;

import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;

/**
 * {@link ThemeHUD} implementation that displays the {@link CompassLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CompassHUD extends AbstractThemeHUD
{
	private CompassLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new CompassLayer();
		return layer;
	}

	@Override
	public void doSetPosition(String position)
	{
		layer.setPosition(position);
	}

	@Override
	public String getPosition()
	{
		return layer.getPosition();
	}

	@Override
	public Icon getIcon()
	{
		return Icons.compass.getIcon();
	}
}
