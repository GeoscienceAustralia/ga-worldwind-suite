package au.gov.ga.worldwind.dataset.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.dataset.AbstractIconItem;

public abstract class AbstractNode extends AbstractIconItem implements INode
{
	private String name;

	private boolean expanded;
	private INode parent;
	private List<INode> children = new ArrayList<INode>();
	private boolean error;

	public AbstractNode(String name, URL iconURL, boolean expanded)
	{
		super(iconURL);
		setName(name);
		setExpanded(expanded);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		if (name == null)
			name = "";
		this.name = name;
	}

	public boolean isError()
	{
		return error;
	}

	public void setError(boolean error)
	{
		this.error = error;
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	public void addChild(INode child)
	{
		children.add(child);
		child.setParent(this);
	}

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

	public void removeChild(INode child)
	{
		if (children.remove(child))
			child.setParent(null);
	}

	public int getChildCount()
	{
		return children.size();
	}

	public INode getChild(int index)
	{
		return children.get(index);
	}

	public int getChildIndex(Object child)
	{
		return children.indexOf(child);
	}

	public INode getParent()
	{
		return parent;
	}

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