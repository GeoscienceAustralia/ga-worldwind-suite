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

import javax.swing.tree.TreeModel;

import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.INode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModelListener;

/**
 * Layer tree listener that listens for tree model events on the Layers tree and
 * updates the ItemModel accordingly.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeListener extends AbstractTreeListener implements LayerTreeModelListener
{
	public LayerTreeListener(ItemModel model, TreeModel treeModel)
	{
		super(model, treeModel);
	}

	@Override
	protected Item objectToItem(Object object)
	{
		Item item = new Item();
		if (object instanceof INode)
		{
			INode node = (INode) object;
			item.setName(node.getName());
			if (node instanceof ILayerNode)
			{
				ILayerNode layer = (ILayerNode) node;
				item.setLeaf(true);
				item.setOpacity((float) layer.getOpacity());
				item.setEnabled(layer.isEnabled());
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
		return false;
	}

	@Override
	public void enabledChanged(ILayerNode layer, boolean enabled)
	{
		Item item = objectToItemWithCache(layer);
		model.setItemEnabled(item, enabled);
	}

	@Override
	public void opacityChanged(ILayerNode layer, double opacity)
	{
		Item item = objectToItemWithCache(layer);
		model.setItemOpacity(item, (float) opacity);
	}
}
