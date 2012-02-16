package au.gov.ga.worldwind.viewer.theme.hud;

import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;

import javax.swing.Icon;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;

/**
 * {@link ThemeHUD} implementation that displays the {@link CrosshairLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
