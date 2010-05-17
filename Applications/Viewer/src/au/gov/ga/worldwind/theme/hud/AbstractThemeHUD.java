package au.gov.ga.worldwind.theme.hud;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemeHUD;

public abstract class AbstractThemeHUD implements ThemeHUD
{
	private Layer layer;

	protected abstract Layer createLayer();

	public AbstractThemeHUD()
	{
		layer = createLayer();
	}

	@Override
	public String getDisplayName()
	{
		return layer.getName();
	}

	@Override
	public void setDisplayName(String name)
	{
		layer.setName(name);
	}

	@Override
	public boolean isOn()
	{
		return layer.isEnabled();
	}

	@Override
	public void setOn(boolean on)
	{
		layer.setEnabled(on);
	}

	@Override
	public void setup(Theme theme)
	{
		theme.getWwd().getModel().getLayers().add(layer);
	}

	@Override
	public void dispose()
	{
		layer.dispose();
	}

	@Override
	public final void setPosition(String position)
	{
		doSetPosition(convertPosition(position));
	}

	public abstract void doSetPosition(String position);

	private static String convertPosition(String position)
	{
		String l = position.toLowerCase();
		if (l.equals("north"))
			return AVKey.NORTH;
		if (l.equals("south"))
			return AVKey.SOUTH;
		if (l.equals("east"))
			return AVKey.EAST;
		if (l.equals("west"))
			return AVKey.WEST;
		if (l.equals("northeast"))
			return AVKey.NORTHEAST;
		if (l.equals("northwest"))
			return AVKey.NORTHWEST;
		if (l.equals("southeast"))
			return AVKey.SOUTHEAST;
		if (l.equals("southwest"))
			return AVKey.SOUTHWEST;
		if (l.equals("center"))
			return AVKey.CENTER;
		return position;
	}
}
