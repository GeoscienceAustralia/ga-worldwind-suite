package au.gov.ga.worldwind.viewer.theme.hud;

import javax.swing.Icon;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import au.gov.ga.worldwind.viewer.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.util.Icons;

public class ControlsHUD extends AbstractThemeHUD
{
	private ViewControlsLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new ViewControlsLayer();
		layer.setShowVeControls(false);
		layer.setShowFovControls(false);
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
	public void setup(Theme theme)
	{
		super.setup(theme);

		WorldWindow wwd = theme.getWwd();
		wwd.addSelectListener(new ViewControlsSelectListener(wwd, layer));
	}
	
	@Override
	public Icon getIcon()
	{
		return Icons.navigation.getIcon();
	}
}
