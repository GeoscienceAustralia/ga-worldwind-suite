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
package au.gov.ga.worldwind.androidremote.client.ui;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.state.ItemModelState;
import au.gov.ga.worldwind.androidremote.shared.model.Item;

/**
 * Pager adapter used by the {@link ItemModelFragment}'s ViewPager. Displays a
 * ListView in each page which displays an {@link Item}'s children.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemModelPagerAdapter extends PagerAdapter
{
	private final ItemModelState state;
	private final LayoutInflater inflater;
	private final boolean flatten;

	public ItemModelPagerAdapter(LayoutInflater inflater, ItemModelState state, boolean flatten)
	{
		this.state = state;
		this.inflater = inflater;
		this.flatten = flatten;
	}

	@Override
	public int getCount()
	{
		int count = state.getCount();
		if (flatten)
		{
			count = Math.min(count, 1);
		}
		return count;
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position)
	{
		View view = inflater.inflate(R.layout.list_fragment, null);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		Item item = state.getItem(position);
		listView.setAdapter(state.createListAdapter(inflater, item, flatten));
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int listPosition, long id)
			{
				state.click(position, listPosition, flatten);
			}
		});
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
	}

	@Override
	public int getItemPosition(Object object)
	{
		return POSITION_NONE;
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == object;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return state.getPageTitle(position);
	}
}
