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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.state.ItemModelState;
import au.gov.ga.worldwind.androidremote.client.state.ItemModelStateProvider;
import au.gov.ga.worldwind.androidremote.client.ui.menu.ItemModelFragmentMenuProvider;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Fragment which displays a {@link ViewPager} (with titles using a
 * TitlePageIndicator). Pages are {@link Item}s in an {@link ItemModel}.
 * Selecting an item will push a new page into the ViewPager (handled by
 * {@link ItemModelState}).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemModelFragment extends SherlockFragment
{
	public final static String ITEM_MODEL_STATE_ID_KEY = "itemStateModelId";
	public final static String FLATTEN_KEY = "flatten";

	private ItemModelStateProvider stateProvider;
	private ItemModelState state;
	private boolean flatten;
	private Handler handler;
	private ItemModelFragmentMenuProvider menuProvider;

	public static ItemModelFragment newInstance(int itemModelStateId, boolean flatten)
	{
		ItemModelFragment f = new ItemModelFragment();
		Bundle arguments = new Bundle();
		arguments.putInt(ITEM_MODEL_STATE_ID_KEY, itemModelStateId);
		arguments.putBoolean(FLATTEN_KEY, flatten);
		f.setArguments(arguments);
		return f;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (!(activity instanceof ItemModelStateProvider))
		{
			throw new IllegalArgumentException("Activity must be an instanceof "
					+ ItemModelStateProvider.class.getSimpleName());
		}
		stateProvider = (ItemModelStateProvider) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle arguments = getArguments();
		int itemModelStateId = arguments.getInt(ITEM_MODEL_STATE_ID_KEY);
		state = stateProvider.getItemModelState(itemModelStateId);
		flatten = arguments.getBoolean(FLATTEN_KEY);
		menuProvider = stateProvider.getMenuProvider(itemModelStateId);

		handler = new Handler();

		setHasOptionsMenu(menuProvider.hasOptionsMenu());
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(ITEM_MODEL_STATE_ID_KEY, state.getModel().getId());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			int itemModelStateId = savedInstanceState.getInt(ITEM_MODEL_STATE_ID_KEY);
			state = stateProvider.getItemModelState(itemModelStateId);
		}

		final View view = inflater.inflate(R.layout.pager_fragment, container, false);
		final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		final ItemModelPagerAdapter adapter = new ItemModelPagerAdapter(inflater, state, flatten);

		state.setCurrentPager(pager);
		state.setCurrentPagerAdapter(adapter);

		//prevent "java.lang.IllegalStateException: Recursive entry to executePendingTransactions"
		//by setting pager adapter after fragment transaction is complete:
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				pager.setAdapter(adapter);
				TitlePageIndicator titles = (TitlePageIndicator) view.findViewById(R.id.titles);
				titles.setViewPager(pager);
			}
		});

		return view;
	}

	public void onBackPressed()
	{
		state.goBack();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		menuProvider.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return menuProvider.onOptionsItemSelected(item);
	}
}
