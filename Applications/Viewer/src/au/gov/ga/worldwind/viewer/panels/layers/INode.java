package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.viewer.panels.dataset.IIconItem;

public interface INode extends IIconItem, Cloneable
{
	public String getName();

	public void setName(String name);

	public URL getInfoURL();

	public void setInfoURL(URL infoURL);

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
	
	public List<INode> getChildren();

	public int getChildIndex(Object child);

	public INode getParent();

	/**
	 * Should only be called by LayerTreeModel
	 */
	public void setParent(INode parent);
	
	public INode clone();
	
	boolean isTransient();
	
	void setTransient(boolean t);
}
