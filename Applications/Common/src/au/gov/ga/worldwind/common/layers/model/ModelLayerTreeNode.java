package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.layertree.LayerTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;
import au.gov.ga.worldwind.common.util.FastShape;

public class ModelLayerTreeNode extends LayerTreeNode
{
	public ModelLayerTreeNode(Layer layer)
	{
		super(layer);
	}

	public void addChild(FastShape shape)
	{
		addChild(new FastShapeTreeNode(shape));
	}

	public void removeChild(FastShape shape)
	{
		TreeNode childToRemove = null;
		for (TreeNode node : children)
		{
			if (node instanceof FastShapeTreeNode)
			{
				if (((FastShapeTreeNode) node).getShape() == shape)
				{
					childToRemove = node;
				}
			}
		}
		if (childToRemove != null)
		{
			removeChild(childToRemove);
		}
	}
}
