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

import android.app.Activity;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.ui.EditItemDialog;
import au.gov.ga.worldwind.androidremote.client.ui.list.FlatLayerListAdapter;
import au.gov.ga.worldwind.androidremote.client.ui.list.LayerListAdapter;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Concrete {@link ItemModelState} subclass that provides state for the layer
 * ItemModel.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerModelState extends ItemModelState
{
	private final Activity activity;

	public LayerModelState(Communicator communicator, Activity activity)
	{
		super(new ItemModel(1, communicator), activity.getString(R.string.layers_tab));
		this.activity = activity;
	}

	@Override
	protected BaseAdapter createListAdapterPlease(LayoutInflater inflater, Item item, boolean flatten)
	{
		return flatten ? new FlatLayerListAdapter(this, inflater) : new LayerListAdapter(model, item, inflater);
	}

	@Override
	protected void itemSelected(Item item)
	{
		if (item.isLeaf())
		{
			Dialog dialog = new EditItemDialog(activity, item, model);
			dialog.setCanceledOnTouchOutside(true);

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());

			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			lp.width = Math.min(metrics.widthPixels - 20, (int) (400 * metrics.density));

			dialog.show();
			dialog.getWindow().setAttributes(lp);
		}
		else
		{
			model.selectItem(item);
		}
	}
}
