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

import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemAddedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemEnabledMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemOpacityMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemRemovedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemSelectedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemsRefreshedMessage;

/**
 * Model that contains the root {@link Item}, and handles any communication of
 * changes to items between devices. Also stores the currently selected item.
 * <p/>
 * Any enabling/disabling of items, or changing of item opacity, etc, should go
 * through this class rather than calling the setter methods on the Item
 * directly. This way the models on both devices can be kept in sync.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemModel
{
	private final int id;
	private final Communicator communicator;

	private Item root;
	private final CompoundItemModelListener listeners = new CompoundItemModelListener();
	private ItemMessage<?> messageBeingHandled;
	private boolean addingItem = false;
	private boolean removingItem = false;
	private Item selectedItem;

	public ItemModel(int id, Communicator communicator)
	{
		this.id = id;
		this.communicator = communicator;
	}

	/**
	 * @return The unique id of this model. Different for different item types
	 *         (layers, datasets, places).
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Sent the message using the communicator if the current message being
	 * handled isn't of the same time (or if there's no message currently being
	 * handled).
	 * 
	 * @param message
	 */
	protected void maybeSendMessage(ItemMessage<?> message)
	{
		//don't repeat a message that is being handled, otherwise a infinite message loop will occur
		if (messageBeingHandled == null || !message.getClass().equals(messageBeingHandled.getClass()))
		{
			communicator.sendMessage(message);
		}
	}

	/**
	 * @return Root item.
	 */
	public Item getRoot()
	{
		return root;
	}

	/**
	 * Set the root item of this model. Will cause an
	 * {@link ItemsRefreshedMessage} message to be sent.
	 * 
	 * @param root
	 */
	public void setRoot(Item root)
	{
		this.root = root;
		listeners.itemsRefreshed(root, messageBeingHandled != null);
		maybeSendMessage(new ItemsRefreshedMessage(id, root));
	}

	/**
	 * Add the given item as a child of the given parent. Causes an
	 * {@link ItemAddedMessage} to be sent.
	 * 
	 * @param item
	 *            Item to add
	 * @param parent
	 *            Parent to add as a child of
	 * @param index
	 *            Index in the parent's child list to insert the item
	 */
	public void addItem(Item item, Item parent, int index)
	{
		//don't allow an item to be added twice (eg from listener)
		if (addingItem)
		{
			return;
		}
		addingItem = true;
		if (0 <= index && index < parent.childCount())
		{
			parent.insertChild(index, item);
		}
		else
		{
			parent.addChild(item);
		}
		item.setParent(parent);
		listeners.itemsAdded(item, parent, index, messageBeingHandled != null);
		maybeSendMessage(new ItemAddedMessage(id, item, item.indicesToRoot()));
		addingItem = false;
	}

	/**
	 * Remove the given item from its parent. Causes an
	 * {@link ItemRemovedMessage} to be sent.
	 * 
	 * @param item
	 *            Item to remove from its parent.
	 */
	public void removeItem(Item item)
	{
		//don't allow an item to be removed twice (eg from listener)
		if (removingItem)
		{
			return;
		}
		removingItem = true;
		if (item == root)
		{
			setRoot(null);
		}
		else if (item.getParent() != null)
		{
			//have to get the indices before removing the item from its parent
			int[] indicesToRoot = item.indicesToRoot();
			item.getParent().removeChild(item);
			item.setParent(null);
			listeners.itemRemoved(item, indicesToRoot, messageBeingHandled != null);
			maybeSendMessage(new ItemRemovedMessage(id, indicesToRoot));
		}
		removingItem = false;
	}

	/**
	 * Remove the given item, and any parents if removing the item will leave
	 * the parent empty. Calls {@link #removeItem(Item)}.
	 * 
	 * @param item
	 *            Item to remove.
	 */
	public void removeItemAndParentsIfEmpty(Item item)
	{
		Item remove = item;
		while (true)
		{
			Item parent = remove.getParent();
			if (parent == null || parent == root || parent.getChildren().size() > 1)
			{
				break;
			}
			remove = parent;
		}
		removeItem(remove);
	}

	/**
	 * Enable/disable the given item. Causes an {@link ItemEnabledMessage} to be
	 * sent.
	 * 
	 * @param item
	 *            Item to enable/disable
	 * @param enabled
	 *            Enable or disable?
	 */
	public void setItemEnabled(Item item, boolean enabled)
	{
		item.setEnabled(enabled);
		listeners.itemEnabled(item, enabled, messageBeingHandled != null);
		maybeSendMessage(new ItemEnabledMessage(id, item.indicesToRoot(), enabled));
	}

	/**
	 * Set the opacity of the given item. Causes an {@link ItemOpacityMessage}
	 * to be sent.
	 * 
	 * @param item
	 *            Item to set the opacity of
	 * @param opacity
	 *            Item's opacity value
	 */
	public void setItemOpacity(Item item, float opacity)
	{
		item.setOpacity(opacity);
		listeners.itemOpacityChanged(item, opacity, messageBeingHandled != null);
		maybeSendMessage(new ItemOpacityMessage(id, item.indicesToRoot(), opacity));
	}

	/**
	 * Select the given item. Causes an {@link ItemSelectedMessage} to be sent.
	 * 
	 * @param item
	 *            Item to select
	 */
	public void selectItem(Item item)
	{
		selectedItem = item;
		listeners.itemSelected(item, messageBeingHandled != null);
		maybeSendMessage(new ItemSelectedMessage(id, item.indicesToRoot()));
	}

	/**
	 * Handle the given ItemMessage. Only handled if the message's modelId
	 * matches this model's id.
	 * 
	 * @param m
	 *            Message to handle.
	 */
	public void handleMessage(ItemMessage<?> m)
	{
		//ensure the incoming message id is for this particular model:
		if (m.getModelId() != id)
		{
			return;
		}

		messageBeingHandled = m;
		if (m instanceof ItemsRefreshedMessage)
		{
			ItemsRefreshedMessage message = (ItemsRefreshedMessage) m;
			setRoot(message.root);
		}
		else if (m instanceof ItemAddedMessage)
		{
			ItemAddedMessage message = (ItemAddedMessage) m;
			if (message.treeIndexPath.length == 0)
			{
				setRoot(message.item);
			}
			else
			{
				int index = message.treeIndexPath[message.treeIndexPath.length - 1];
				Item parent = itemForTreeIndexPath(message.treeIndexPath, message.treeIndexPath.length - 2);
				if (parent != null)
				{
					addItem(message.item, parent, index);
				}
			}
		}
		else if (m instanceof ItemEnabledMessage)
		{
			ItemEnabledMessage message = (ItemEnabledMessage) m;
			Item item = itemForTreeIndexPath(message.treeIndexPath);
			if (item != null)
			{
				setItemEnabled(item, message.enabled);
			}
		}
		else if (m instanceof ItemOpacityMessage)
		{
			ItemOpacityMessage message = (ItemOpacityMessage) m;
			Item item = itemForTreeIndexPath(message.treeIndexPath);
			if (item != null)
			{
				setItemOpacity(item, message.opacity);
			}
		}
		else if (m instanceof ItemRemovedMessage)
		{
			ItemRemovedMessage message = (ItemRemovedMessage) m;
			Item item = itemForTreeIndexPath(message.treeIndexPath);
			if (item != null)
			{
				removeItem(item);
			}
		}
		else if (m instanceof ItemSelectedMessage)
		{
			ItemSelectedMessage message = (ItemSelectedMessage) m;
			Item item = itemForTreeIndexPath(message.treeIndexPath);
			if (item != null)
			{
				selectItem(item);
			}
		}
		messageBeingHandled = null;
	}

	/**
	 * Add a listener to this model.
	 * 
	 * @param listener
	 *            Listener to add.
	 */
	public void addListener(ItemModelListener listener)
	{
		listeners.addListener(listener);
	}

	/**
	 * Remove a listener from this model.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	public void removeListener(ItemModelListener listener)
	{
		listeners.removeListener(listener);
	}

	/**
	 * Get the item in this model at the given tree index path.
	 * 
	 * @param treeIndexPath
	 * @return Item at treeIndexPath
	 */
	public Item itemForTreeIndexPath(int[] treeIndexPath)
	{
		return itemForTreeIndexPath(treeIndexPath, treeIndexPath.length - 1);
	}

	/**
	 * Get the item in this model at the given tree index path and level in the
	 * path.
	 * 
	 * @param treeIndexPath
	 * @param level
	 *            Level at which to get the item (-1 is root).
	 * @return Item at level in treeIndexPath
	 */
	public Item itemForTreeIndexPath(int[] treeIndexPath, int level)
	{
		level = Math.min(level, treeIndexPath.length - 1);
		Item item = root;
		for (int i = 0; i <= level; i++)
		{
			item = item.getChild(treeIndexPath[i]);
		}
		return item;
	}

	/**
	 * @return Currently selected item.
	 * @see #selectItem(Item)
	 */
	public Item getSelectedItem()
	{
		return selectedItem;
	}
}
