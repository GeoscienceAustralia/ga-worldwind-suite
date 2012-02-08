package au.gov.ga.worldwind.common.layers.model;

import au.gov.ga.worldwind.common.util.FastShape;
import gov.nasa.worldwind.util.tree.BasicTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;

/**
 * {@link TreeNode} used to enable/disable a FastShape. Used for displaying
 * {@link FastShape}s associated with a {@link ModelLayer} in a hierarchical
 * tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FastShapeTreeNode extends BasicTreeNode
{
	private final FastShape shape;

	public FastShapeTreeNode(FastShape shape)
	{
		super(shape.getName());
		this.shape = shape;
	}

	@Override
	public boolean isSelected()
	{
		return shape.isEnabled();
	}

	@Override
	public void setSelected(boolean selected)
	{
		super.setSelected(selected);
		shape.setEnabled(selected);
	}

	public FastShape getShape()
	{
		return shape;
	}
}
