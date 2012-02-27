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
package au.gov.ga.worldwind.viewer.panels.layers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.tree.TreeNode;

/**
 * Helper class that supports connecting a World Wind {@link TreeNode} to a
 * layer tree {@link ILayerNode}. This allows a {@link TreeNode} and its child
 * hierarchy to be added to the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WWTreeToLayerTreeConnector
{
	/**
	 * Connect a {@link TreeNode} to a {@link ILayerNode}, and associate with
	 * the given {@link LayerTreeModel}.
	 * 
	 * @param model
	 * @param layerNode
	 * @param treeNode
	 */
	public static void connect(LayerTreeModel model, ILayerNode layerNode, TreeNode treeNode)
	{
		new Listener(model, layerNode, treeNode);
	}

	/**
	 * {@link PropertyChangeListener} that listeners for {@link TreeNode}
	 * changes and adds required {@link ILayerNode}s when new tree node children
	 * are added.
	 */
	protected static class Listener implements PropertyChangeListener
	{
		private final LayerTreeModel model;
		private final ILayerNode layerNode;
		private final TreeNode treeNode;

		private Map<TreeNode, ILayerNode> childMap = new HashMap<TreeNode, ILayerNode>();

		public Listener(LayerTreeModel model, ILayerNode layerNode, TreeNode treeNode)
		{
			this.model = model;
			this.layerNode = layerNode;
			this.treeNode = treeNode;

			refreshChildren();
			treeNode.addPropertyChangeListener(AVKey.TREE_NODE, this);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			refreshChildren();
		}

		protected void refreshChildren()
		{
			Set<TreeNode> childrenToRemove = new HashSet<TreeNode>(childMap.keySet());

			int index = 0;
			Iterable<TreeNode> treeNodeChildren = treeNode.getChildren();
			for (TreeNode tn : treeNodeChildren)
			{
				if (!childrenToRemove.remove(tn))
				{
					//child doesn't exist in the list, so add it
					TreeNodeLayerNode newChild = new TreeNodeLayerNode(tn, null, false);
					newChild.setTransient(true); //don't save to the layers list
					childMap.put(tn, newChild);
					model.insertNodeInto(newChild, layerNode, index, true);

					WWTreeToLayerTreeConnector.connect(model, newChild, tn); //recurse (kinda)
				}
				index++;
			}

			for (TreeNode childToRemove : childrenToRemove)
			{
				INode nodeToRemove = childMap.get(childToRemove);
				model.removeNodeFromParent(nodeToRemove, true);
				childMap.remove(childToRemove);
			}
		}
	}
}
