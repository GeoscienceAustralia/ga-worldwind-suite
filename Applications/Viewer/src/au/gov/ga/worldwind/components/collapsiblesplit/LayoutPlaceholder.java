package au.gov.ga.worldwind.components.collapsiblesplit;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class LayoutPlaceholder
{
	public final Component component;

	private float weight;
	private boolean expanded;
	private boolean resizable;

	private List<CollapsibleSplitListener> listeners = new ArrayList<CollapsibleSplitListener>(1);

	//package private
	Rectangle nextDividerBounds;
	int extraComponentSpace;

	public LayoutPlaceholder(Component component, float weight, boolean expanded, boolean resizable)
	{
		this.component = component;
		this.weight = weight;
		this.expanded = expanded;
		this.resizable = resizable;
	}

	public float getExpandedWeight()
	{
		if (takesExtraSpace())
			return weight;
		return 0f;
	}

	public boolean takesExtraSpace()
	{
		return resizable && expanded;
	}

	public void addListener(CollapsibleSplitListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(CollapsibleSplitListener listener)
	{
		listeners.remove(listener);
	}

	public float getWeight()
	{
		return weight;
	}

	public void setWeight(float weight)
	{
		if (this.weight != weight)
		{
			this.weight = weight;
			for (CollapsibleSplitListener listener : listeners)
				listener.weightChanged(weight);
		}
	}

	public void addToWeight(float amount)
	{
		setWeight(getWeight() + amount);
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		if (this.expanded != expanded)
		{
			this.expanded = expanded;
			for (CollapsibleSplitListener listener : listeners)
				listener.expandedToggled(expanded);
		}
	}

	public boolean isResizable()
	{
		return resizable;
	}

	public void setResizable(boolean resizable)
	{
		if (this.resizable != resizable)
		{
			this.resizable = resizable;
			for (CollapsibleSplitListener listener : listeners)
				listener.resizableToggled(resizable);
		}
	}
}
