package au.gov.ga.worldwind.common.ui.collapsiblesplit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollapsibleSplitLayout implements LayoutManager2
{
	private final List<LayoutPlaceholder> placeholders = new ArrayList<LayoutPlaceholder>();
	private final Map<Component, LayoutPlaceholder> componentMap =
			new HashMap<Component, LayoutPlaceholder>();

	private int dividerSize = 5;
	private boolean vertical = true;

	@Override
	public void addLayoutComponent(Component comp, Object constraints)
	{
		if (!(constraints instanceof CollapsibleSplitConstraints))
			throw new IllegalArgumentException(
					"cannot add to layout: constraints must be an instanceof "
							+ CollapsibleSplitConstraints.class.getSimpleName());

		CollapsibleSplitConstraints c = (CollapsibleSplitConstraints) constraints;

		//ensure the component matches the expanded state
		if (comp instanceof ICollapsible)
		{
			ICollapsible collapsible = (ICollapsible) comp;
			if (collapsible.isCollapsed() == c.expanded)
				collapsible.setCollapsed(!c.expanded);
		}

		LayoutPlaceholder placeholder =
				new LayoutPlaceholder(comp, c.weight, c.expanded, c.resizable);
		placeholders.add(placeholder);
		componentMap.put(comp, placeholder);
	}

	@Override
	public void addLayoutComponent(String name, Component comp)
	{
	}

	@Override
	public void removeLayoutComponent(Component comp)
	{
		if (componentMap.containsKey(comp))
		{
			LayoutPlaceholder placeholder = componentMap.get(comp);
			placeholders.remove(placeholder);
			componentMap.remove(comp);
		}
	}

	public void addListener(Component comp, CollapsibleSplitListener listener)
	{
		if (componentMap.containsKey(comp))
			componentMap.get(comp).addListener(listener);
	}

	public void removeListener(Component comp, CollapsibleSplitListener listener)
	{
		if (componentMap.containsKey(comp))
			componentMap.get(comp).removeListener(listener);
	}

	@Override
	public float getLayoutAlignmentX(Container target)
	{
		return 0.5f;
	}

	@Override
	public float getLayoutAlignmentY(Container target)
	{
		return 0.5f;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return layoutSize(parent, true);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return layoutSize(parent, false);
	}

	private Dimension layoutSize(Container parent, boolean minimum)
	{
		int width = 0;
		int height = 0;

		boolean anyAdded = false;
		for (LayoutPlaceholder placeholder : placeholders)
		{
			if (componentIsVisible(placeholder))
			{
				if (anyAdded)
				{
					if (isVertical())
						height += getDividerSize();
					else
						width += getDividerSize();
				}
				Component component = placeholder.component;
				Dimension size =
						minimum ? component.getMinimumSize() : component.getPreferredSize();
				if (isVertical())
				{
					width = Math.max(width, size.width);
					height += size.height;
				}
				else
				{
					width += size.width;
					height = Math.max(height, size.height);
				}
				anyAdded = true;
			}
		}

		Insets insets = parent.getInsets();
		width += insets.left + insets.right;
		height += insets.top + insets.bottom;
		return new Dimension(width, height);
	}

	@Override
	public Dimension maximumLayoutSize(Container target)
	{
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void layoutContainer(Container parent)
	{
		Rectangle bounds = parent.getBounds();
		Insets insets = parent.getInsets();
		float totalExpandedWeight = totalExpandedWeight();
		Dimension minimumSize = minimumLayoutSize(parent); //includes all component AND dividers
		int x = bounds.x + insets.left;
		int y = bounds.y + insets.top;
		int available, otherDimension;

		//if all expanded panels have no weight, then redistribute the total weight
		//evenly amongst the expanded panels
		if (totalExpandedWeight <= 0)
			redistributeTotalWeight();
		totalExpandedWeight = totalExpandedWeight();
		//if we still don't have any expanded panels with weight, all panels must be collapsed
		if (totalExpandedWeight <= 0) //fix so we don't get a divide by zero exception
			totalExpandedWeight = 1f;

		if (isVertical())
		{
			available = bounds.height - minimumSize.height - insets.top - insets.bottom;
			otherDimension = bounds.width - insets.left - insets.right;
		}
		else
		{
			available = bounds.width - minimumSize.width - insets.left - insets.right;
			otherDimension = bounds.height - insets.top - insets.bottom;
		}

		LayoutPlaceholder lastPlaceholder = null;
		for (LayoutPlaceholder placeholder : placeholders)
		{
			if (componentIsVisible(placeholder))
			{
				Component component = placeholder.component;
				Dimension minimum = component.getMinimumSize();
				Rectangle b = new Rectangle(x, y, 0, 0);
				int extraSpace;

				if (isVertical())
				{
					b.width = otherDimension;
					extraSpace =
							Math.round(available * placeholder.getExpandedWeight()
									/ totalExpandedWeight);
					b.height = minimum.height + extraSpace;
					y += b.height;
				}
				else
				{
					b.height = otherDimension;
					extraSpace =
							Math.round(available * placeholder.getExpandedWeight()
									/ totalExpandedWeight);
					b.width = minimum.width + extraSpace;
					x += b.width;
				}
				component.setBounds(b);
				placeholder.extraComponentSpace = extraSpace;

				b = new Rectangle(x, y, 0, 0);
				if (isVertical())
				{
					b.x = bounds.x;
					b.width = bounds.width;
					b.height = getDividerSize();
					y += b.height;
				}
				else
				{
					b.y = bounds.y;
					b.height = bounds.height;
					b.width = getDividerSize();
					x += b.width;
				}
				placeholder.nextDividerBounds = b;

				lastPlaceholder = placeholder;
			}
		}

		if (lastPlaceholder != null)
			lastPlaceholder.nextDividerBounds = null;
	}

	@Override
	public void invalidateLayout(Container target)
	{
	}

	public int dividerAt(int x, int y)
	{
		int i = 0;
		for (LayoutPlaceholder placeholder : placeholders)
		{
			if (componentIsVisible(placeholder))
			{
				if (placeholder.nextDividerBounds != null
						&& placeholder.nextDividerBounds.contains(x, y))
					return i;
			}
			i++;
		}
		return -1;
	}

	public int dividerAboveComponentBounds(Component component)
	{
		if (componentMap.containsKey(component))
		{
			LayoutPlaceholder placeholder = componentMap.get(component);
			int index = placeholders.indexOf(placeholder);
			for (int i = index - 1; i >= 0; i--)
			{
				if (componentIsVisible(placeholders.get(i)))
				{
					return i;
				}
			}
		}
		return -1;
	}

	public Rectangle dividerBounds(int index)
	{
		if (-1 < index && index < placeholders.size())
			return placeholders.get(index).nextDividerBounds;
		return null;
	}

	public Point setDividerPosition(int index, int x, int y)
	{
		if (index < 0 || index >= placeholders.size())
			return null;

		int totalExpandedBefore = 0, totalExpandedAfter = 0;
		int totalSpaceBefore = 0, totalSpaceAfter = 0;
		float totalWeightBefore = 0f, totalWeightAfter = 0f;
		LayoutPlaceholder previousExpanded = null, nextExpanded = null;

		for (int i = 0; i < placeholders.size(); i++)
		{
			LayoutPlaceholder placeholder = placeholders.get(i);
			if (placeholder.takesExtraSpace() && componentIsVisible(placeholder))
			{
				if (i <= index)
				{
					totalSpaceBefore += placeholder.extraComponentSpace;
					totalWeightBefore += placeholder.getWeight();
					totalExpandedBefore++;
					previousExpanded = placeholder;
				}
				else
				{
					totalSpaceAfter += placeholder.extraComponentSpace;
					totalWeightAfter += placeholder.getWeight();
					totalExpandedAfter++;
					if (nextExpanded == null)
						nextExpanded = placeholder;
				}
			}
		}

		int availableRoom = totalSpaceBefore + totalSpaceAfter;
		float weightTotal = totalWeightBefore + totalWeightAfter;
		Rectangle current = dividerBounds(index);

		if (availableRoom <= 0 || weightTotal <= 0 || totalExpandedBefore == 0
				|| totalExpandedAfter == 0)
			return current.getLocation();

		int currentPosition = isVertical() ? current.y : current.x;
		int newPosition = isVertical() ? y : x;
		int movement = newPosition - currentPosition;

		float newTotalWeightBefore = (totalSpaceBefore + movement) * weightTotal / availableRoom;
		float newTotalWeightAfter = (totalSpaceAfter - movement) * weightTotal / availableRoom;

		if (newTotalWeightBefore < 0)
		{
			newTotalWeightBefore = 0;
			newTotalWeightAfter = weightTotal;
			newPosition = currentPosition - totalSpaceBefore;
		}
		else if (newTotalWeightAfter < 0)
		{
			newTotalWeightBefore = weightTotal;
			newTotalWeightAfter = 0;
			newPosition = currentPosition + totalSpaceAfter;
		}

		//distribute deltaBefore among all previous placeholders
		float deltaBefore = newTotalWeightBefore - totalWeightBefore;
		if (deltaBefore >= 0)
		{
			previousExpanded.addToWeight(deltaBefore);
		}
		else
		{
			deltaBefore = -deltaBefore;
			for (int i = index; i >= 0; i--)
			{
				LayoutPlaceholder placeholder = placeholders.get(i);
				if (placeholder.takesExtraSpace() && componentIsVisible(placeholder))
				{
					if (placeholder.getWeight() >= deltaBefore)
					{
						placeholder.addToWeight(-deltaBefore);
						break;
					}
					else
					{
						deltaBefore -= placeholder.getWeight();
						placeholder.setWeight(0);
					}
				}
			}
		}

		//distribute deltaAfter among all next placeholders
		float deltaAfter = newTotalWeightAfter - totalWeightAfter;
		if (deltaAfter >= 0)
		{
			nextExpanded.addToWeight(deltaAfter);
		}
		else
		{
			deltaAfter = -deltaAfter;
			for (int i = index + 1; i < placeholders.size(); i++)
			{
				LayoutPlaceholder placeholder = placeholders.get(i);
				if (placeholder.takesExtraSpace() && componentIsVisible(placeholder))
				{
					if (placeholder.getWeight() >= deltaAfter)
					{
						placeholder.addToWeight(-deltaAfter);
						break;
					}
					else
					{
						deltaAfter -= placeholder.getWeight();
						placeholder.setWeight(0);
					}
				}
			}
		}


		x = isVertical() ? current.x : newPosition;
		y = isVertical() ? newPosition : current.y;
		return new Point(x, y);
	}

	public int getDividerSize()
	{
		return dividerSize;
	}

	public void setDividerSize(int dividerSize)
	{
		this.dividerSize = dividerSize;
	}

	public boolean isVertical()
	{
		return vertical;
	}

	public void setVertical(boolean vertical)
	{
		this.vertical = vertical;
	}

	public float getWeight(Component comp)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		return componentMap.get(comp).getWeight();
	}

	public void setWeight(Component comp, float weight)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		componentMap.get(comp).setWeight(Math.max(0f, weight)); //weight cannot be negative
	}

	public boolean isExpanded(Component comp)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		return componentMap.get(comp).isExpanded();
	}

	public void setExpanded(Component comp, boolean expanded)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		componentMap.get(comp).setExpanded(expanded);
	}

	public boolean isResizable(Component comp)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		return componentMap.get(comp).isResizable();
	}

	public void setResizable(Component comp, boolean resizable)
	{
		if (!componentMap.containsKey(comp))
			throw new IllegalArgumentException("component not found");
		componentMap.get(comp).setResizable(resizable);
	}

	private float totalExpandedWeight()
	{
		float expandedWeight = 0f;
		for (LayoutPlaceholder placeholder : placeholders)
			if (componentIsVisible(placeholder))
				expandedWeight += placeholder.getExpandedWeight();
		if (expandedWeight < 0f)
			expandedWeight = 0f;
		return expandedWeight;
	}

	private void redistributeTotalWeight()
	{
		float totalWeight = 0f;
		int countExpanded = 0;
		for (LayoutPlaceholder placeholder : placeholders)
		{
			if (componentIsVisible(placeholder))
			{
				if (placeholder.isResizable())
					totalWeight += placeholder.getWeight();
				if (placeholder.takesExtraSpace())
					countExpanded++;
			}
		}
		if (countExpanded == 0)
			return;
		if (totalWeight < 0f)
			totalWeight = 1f;
		for (LayoutPlaceholder placeholder : placeholders)
		{
			if (componentIsVisible(placeholder) && placeholder.takesExtraSpace())
			{
				placeholder.setWeight(totalWeight / countExpanded);
			}
		}
	}

	private boolean componentIsVisible(LayoutPlaceholder placeholder)
	{
		return placeholder.component.isVisible();
	}
}
