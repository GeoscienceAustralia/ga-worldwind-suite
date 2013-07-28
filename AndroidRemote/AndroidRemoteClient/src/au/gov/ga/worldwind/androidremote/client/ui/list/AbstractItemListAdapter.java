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
package au.gov.ga.worldwind.androidremote.client.ui.list;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import au.gov.ga.worldwind.androidremote.client.ui.ItemModelPagerAdapter;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Adapter used by the {@link ItemModelPagerAdapter}'s ListView for displaying
 * item children.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractItemListAdapter extends BaseAdapter
{
	protected final ItemModel model;
	private final Item item;
	protected final LayoutInflater inflater;

	public AbstractItemListAdapter(ItemModel model, Item item, LayoutInflater inflater)
	{
		this.model = model;
		this.item = item;
		this.inflater = inflater;
	}

	@Override
	public int getCount()
	{
		if (item == null)
		{
			return 0;
		}
		return item.childCount();
	}

	@Override
	public Object getItem(int position)
	{
		return item.getChild(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}
}
