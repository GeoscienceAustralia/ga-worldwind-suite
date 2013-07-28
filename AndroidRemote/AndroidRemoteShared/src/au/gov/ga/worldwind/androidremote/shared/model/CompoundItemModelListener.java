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
package au.gov.ga.worldwind.androidremote.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ItemModelListener} implementation that delegates methods to a list of
 * child {@link ItemModelListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CompoundItemModelListener implements ItemModelListener
{
	private final List<ItemModelListener> listeners = new ArrayList<ItemModelListener>();

	/**
	 * Add a listener.
	 * 
	 * @param listener
	 *            Listener to add.
	 */
	public void addListener(ItemModelListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	public void removeListener(ItemModelListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void itemsRefreshed(Item root, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemsRefreshed(root, remote);
		}
	}

	@Override
	public void itemsAdded(Item item, Item parent, int index, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemsAdded(item, parent, index, remote);
		}
	}

	@Override
	public void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemRemoved(item, oldIndicesToRoot, remote);
		}
	}

	@Override
	public void itemEnabled(Item item, boolean enabled, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemEnabled(item, enabled, remote);
		}
	}

	@Override
	public void itemSelected(Item item, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemSelected(item, remote);
		}
	}

	@Override
	public void itemOpacityChanged(Item item, float opacity, boolean remote)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).itemOpacityChanged(item, opacity, remote);
		}
	}
}
