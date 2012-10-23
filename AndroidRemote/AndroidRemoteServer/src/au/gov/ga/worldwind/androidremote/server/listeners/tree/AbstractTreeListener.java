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
package au.gov.ga.worldwind.androidremote.server.listeners.tree;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;

import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Abstract listener for tree models that converts tree change events to
 * {@link ItemModel} changes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractTreeListener implements TreeModelListener
{
	protected final ItemModel model;
	protected final TreeModel treeModel;
	protected final Map<Object, Item> objectToItemMap = new HashMap<Object, Item>();

	public AbstractTreeListener(ItemModel model, TreeModel treeModel)
	{
		this.model = model;
		this.treeModel = treeModel;
	}

	public void clear()
	{
		objectToItemMap.clear();
		model.setRoot(null);
	}

	public void sendRefreshedMessage()
	{
		Object root = treeModel.getRoot();
		Item rootItem = objectToItemWithCache(root);
		addChildren(root, rootItem);
		model.setRoot(rootItem);
	}

	private void addChildren(Object parent, Item parentItem)
	{
		for (int i = 0; i < treeModel.getChildCount(parent); i++)
		{
			Object child = treeModel.getChild(parent, i);
			Item childItem = objectToItemWithCache(child);
			parentItem.addChild(childItem);
			childItem.setParent(parentItem);
			addChildren(child, childItem);
		}
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		//TODO implement this in the layer/dataset trees!
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
		Object[] path = e.getPath();
		if (path.length > 0 && path[0] == treeModel.getRoot())
		{
			Object parent = path[path.length - 1];
			Item parentItem = objectToItemWithCache(parent);
			for (int i = 0; i < e.getChildren().length; i++)
			{
				Object child = e.getChildren()[i];
				int index = e.getChildIndices()[i];
				Item childItem = objectToItemWithCache(child);
				if (shouldAddChildrenOnInsert())
				{
					addChildren(child, childItem);
				}
				model.addItem(childItem, parentItem, index);
			}
		}
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
		Object[] children = e.getChildren();
		for (Object child : children)
		{
			Item item = objectToItemMap.remove(child);
			if (item != null)
			{
				model.removeItem(item);
			}
		}
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
		// TODO Auto-generated method stub
	}

	protected Item objectToItemWithCache(Object object)
	{
		if (!objectToItemMap.containsKey(object))
		{
			objectToItemMap.put(object, objectToItem(object));
		}
		return objectToItemMap.get(object);
	}

	protected abstract Item objectToItem(Object object);

	protected abstract boolean shouldAddChildrenOnInsert();
}
