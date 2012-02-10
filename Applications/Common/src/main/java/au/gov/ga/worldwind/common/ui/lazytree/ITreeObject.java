package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.MutableTreeNode;

/**
 * Represents any object that can be displayed in the {@link LazyTree}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITreeObject
{
	/**
	 * @param model
	 * @return The list of child tree nodes of this object.
	 */
	public MutableTreeNode[] getChildren(LazyTreeModel model);
}
