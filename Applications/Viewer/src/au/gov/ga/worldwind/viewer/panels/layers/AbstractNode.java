package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.viewer.panels.dataset.AbstractIconItem;

public abstract class AbstractNode extends AbstractIconItem implements INode
{
	private String name;
	private URL infoURL;

	private boolean expanded;
	private INode parent;
	private List<INode> children = new ArrayList<INode>();

	public AbstractNode(String name, URL infoURL, URL iconURL, boolean expanded)
	{
		super(iconURL);
		setName(name);
		setInfoURL(infoURL);
		setExpanded(expanded);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		if (name == null)
			name = "";
		this.name = name;
	}

	@Override
	public URL getInfoURL()
	{
		return infoURL;
	}

	@Override
	public void setInfoURL(URL infoURL)
	{
		this.infoURL = infoURL;
	}

	@Override
	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	@Override
	public void addChild(INode child)
	{
		children.add(child);
		child.setParent(this);
	}

	@Override
	public void insertChild(int index, INode child)
	{
		if (index < 0 || index > getChildCount())
		{
			addChild(child);
			return;
		}
		children.add(index, child);
		child.setParent(this);
	}

	@Override
	public void removeChild(INode child)
	{
		if (children.remove(child))
			child.setParent(null);
	}

	@Override
	public int getChildCount()
	{
		return children.size();
	}

	@Override
	public INode getChild(int index)
	{
		return children.get(index);
	}

	@Override
	public int getChildIndex(Object child)
	{
		return children.indexOf(child);
	}

	@Override
	public INode getParent()
	{
		return parent;
	}

	@Override
	public void setParent(INode parent)
	{
		this.parent = parent;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}