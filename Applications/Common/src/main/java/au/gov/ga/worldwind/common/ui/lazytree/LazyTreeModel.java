package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * The model interface to use for {@link LazyTree}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface LazyTreeModel extends TreeModel
{
	/**
	 * Remove the given tree node from it's parent.
	 * 
	 * @param childAt
	 *            Child to remove
	 */
	void removeNodeFromParent(MutableTreeNode childAt);

	/**
	 * Insert the given tree node into the given parent's children at the
	 * specified index.
	 * 
	 * @param newChild
	 *            Child to add
	 * @param parent
	 *            Parent to add child to
	 * @param index
	 *            Index at which to insert
	 */
	void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index);
}
