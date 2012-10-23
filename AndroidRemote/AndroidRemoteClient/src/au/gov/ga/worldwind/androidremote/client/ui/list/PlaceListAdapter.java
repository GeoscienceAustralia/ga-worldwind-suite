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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Adapter used for displaying place nodes in a ListView.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlaceListAdapter extends AbstractItemListAdapter
{
	public PlaceListAdapter(ItemModel model, Item item, LayoutInflater inflater)
	{
		super(model, item, inflater);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view =
				convertView != null ? convertView : inflater.inflate(android.R.layout.simple_list_item_checked, parent,
						false);
		CheckedTextView text = (CheckedTextView) view.findViewById(android.R.id.text1);

		view.setBackgroundResource(R.drawable.list_item_background);

		Item item = (Item) getItem(position);
		text.setText(item.getName());

		int iconId = R.drawable.bookmarks;
		text.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);

		boolean checked = model.getSelectedItem() == item;
		text.setChecked(checked);

		return view;
	}
}
