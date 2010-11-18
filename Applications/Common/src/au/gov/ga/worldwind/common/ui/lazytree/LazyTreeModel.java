package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * The model interface to use for {@link LazyTree}s
 */
public interface LazyTreeModel extends TreeModel
{

	void removeNodeFromParent(MutableTreeNode childAt);

	void insertNodeInto(MutableTreeNode mutableTreeNode, MutableTreeNode lazyTreeNode, int i);

}
