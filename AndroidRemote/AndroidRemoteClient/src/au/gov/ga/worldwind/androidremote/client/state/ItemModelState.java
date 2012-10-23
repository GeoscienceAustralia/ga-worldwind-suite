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

import java.util.ArrayList;
import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import au.gov.ga.worldwind.androidremote.client.ui.ItemModelPagerAdapter;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemsRefreshedMessage;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModelListener;

/**
 * Tracks additional state information for an {@link ItemModel}. Handles
 * refreshing of the pager and list when the model changes. Also acts as a
 * datasource for the {@link PagerAdapter} implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ItemModelState implements OnPageChangeListener
{
	private final String rootTitle;
	protected final ItemModel model;
	protected Item root = null;
	protected Item currentItem = null;
	protected ItemModelPagerAdapter currentPagerAdapter;
	protected BaseAdapter currentListAdapter;
	protected ViewPager currentPager;
	protected List<Item> flattened;
	protected int currentPage = 0;
	protected final List<Integer> pageHistory = new ArrayList<Integer>();
	protected boolean goingBack = false;
	private int ignoreLevels = 0;

	public ItemModelState(ItemModel model, String rootTitle)
	{
		this.model = model;
		this.rootTitle = rootTitle;
		model.addListener(listener);
	}

	/**
	 * @return Associated {@link ItemModel}
	 */
	public ItemModel getModel()
	{
		return model;
	}

	public int getIgnoreLevels()
	{
		return ignoreLevels;
	}

	protected void setIgnoreLevels(int ignoreLevels)
	{
		this.ignoreLevels = ignoreLevels;
		validateCurrentItemDepth();
	}

	protected void validateCurrentItemDepth()
	{
		if (currentItem != null)
		{
			int currentItemDepth = currentItem.depth();
			while (currentItemDepth < ignoreLevels)
			{
				currentItem = currentItem.getChild(0);
				currentItemDepth++;
			}
		}
	}

	/**
	 * @return The {@link PagerAdapter} for the currently visible
	 *         {@link ViewPager} for this model state.
	 */
	public ItemModelPagerAdapter getCurrentPagerAdapter()
	{
		return currentPagerAdapter;
	}

	/**
	 * Set the current pager adapter.
	 * {@link PagerAdapter#notifyDataSetChanged()} is called on this adapter
	 * when the model's data changes.
	 * 
	 * @param currentPagerAdapter
	 */
	public void setCurrentPagerAdapter(ItemModelPagerAdapter currentPagerAdapter)
	{
		this.currentPagerAdapter = currentPagerAdapter;
	}

	/**
	 * @return The currently visible {@link ViewPager} for this model state.
	 */
	public ViewPager getCurrentPager()
	{
		return currentPager;
	}

	/**
	 * Set the currently visible pager adapter.
	 * {@link ViewPager#setCurrentItem(int, boolean)} is called to push a new
	 * page into the view when an item is clicked. See
	 * {@link #click(int, int, boolean)}.
	 * 
	 * @param currentPager
	 */
	public void setCurrentPager(ViewPager currentPager)
	{
		this.currentPager = currentPager;
	}

	/**
	 * @return The list adapter for the currently visible list in the pager for
	 *         this state.
	 */
	public BaseAdapter getCurrentListAdapter()
	{
		return currentListAdapter;
	}

	/**
	 * Set the list adapter used by the currently visible list.
	 * {@link BaseAdapter#notifyDataSetChanged()} is called on this object when
	 * the list data changes (eg the opacity of an item changes).
	 * 
	 * @param currentListAdapter
	 */
	public void setCurrentListAdapter(BaseAdapter currentListAdapter)
	{
		this.currentListAdapter = currentListAdapter;
	}

	/**
	 * Handle a received {@link ItemMessage}.
	 * 
	 * @param itemMessage
	 */
	public void handleMessage(ItemMessage<?> itemMessage)
	{
		model.handleMessage(itemMessage);
	}

	/**
	 * @return Number of items in this state. Corresponds to the number of pages
	 *         shown by this state's {@link ViewPager}.
	 */
	public int getCount()
	{
		if (currentItem == null)
		{
			return 0;
		}
		return currentItem.depth() + 1 - ignoreLevels;
	}

	/**
	 * Return the page item at the given position. The view pager uses this item
	 * as its data to display (ie the list of this item's children).
	 * 
	 * @param position
	 * @return Item for the page at position.
	 */
	public Item getItem(int position)
	{
		int count = getCount();
		Item item = currentItem;
		while (count >= position + 2)
		{
			item = item.getParent();
			if (item == null)
			{
				return null;
			}
			count--;
		}
		return item;
	}

	/**
	 * Title of the page at the given position.
	 * 
	 * @param position
	 * @return Title of the page at position.
	 */
	public String getPageTitle(int position)
	{
		if (position <= 0)
		{
			return rootTitle;
		}
		return getItem(position).getName();
	}

	/**
	 * Called when the user clicks (taps) an item in this state. Updates the
	 * currentItem to the item tapped, and moves the ViewPager to the right to
	 * view the item.
	 * 
	 * @param clickedAtIndex
	 *            Page position at which an item was clicked
	 * @param childIndex
	 *            Item index that was clicked
	 * @param flatten
	 *            Is this a flattened list?
	 */
	public void click(int clickedAtIndex, int childIndex, boolean flatten)
	{
		if (flatten)
		{
			itemSelected(flattened.get(childIndex));
		}
		else
		{
			Item item = getItem(clickedAtIndex);
			Item child = item.getChild(childIndex);
			if (child != null)
			{
				if (!child.isLeaf() || child.childCount() > 0)
				{
					currentItem = child;
					if (currentPagerAdapter != null)
					{
						currentPagerAdapter.notifyDataSetChanged();
					}
					if (currentPager != null)
					{
						currentPager.setCurrentItem(clickedAtIndex + 1, true);
					}
				}
				itemSelected(child);
			}
		}
	}

	/**
	 * @return A flattened list of items (list of all leaves in the tree).
	 */
	public List<Item> getFlattened()
	{
		if (flattened == null)
		{
			flattened = new ArrayList<Item>();
			Item root = model.getRoot();
			if (root != null)
			{
				addLeavesToList(flattened, root);
			}
		}
		return flattened;
	}

	/**
	 * Add all children of parent that are leaves to the given list. Recurses
	 * over parent's children.
	 * 
	 * @param list
	 *            List to add leaves to
	 * @param parent
	 *            Parent item to check if isLeaf (recurses over children)
	 */
	protected void addLeavesToList(List<Item> list, Item parent)
	{
		if (parent.isLeaf())
		{
			list.add(parent);
		}
		else
		{
			for (Item child : parent.getChildren())
			{
				addLeavesToList(list, child);
			}
		}
	}

	/**
	 * Clear the state. Called when disconnected from server.
	 */
	public void clear()
	{
		model.setRoot(null);
		flattened = null;
		if (currentPagerAdapter != null)
		{
			currentPagerAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Called when the server sends the root node in a
	 * {@link ItemsRefreshedMessage}.
	 * 
	 * @param root
	 */
	protected void handleItemsRefreshed(Item root)
	{
		this.root = root;
		currentItem = root;
		validateCurrentItemDepth();
		if (currentPagerAdapter != null)
		{
			currentPagerAdapter.notifyDataSetChanged();
		}
		flattened = null;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
	}

	@Override
	public void onPageSelected(int page)
	{
		if (!goingBack)
		{
			pageHistory.add(currentPage);
		}
		currentPage = page;
	}

	/**
	 * Called when the user presses the back button. Goes back through the
	 * {@link ViewPager}'s history.
	 */
	public void goBack()
	{
		goingBack = true;
		if (currentPager != null && !pageHistory.isEmpty())
		{
			int page = pageHistory.remove(pageHistory.size() - 1);
			currentPager.setCurrentItem(page, true);
		}
		goingBack = false;
	}

	/**
	 * Called when the user selects an item. For subclasses to implement.
	 * 
	 * @param item
	 *            Selected item
	 */
	protected abstract void itemSelected(Item item);

	/**
	 * Create a {@link ListAdapter} for displaying the given item's children.
	 * 
	 * @param inflater
	 * @param item
	 *            Item whose children will be displayed in the list.
	 * @param flatten
	 *            Should a flattened list adapter be created?
	 * @return New {@link ListAdapter}.
	 */
	public final ListAdapter createListAdapter(LayoutInflater inflater, Item item, boolean flatten)
	{
		BaseAdapter adapter = createListAdapterPlease(inflater, item, flatten);
		setCurrentListAdapter(adapter);
		return adapter;
	}

	/**
	 * Create a {@link ListAdapter} for displaying the given item's children.
	 * 
	 * @param inflater
	 * @param item
	 *            Item whose children will be displayed in the list.
	 * @param flatten
	 *            Should a flattened list adapter be created?
	 * @return New {@link ListAdapter}.
	 */
	protected abstract BaseAdapter createListAdapterPlease(LayoutInflater inflater, Item item, boolean flatten);

	/**
	 * {@link ItemModelListener} implementation that passes ItemModel messages
	 * to this state object.
	 */
	private final ItemModelListener listener = new ItemModelListener()
	{
		@Override
		public void itemsRefreshed(Item root, boolean remote)
		{
			handleItemsRefreshed(root);
		}

		@Override
		public void itemsAdded(Item item, Item parent, int index, boolean remote)
		{
			flattened = null;
			if (currentPagerAdapter != null)
			{
				currentPagerAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote)
		{
			flattened = null;

			Item newItem = root;
			for (int i = 0; i < oldIndicesToRoot.length - 1; i++)
			{
				Item child = newItem.getChild(oldIndicesToRoot[i]);
				if (child == null)
				{
					break;
				}
				newItem = child;
			}
			currentItem = newItem;
			validateCurrentItemDepth();

			if (currentPagerAdapter != null)
			{
				currentPagerAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void itemEnabled(Item item, boolean enabled, boolean remote)
		{
			if (currentListAdapter != null)
			{
				currentListAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void itemSelected(Item item, boolean remote)
		{
			if (currentListAdapter != null)
			{
				currentListAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void itemOpacityChanged(Item item, float opacity, boolean remote)
		{
			if (currentListAdapter != null)
			{
				currentListAdapter.notifyDataSetChanged();
			}
		}
	};
}
