package au.gov.ga.worldwind.theme.hud;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import au.gov.ga.worldwind.theme.AbstractThemeHUD;
import au.gov.ga.worldwind.theme.Theme;

public class ControlsHUD extends AbstractThemeHUD
{
	private ViewControlsLayer layer;

	@Override
	protected Layer createLayer()
	{
		layer = new ViewControlsLayer();
		layer.setShowVeControls(false);
		layer.setShowFovControls(true);
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
}
