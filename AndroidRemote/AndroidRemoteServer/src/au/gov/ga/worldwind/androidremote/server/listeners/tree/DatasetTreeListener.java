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

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.common.util.MultiMap;
import au.gov.ga.worldwind.viewer.panels.dataset.IData;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;

/**
 * Dataset tree listener that listens for tree model events on the Datasets tree
 * and updates the ItemModel accordingly. Also listens to the layer tree model
 * for changes so that dataset layer Items in the ItemModel know when they are
 * enabled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetTreeListener extends AbstractTreeListener
{
	private final LayerTreeModel layerTreeModel;
	private final MultiMap<URL, Item> layerUrlToItems = new MultiMap<URL, Item>();

	public DatasetTreeListener(ItemModel model, TreeModel treeModel, LayerTreeModel layerTreeModel)
	{
		super(model, treeModel);
		this.layerTreeModel = layerTreeModel;
	}

	@Override
	protected Item objectToItem(Object object)
	{
		Item item = new Item();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
		if (node.getUserObject() instanceof IData)
		{
			IData data = (IData) node.getUserObject();
			item.setName(data.getName());
			if (data instanceof ILayerDefinition)
			{
				ILayerDefinition layer = (ILayerDefinition) data;
				item.setLeaf(true);
				item.setEnabled(layerTreeModel.containsLayer(layer));
				layerUrlToItems.putSingle(layer.getLayerURL(), item);
			}
		}
		else
		{
			item.setName(object.toString());
		}
		return item;
	}

	@Override
	protected boolean shouldAddChildrenOnInsert()
	{
		return true;
	}

	public void setLayerTreeModelListenerEnabled(boolean enabled)
	{
		if (enabled)
		{
			layerTreeModel.addTreeModelListener(layerTreeModelListener);
		}
		else
		{
			layerTreeModel.removeTreeModelListener(layerTreeModelListener);
		}
	}

	private final TreeModelListener layerTreeModelListener = new TreeModelListener()
	{
		@Override
		public void treeNodesChanged(TreeModelEvent e)
		{
		}

		@Override
		public void treeNodesInserted(TreeModelEvent e)
		{
			refreshUrlsForChildren(e, false);
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e)
		{
			refreshUrlsForChildren(e, true);
		}

		@Override
		public void treeStructureChanged(TreeModelEvent e)
		{
		}

		private void refreshUrlsForChildren(TreeModelEvent e, boolean shouldCheckChildren)
		{
			Set<URL> urlsToCheck = new HashSet<URL>();
			for (Object child : e.getChildren())
			{
				addUrlsFromChildren(child, urlsToCheck, shouldCheckChildren);
			}
			for (URL url : urlsToCheck)
			{
				List<Item> items = layerUrlToItems.get(url);
				if (items != null && !items.isEmpty())
				{
					boolean enabled = layerTreeModel.containsLayer(url);
					for (Item item : items)
					{
						if (item.isEnabled() != enabled)
						{
							model.setItemEnabled(item, enabled);
						}
					}
				}
			}
		}

		private void addUrlsFromChildren(Object node, Set<URL> urls, boolean recurse)
		{
			if (node instanceof ILayerNode)
			{
				ILayerNode layer = (ILayerNode) node;
				urls.add(layer.getLayerURL());
			}
			if (recurse)
			{
				int childCount = layerTreeModel.getChildCount(node);
				for (int i = 0; i < childCount; i++)
				{
					Object child = layerTreeModel.getChild(node, i);
					addUrlsFromChildren(child, urls, recurse);
				}
			}
		}
	};
}
