package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Default implementation of the {@link LazyTreeModel} interface
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLazyTreeModel extends DefaultTreeModel implements LazyTreeModel
{
	public DefaultLazyTreeModel(TreeNode root)
	{
		super(root);
	}
}
