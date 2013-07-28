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
package au.gov.ga.worldwind.androidremote.client.state;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.ui.list.PlaceListAdapter;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Concrete {@link ItemModelState} subclass that provides state for the place
 * ItemModel.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlaceModelState extends ItemModelState
{
	public PlaceModelState(Communicator communicator, Context context)
	{
		super(new ItemModel(2, communicator), context.getString(R.string.places_tab));
	}

	@Override
	protected BaseAdapter createListAdapterPlease(LayoutInflater inflater, Item item, boolean flatten)
	{
		return new PlaceListAdapter(model, item, inflater);
	}

	@Override
	protected void itemSelected(Item item)
	{
		model.selectItem(item);
	}
}
