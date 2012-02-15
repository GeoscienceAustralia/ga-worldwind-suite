package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.viewer.panels.dataset.IIconItem;

/**
 * Superinterface of all layer tree nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface INode extends IIconItem, Cloneable
{
	/**
	 * @return Name (label) of this node.
	 */
	String getName();

	/**
	 * Set the name of this node
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * @return URL which contains the information/metadata for this node
	 */
	URL getInfoURL();

	/**
	 * Set the information URL for this node
	 * 
	 * @param infoURL
	 */
	void setInfoURL(URL infoURL);

	/**
	 * @return Is this node expanded (if it has children)?
	 */
	boolean isExpanded();

	/**
	 * Set this node's expanded state. Should only be called by
	 * {@link LayerTreeModel}.
	 * 
	 * @param expanded
	 */
	void setExpanded(boolean expanded);

	/**
	 * Add a child to this node. Should only be called by {@link LayerTreeModel}
	 * .
	 * 
	 * @param child
	 */
	void addChild(INode child);

	/**
	 * Insert a child at the given index to this node. Should only be called by
	 * {@link LayerTreeModel}.
	 * 
	 * @param index
	 * @param child
	 */
	void insertChild(int index, INode child);

	/**
	 * Remove a child from this node. Should only be called by
	 * {@link LayerTreeModel}.
	 * 
	 * @param child
	 */
	void removeChild(INode child);

	/**
	 * @return Count of this node's children
	 */
	int getChildCount();

	/**
	 * @param index
	 *            Index of the child to get
	 * @return Child at the given index
	 */
	INode getChild(int index);

	/**
	 * @return A list of this node's children
	 */
	List<INode> getChildren();

	/**
	 * Find the index of the given child in this node's child list.
	 * 
	 * @param child
	 * @return Index of child
	 */
	int getChildIndex(Object child);

	/**
	 * @return Parent node of this node
	 */
	INode getParent();

	/**
	 * Set the parent of this node. Should only be called by
	 * {@link LayerTreeModel}.
	 * 
	 * @param parent
	 */
	void setParent(INode parent);

	/**
	 * @return Copy of this node
	 */
	INode clone();

	/**
	 * @return True if this node is transient. Transient nodes are not saved to
	 *         the user's layer list persistance file.
	 */
	boolean isTransient();

	/**
	 * Mark this node as transient.
	 * 
	 * @param t
	 * @see INode#isTransient()
	 */
	void setTransient(boolean t);
}
