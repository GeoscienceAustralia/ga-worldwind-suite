package au.gov.ga.worldwind.components.collapsiblesplit;

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
	private final List<Placeholder> placeholders = new ArrayList<Placeholder>();
	private final Map<String, Placeholder> placeholderMap = new HashMap<String, Placeholder>();
	private final Map<String, Component> componentMap = new HashMap<String, Component>();
	private final Map<Component, String> reverseComponentMap = new HashMap<Component, String>();

	private int dividerSize;
	private boolean vertical;

	public void addPlaceholder(String name, float weight)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"placeholder name cannot be null");
		if (placeholderMap.containsKey(name))
			throw new IllegalArgumentException("placeholder already added: "
					+ name);

		//check if there is already a collapsible component for this name; if there is,
		//use it's collapsed state for the expanded property, otherwise default to true
		boolean expanded = true;
		if (componentMap.containsKey(name))
		{
			Component c = componentMap.get(name);
			if (c instanceof ICollapsible)
			{
				expanded = !((ICollapsible) c).isCollapsed();
			}
		}

		Placeholder placeholder = new Placeholder();
		placeholder.name = name;
		placeholder.weight = weight;
		placeholder.expanded = expanded;
		placeholders.add(placeholder);
		placeholderMap.put(name, placeholder);
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints)
	{
		if (constraints instanceof String)
			addLayoutComponent((String) constraints, comp);
		else
			throw new IllegalArgumentException(
					"cannot add to layout: constraint must be a string");
	}

	@Override
	public void addLayoutComponent(String name, Component comp)
	{
		if (name == null)
			throw new IllegalArgumentException(
					"cannot add to layout: name not specified");
		if (comp == null)
			throw new IllegalArgumentException(
					"cannot add to layout: component is null");

		if (componentMap.containsKey(name) && componentMap.get(name) != comp)
			throw new IllegalArgumentException(
					"cannot add to layout: name is not unique");
		if (reverseComponentMap.containsKey(comp)
				&& !reverseComponentMap.get(comp).equals(name))
			throw new IllegalArgumentException(
					"cannot add to layout: component is not unique");

		componentMap.put(name, comp);
		reverseComponentMap.put(comp, name);

		if (comp instanceof ICollapsible)
		{
			if (isPlaceholderNameValid(name))
			{
				setExpanded(name, !((ICollapsible) comp).isCollapsed());
			}
		}
	}

	@Override
	public void removeLayoutComponent(Component comp)
	{
		if (reverseComponentMap.containsKey(comp))
		{
			String name = reverseComponentMap.get(comp);
			reverseComponentMap.remove(comp);
			componentMap.remove(name);
		}
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
		for (Placeholder placeholder : placeholders)
		{
			if (placeholderHasComponent(placeholder))
			{
				if (anyAdded)
				{
					if (isVertical())
						height += getDividerSize();
					else
						width += getDividerSize();
				}
				Component component = placeholderComponent(placeholder);
				Dimension size = minimum ? component.getMinimumSize()
						: component.getPreferredSize();
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
			available = bounds.height - minimumSize.height - insets.top
					- insets.bottom;
			otherDimension = bounds.width - insets.left - insets.right;
		}
		else
		{
			available = bounds.width - minimumSize.width - insets.left
					- insets.right;
			otherDimension = bounds.height - insets.top - insets.bottom;
		}

		Placeholder lastPlaceholder = null;
		int i = 0;
		for (Placeholder placeholder : placeholders)
		{
			if (placeholderHasComponent(placeholder))
			{
				Component component = placeholderComponent(placeholder);
				Dimension minimum = component.getMinimumSize();
				Rectangle b = new Rectangle(x, y, 0, 0);
				int extraSpace;

				if (isVertical())
				{
					b.width = otherDimension;
					extraSpace = Math.round(available
							* placeholder.getExpandedWeight()
							/ totalExpandedWeight);
					b.height = minimum.height + extraSpace;
					y += b.height;
				}
				else
				{
					b.height = otherDimension;
					extraSpace = Math.round(available
							* placeholder.getExpandedWeight()
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
				i++;
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
		for (Placeholder placeholder : placeholders)
		{
			if (placeholderHasComponent(placeholder))
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
		if (reverseComponentMap.containsKey(component))
		{
			String name = reverseComponentMap.get(component);
			if (isPlaceholderNameValid(name))
			{
				int index = placeholders.indexOf(placeholderMap.get(name));
				for (int i = index - 1; i >= 0; i--)
				{
					if (placeholderHasComponent(placeholders.get(i)))
					{
						return i;
					}
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
		Placeholder previousExpanded = null, nextExpanded = null;

		for (int i = 0; i < placeholders.size(); i++)
		{
			Placeholder placeholder = placeholders.get(i);
			if (placeholder.expanded && placeholderHasComponent(placeholder))
			{
				if (i <= index)
				{
					totalSpaceBefore += placeholder.extraComponentSpace;
					totalWeightBefore += placeholder.weight;
					totalExpandedBefore++;
					previousExpanded = placeholder;
				}
				else
				{
					totalSpaceAfter += placeholder.extraComponentSpace;
					totalWeightAfter += placeholder.weight;
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

		float newTotalWeightBefore = (totalSpaceBefore + movement)
				* weightTotal / availableRoom;
		float newTotalWeightAfter = (totalSpaceAfter - movement) * weightTotal
				/ availableRoom;

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
			previousExpanded.weight += deltaBefore;
		}
		else
		{
			deltaBefore = -deltaBefore;
			for (int i = index; i >= 0; i--)
			{
				Placeholder placeholder = placeholders.get(i);
				if (placeholder.expanded
						&& placeholderHasComponent(placeholder))
				{
					if (placeholder.weight >= deltaBefore)
					{
						placeholder.weight -= deltaBefore;
						break;
					}
					else
					{
						deltaBefore -= placeholder.weight;
						placeholder.weight = 0;
					}
				}
			}
		}

		//distribute deltaAfter among all next placeholders
		float deltaAfter = newTotalWeightAfter - totalWeightAfter;
		if (deltaAfter >= 0)
		{
			nextExpanded.weight += deltaAfter;
		}
		else
		{
			deltaAfter = -deltaAfter;
			for (int i = index + 1; i < placeholders.size(); i++)
			{
				Placeholder placeholder = placeholders.get(i);
				if (placeholder.expanded
						&& placeholderHasComponent(placeholder))
				{
					if (placeholder.weight >= deltaAfter)
					{
						placeholder.weight -= deltaAfter;
						break;
					}
					else
					{
						deltaAfter -= placeholder.weight;
						placeholder.weight = 0;
					}
				}
			}
		}


		x = isVertical() ? current.x : newPosition;
		y = isVertical() ? newPosition : current.y;
		return new Point(x, y);
	}


		/*Placeholder previous = previousExpandedPlaceholder(index, true);
		Placeholder next = nextExpandedPlaceholder(index, false);
		if (previous == null || next == null)
			return null;

		Rectangle current = dividerBounds(index);
		float weightTotal = previous.weight + next.weight;
		if (weightTotal == 0)
			return current.getLocation();

		int availableRoom = previous.extraComponentSpace
				+ next.extraComponentSpace;

		int currentPosition = isVertical() ? current.y : current.x;
		int newPosition = isVertical() ? y : x;
		int movement = newPosition - currentPosition;

		float newPreviousWeight = (previous.extraComponentSpace + movement)
				* weightTotal / availableRoom;
		float newNextWeight = (next.extraComponentSpace - movement)
				* weightTotal / availableRoom;

		if (newPreviousWeight < 0)
		{
			newPreviousWeight = 0;
			newNextWeight = weightTotal;
			newPosition = currentPosition - previous.extraComponentSpace;
		}
		else if (newNextWeight < 0)
		{
			newPreviousWeight = weightTotal;
			newNextWeight = 0;
			newPosition = currentPosition + next.extraComponentSpace;
		}

		//set the new weights
		previous.weight = newPreviousWeight;
		next.weight = newNextWeight;

		x = isVertical() ? current.x : newPosition;
		y = isVertical() ? newPosition : current.y;
		return new Point(x, y);
	}

	private Placeholder previousExpandedPlaceholder(int index,
			boolean includeCurrent)
	{
		if (-1 < index && index < placeholders.size())
		{
			if (!includeCurrent)
				index--;
			for (int i = index; i >= 0; i--)
			{
				Placeholder placeholder = placeholders.get(i);
				if (placeholder.expanded
						&& placeholderHasComponent(placeholder))
					return placeholder;
			}
		}
		return null;
	}

	private Placeholder nextExpandedPlaceholder(int index,
			boolean includeCurrent)
	{
		if (-1 < index && index < placeholders.size())
		{
			if (!includeCurrent)
				index++;
			for (int i = index; i < placeholders.size(); i++)
			{
				Placeholder placeholder = placeholders.get(i);
				if (placeholder.expanded
						&& placeholderHasComponent(placeholder))
					return placeholder;
			}
		}
		return null;
	}*/

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

	public float getWeight(String placeholderName)
	{
		if (!isPlaceholderNameValid(placeholderName))
			throw new IllegalArgumentException("placeholder not found");
		return placeholderMap.get(placeholderName).weight;
	}

	public void setWeight(String placeholderName, float weight)
	{
		if (!isPlaceholderNameValid(placeholderName))
			throw new IllegalArgumentException("placeholder not found");
		weight = Math.min(0, weight);
		placeholderMap.get(placeholderName).weight = weight;
	}

	public boolean isExpanded(String placeholderName)
	{
		if (!isPlaceholderNameValid(placeholderName))
			throw new IllegalArgumentException("placeholder not found");
		return placeholderMap.get(placeholderName).expanded;
	}

	public void setExpanded(String placeholderName, boolean expanded)
	{
		if (!isPlaceholderNameValid(placeholderName))
			throw new IllegalArgumentException("placeholder not found");
		placeholderMap.get(placeholderName).expanded = expanded;
	}

	private boolean isPlaceholderNameValid(String placeholderName)
	{
		return placeholderMap.containsKey(placeholderName);
	}

	public String getPlaceholderName(Component component)
	{
		if (reverseComponentMap.containsKey(component))
		{
			String name = reverseComponentMap.get(component);
			if (isPlaceholderNameValid(name))
				return name;
		}
		return null;
	}

	private float totalExpandedWeight()
	{
		float expandedWeight = 0f;
		for (Placeholder placeholder : placeholders)
			if (placeholderHasComponent(placeholder))
				expandedWeight += placeholder.getExpandedWeight();
		if (expandedWeight < 0f)
			expandedWeight = 0f;
		return expandedWeight;
	}

	private void redistributeTotalWeight()
	{
		float totalWeight = 0f;
		int countExpanded = 0;
		for (Placeholder placeholder : placeholders)
		{
			if (placeholderHasComponent(placeholder))
			{
				totalWeight += placeholder.weight;
				if (placeholder.expanded)
					countExpanded++;
			}
		}
		if (countExpanded == 0)
			return;
		if (totalWeight < 0f)
			totalWeight = 1f;
		for (Placeholder placeholder : placeholders)
		{
			if (placeholderHasComponent(placeholder) && placeholder.expanded)
			{
				placeholder.weight = totalWeight / countExpanded;
			}
		}
	}

	private boolean placeholderHasComponent(Placeholder placeholder)
	{
		return componentMap.containsKey(placeholder.name);
	}

	private Component placeholderComponent(Placeholder placeholder)
	{
		return componentMap.get(placeholder.name);
	}

	private static class Placeholder
	{
		public String name;
		public float weight;
		public boolean expanded;
		public Rectangle nextDividerBounds;
		public int extraComponentSpace;

		public float getExpandedWeight()
		{
			if (expanded)
				return weight;
			return 0f;
		}
	}
}
