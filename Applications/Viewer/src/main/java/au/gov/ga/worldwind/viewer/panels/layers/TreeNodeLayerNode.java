package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.util.tree.TreeNode;

import java.net.URL;

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

	public TreeNode getTreeNode()
	{
		return treeNode;
	}
}
