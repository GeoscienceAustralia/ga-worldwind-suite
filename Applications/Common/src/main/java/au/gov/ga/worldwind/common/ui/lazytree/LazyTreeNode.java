package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * {@link JTree} node that supports loading its children lazily when expanded.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class LazyTreeNode extends DefaultMutableTreeNode
{
	private LazyTreeModel model;
	private boolean errorLoading = false;

	public LazyTreeNode(Object userObject, LazyTreeModel model)
	{
		super(userObject);
		this.model = model;
		setAllowsChildren(true);
	}

	protected void setChildren(MutableTreeNode... nodes)
	{
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			model.removeNodeFromParent((MutableTreeNode) getChildAt(0));
		}
		for (int i = 0; nodes != null && i < nodes.length; i++)
		{
			model.insertNodeInto(nodes[i], this, i);
		}
		setAllowsChildren(nodes != null && nodes.length > 0);
	}

	protected void reset()
	{
		setChildren((MutableTreeNode[]) null);
		setAllowsChildren(true);
		setErrorLoading(false);
	}

	protected boolean areChildrenLoaded()
	{
		return getChildCount() > 0 && getAllowsChildren();
	}

	@Override
	public boolean isLeaf()
	{
		return !getAllowsChildren();
	}

	public boolean isErrorLoading()
	{
		return errorLoading;
	}

	public void setErrorLoading(boolean errorLoading)
	{
		this.errorLoading = errorLoading;
	}

	public abstract MutableTreeNode[] loadChildren(LazyTreeModel model) throws Exception;
}
