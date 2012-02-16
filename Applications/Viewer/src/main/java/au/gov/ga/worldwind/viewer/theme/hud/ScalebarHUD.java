package au.gov.ga.worldwind.viewer.theme.hud;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;

import javax.swing.Icon;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;

/**
 * {@link ThemeHUD} implementation that displays the {@link ScalebarLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ScalebarHUD extends AbstractThemeHUD
{
	private ScalebarLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new ScalebarLayer();
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
		return Icons.scalebar.getIcon();
	}
}
