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
package au.gov.ga.worldwind.viewer.theme;

import javax.swing.JPanel;

import au.gov.ga.worldwind.viewer.panels.SideBar;

/**
 * Represents a panel that is displayed in the collapsible sidebar of the
 * application. Various panels can be enabled/disabled from the {@link Theme}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ThemePanel extends ThemePiece
{
	/**
	 * @return Parent {@link JPanel} of this panel; this is added into the
	 *         {@link SideBar}.
	 */
	JPanel getPanel();

	/**
	 * @return Is this panel resizable?
	 */
	boolean isResizable();

	/**
	 * Enable/disable the resizable state of this panel.
	 * 
	 * @param resizable
	 */
	void setResizable(boolean resizable);

	/**
	 * @return The resize weight of this panel. The ratio of this weight to the
	 *         total weight of all panels determines the size of this panel, and
	 *         also the percentage of any extra size is allocated to this panel
	 *         when resizing.
	 */
	float getWeight();

	/**
	 * Set the resize weight of this panel
	 * 
	 * @param weight
	 */
	void setWeight(float weight);

	/**
	 * @return Is this panel expanded (ie not collapsed)?
	 */
	boolean isExpanded();

	/**
	 * Set this panel's expanded state
	 * 
	 * @param expanded
	 */
	void setExpanded(boolean expanded);

	/**
	 * Listener interface for receiving {@link ThemePanel} events.
	 */
	public interface ThemePanelListener extends ThemePieceListener
	{
		/**
		 * Called when the panel's resizable property changes.
		 * 
		 * @param source
		 */
		void resizableToggled(ThemePanel source);

		/**
		 * Called when the panel's resize weight changes.
		 * 
		 * @param source
		 */
		void weightChanged(ThemePanel source);

		/**
		 * Called when the panel is expanded/collapsed.
		 * 
		 * @param source
		 */
		void expandedToggled(ThemePanel source);
	}

	/**
	 * Blank implementation of the {@link ThemePanelListener} interface.
	 */
	public class ThemePanelAdapter extends ThemePieceAdapter implements ThemePanelListener
	{
		@Override
		public void expandedToggled(ThemePanel source)
		{
		}

		@Override
		public void resizableToggled(ThemePanel source)
		{
		}

		@Override
		public void weightChanged(ThemePanel source)
		{
		}
	}
}
