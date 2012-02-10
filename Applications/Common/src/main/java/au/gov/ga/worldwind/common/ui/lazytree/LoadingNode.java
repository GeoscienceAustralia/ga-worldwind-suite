package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree node that represents a node that is loading. Can be displayed as a child
 * of a lazy tree node while it is loading its actual children.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LoadingNode extends DefaultMutableTreeNode implements ILoadingNode
{
	public LoadingNode(String label)
	{
		super(label, false);
	}

	@Override
	public boolean isLoading()
	{
		return true;
	}
}
