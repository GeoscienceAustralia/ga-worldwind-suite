/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.theme;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link ThemeHUD} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractThemeHUD implements ThemeHUD
{
	private Layer layer;
	private List<ThemePieceListener> listeners = new ArrayList<ThemePieceListener>();
	private List<ThemeHUDListener> hudListeners = new ArrayList<ThemeHUDListener>();

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
		String oldName = layer.getName();
		if (oldName != name && (name == null || !name.equals(oldName)))
		{
			layer.setName(name);
			raiseDisplayNameChange();
		}
	}

	@Override
	public boolean isOn()
	{
		return layer.isEnabled();
	}

	@Override
	public void setOn(boolean on)
	{
		if (isOn() != on)
		{
			layer.setEnabled(on);
			raiseOnToggled();
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
		position = convertPosition(position);
		String oldPosition = getPosition();
		if (oldPosition != position && (position == null || !position.equals(oldPosition)))
		{
			doSetPosition(position);
			raisePositionChanged();
		}
	}

	public abstract void doSetPosition(String position);

	private static String convertPosition(String position)
	{
		if (position == null)
			return null;
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
		if (listener instanceof ThemeHUDListener)
			hudListeners.add((ThemeHUDListener) listener);
	}

	@Override
	public void removeListener(ThemePieceListener listener)
	{
		listeners.remove(listener);
		if (listener instanceof ThemeHUDListener)
			hudListeners.remove((ThemeHUDListener) listener);
	}

	protected void raiseOnToggled()
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).onToggled(this);
	}

	protected void raiseDisplayNameChange()
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).displayNameChanged(this);
	}

	protected void raisePositionChanged()
	{
		for (int i = hudListeners.size() - 1; i >= 0; i--)
			hudListeners.get(i).positionChanged(this);
	}
}
