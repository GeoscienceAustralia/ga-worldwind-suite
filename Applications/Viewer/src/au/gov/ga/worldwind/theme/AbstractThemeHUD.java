package au.gov.ga.worldwind.theme;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;

public abstract class AbstractThemeHUD implements ThemeHUD
{
	private Layer layer;
	private List<ThemePieceListener> listeners = new ArrayList<ThemePieceListener>();

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
		if (layer.isEnabled() != on)
		{
			layer.setEnabled(on);
			for (ThemePieceListener listener : listeners)
				listener.onToggled(on);
		}
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

	@Override
	public void addListener(ThemePieceListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(ThemePieceListener listener)
	{
		listeners.remove(listener);
	}
}
