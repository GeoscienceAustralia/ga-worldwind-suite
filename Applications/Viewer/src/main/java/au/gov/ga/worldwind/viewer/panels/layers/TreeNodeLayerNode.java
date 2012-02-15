package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.util.tree.TreeNode;

import java.net.URL;

/**
 * Subclass of {@link LayerNode} that connects the 'enable' getter/setter to a
 * {@link TreeNode}'s 'selected' getter/setter. This allows adding a World Wind
 * {@link TreeNode} to a layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TreeNodeLayerNode extends LayerNode
{
	private final TreeNode treeNode;

	public TreeNodeLayerNode(TreeNode treeNode, URL iconURL, boolean expanded)
	{
		super(treeNode.getText(), null, iconURL, expanded, null, treeNode.isSelected(), 1.0, null);
		this.treeNode = treeNode;
	}

	@Override
	public boolean isEnabled()
	{
		if (treeNode == null) //null in superconstructor
			return super.isEnabled();
		return treeNode.isSelected();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if (treeNode == null) //null in superconstructor
			super.setEnabled(enabled);
		else
			treeNode.setSelected(enabled);
	}

	/**
	 * @return The tree node associated with this layer node.
	 */
	public TreeNode getTreeNode()
	{
		return treeNode;
	}
}
