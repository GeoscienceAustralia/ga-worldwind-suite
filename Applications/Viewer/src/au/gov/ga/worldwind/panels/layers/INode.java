package au.gov.ga.worldwind.panels.layers;

import java.net.URL;

import au.gov.ga.worldwind.panels.dataset.IIconItem;

public interface INode extends IIconItem
{
	public String getName();

	public void setName(String name);

	public URL getDescriptionURL();

	//public void setDescriptionURL(URL descriptionURL);

	public boolean isExpanded();

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void setExpanded(boolean expanded);

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void addChild(INode child);

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void insertChild(int index, INode child);

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void removeChild(INode child);

	public int getChildCount();

	public INode getChild(int index);

	public int getChildIndex(Object child);

	public INode getParent();

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void setParent(INode parent);
}
