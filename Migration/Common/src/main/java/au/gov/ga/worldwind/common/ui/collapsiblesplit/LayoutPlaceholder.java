/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.common.ui.collapsiblesplit;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Layout placeholder used by the {@link CollapsibleSplitLayout}. Stores
 * collapse information required for each component in the layout.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	/**
	 * @return Weight of this component if it's resizable and expanded,
	 *         otherwise 0.
	 */
	public float getExpandedWeight()
	{
		if (takesExtraSpace())
			return weight;
		return 0f;
	}

	/**
	 * @return Does this component take extra space when being resized? This is
	 *         only the case if it is both resizable and currently expanded.
	 */
	public boolean takesExtraSpace()
	{
		return resizable && expanded;
	}

	/**
	 * Add a listener for collapse events.
	 * 
	 * @param listener
	 */
	public void addListener(CollapsibleSplitListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener for collapse events.
	 * 
	 * @param listener
	 */
	public void removeListener(CollapsibleSplitListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @return Resize weight for the associated component in the layout. This
	 *         controls how much extra width/height is assigned to this
	 *         component when other neighbouring components are being resized.
	 */
	public float getWeight()
	{
		return weight;
	}

	/**
	 * Set the resize weight for the associated component in the layout.
	 * 
	 * @param weight
	 */
	public void setWeight(float weight)
	{
		if (this.weight != weight)
		{
			this.weight = weight;
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).weightChanged(weight);
		}
	}

	/**
	 * Add the given amount to the resize weight.
	 * 
	 * @param amount
	 */
	public void addToWeight(float amount)
	{
		setWeight(getWeight() + amount);
	}

	/**
	 * @return Is this layout component expanded?
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Expand/collapse this layout component.
	 * 
	 * @param expanded
	 */
	public void setExpanded(boolean expanded)
	{
		if (this.expanded != expanded)
		{
			this.expanded = expanded;
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).expandedToggled(expanded);
		}
	}

	/**
	 * @return Is this layout component resizable?
	 */
	public boolean isResizable()
	{
		return resizable;
	}

	/**
	 * Enable/disable resizing this layout component.
	 * 
	 * @param resizable
	 */
	public void setResizable(boolean resizable)
	{
		if (this.resizable != resizable)
		{
			this.resizable = resizable;
			for (int i = listeners.size() - 1; i >= 0; i--)
				listeners.get(i).resizableToggled(resizable);
		}
	}
}
