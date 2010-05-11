package au.gov.ga.worldwind.panels.layers;

import au.gov.ga.worldwind.dataset.IIconItem;

public interface INode extends IIconItem
{
	public String getName();

	public void setName(String name);

	public boolean isIconLoaded();

	public boolean isExpanded();

	public void setExpanded(boolean expanded);
	
	public boolean isError();
	
	public void setError(boolean error);

	public void addChild(INode child);

	public void insertChild(int index, INode child);

	public void removeChild(INode child);

	public int getChildCount();

	public INode getChild(int index);

	public int getChildIndex(Object child);

	public INode getParent();

	public void setParent(INode parent);
}
