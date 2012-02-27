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

/**
 * Represents a collapsible component.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ICollapsible
{
	/**
	 * Add a listener for collapse events to this object.
	 * 
	 * @param listener
	 */
	public void addCollapseListener(CollapseListener listener);

	/**
	 * Remove a listener for collapse events from this object.
	 * 
	 * @param listener
	 */
	public void removeCollapseListener(CollapseListener listener);

	/**
	 * @return Is this component is collapsed?
	 */
	public boolean isCollapsed();

	/**
	 * Collapse/expand this component.
	 * 
	 * @param collapsed
	 */
	public void setCollapsed(boolean collapsed);
}
