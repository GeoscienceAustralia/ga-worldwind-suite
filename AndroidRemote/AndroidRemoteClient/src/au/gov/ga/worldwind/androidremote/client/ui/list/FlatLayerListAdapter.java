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
import au.gov.ga.worldwind.androidremote.client.state.ItemModelState;
import au.gov.ga.worldwind.androidremote.shared.model.Item;

/**
 * Adapter used for displaying a flattened list of layer nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlatLayerListAdapter extends LayerListAdapter
{
	private final ItemModelState state;

	public FlatLayerListAdapter(ItemModelState state, LayoutInflater inflater)
	{
		super(state.getModel(), state.getModel().getRoot(), inflater);
		this.state = state;
	}

	@Override
	public int getCount()
	{
		return state.getFlattened().size();
	}

	@Override
	public Object getItem(int position)
	{
		return state.getFlattened().get(position);
	}

	@Override
	protected String getText(Item item)
	{
		StringBuilder sb = new StringBuilder();
		Item parent = item.getParent();
		while (parent != null && parent.getParent() != null)
		{
			sb.insert(0, parent.getName() + " | ");
			parent = parent.getParent();
		}
		sb.append(super.getText(item));
		return sb.toString();
	}
}
