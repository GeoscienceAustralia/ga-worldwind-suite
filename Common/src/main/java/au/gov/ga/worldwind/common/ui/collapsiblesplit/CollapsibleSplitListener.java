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
 * The listener interface for receiving events from the component
 * {@link LayoutPlaceholder}s in the {@link CollapsibleSplitLayout}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CollapsibleSplitListener
{
	/**
	 * Called when the component's resize weight changes.
	 * 
	 * @param weight
	 */
	public void weightChanged(float weight);

	/**
	 * Called when the component's resizable property is toggled.
	 * 
	 * @param resizable
	 */
	public void resizableToggled(boolean resizable);

	/**
	 * Called when the component is expanded or collapsed.
	 * 
	 * @param expanded
	 */
	public void expandedToggled(boolean expanded);

	/**
	 * Blank implementation of the {@link CollapsibleSplitListener} interface.
	 */
	public class CollapsibleSplitAdapter implements CollapsibleSplitListener
	{
		@Override
		public void expandedToggled(boolean expanded)
		{
		}

		@Override
		public void resizableToggled(boolean resizable)
		{
		}

		@Override
		public void weightChanged(float weight)
		{
		}
	}
}
