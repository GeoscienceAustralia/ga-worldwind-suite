package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;

/**
 * Super interface for all nodes in the dataset tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IData extends IIconItem
{
	/**
	 * @return The name (label) of this node
	 */
	String getName();

	/**
	 * @return The URL to the information/metadata for this node
	 */
	URL getInfoURL();

	/**
	 * @return Is this node a base node in the hierarchy of its children? When
	 *         adding layers from the dataset panel to the layers panel, the
	 *         parent hierarchy is also added, up until a base node.
	 */
	boolean isBase();

	/**
	 * Create a {@link MutableTreeNode} for this node in the provided tree
	 * model.
	 * 
	 * @param model
	 * @return New {@link MutableTreeNode} associated with this node.
	 */
	MutableTreeNode createMutableTreeNode(LazyTreeModel model);
}
