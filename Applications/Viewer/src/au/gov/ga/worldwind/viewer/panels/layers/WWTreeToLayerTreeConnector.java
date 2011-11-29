package au.gov.ga.worldwind.viewer.panels.layers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.tree.TreeNode;

public class WWTreeToLayerTreeConnector
{
	public static void connect(LayerTreeModel model, ILayerNode layerNode, TreeNode treeNode)
	{
		new Listener(model, layerNode, treeNode);
	}

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
