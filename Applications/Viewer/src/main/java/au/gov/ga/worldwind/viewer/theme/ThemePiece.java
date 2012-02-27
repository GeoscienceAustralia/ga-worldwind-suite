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

import gov.nasa.worldwind.Disposable;

import javax.swing.Icon;

/**
 * Super-interface for a piece of a {@link Theme}. Examples of pieces are HUD
 * elements, or UI panels such as the layer panel.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ThemePiece extends Disposable
{
	/**
	 * Setup this piece with the given theme.
	 * 
	 * @param theme
	 */
	void setup(Theme theme);

	/**
	 * @return A string to use as the display name
	 */
	String getDisplayName();

	/**
	 * Set this piece's display name.
	 * 
	 * @param name
	 */
	void setDisplayName(String name);

	/**
	 * @return Is this piece on? Generally on means visible.
	 */
	boolean isOn();

	/**
	 * Turn this piece on/off.
	 * 
	 * @param on
	 */
	void setOn(boolean on);

	/**
	 * Add a listener to listen for property change events.
	 * 
	 * @param listener
	 */
	void addListener(ThemePieceListener listener);

	/**
	 * Remove a property change event listener.
	 * 
	 * @param listener
	 */
	void removeListener(ThemePieceListener listener);

	/**
	 * @return The icon that represents this piece.
	 */
	Icon getIcon();

	/**
	 * Listener interface for receiving piece property change events.
	 */
	public interface ThemePieceListener
	{
		/**
		 * Called when piece is toggled on/off.
		 * 
		 * @param source
		 */
		void onToggled(ThemePiece source);

		/**
		 * Called when the piece's display name changes.
		 * 
		 * @param source
		 */
		void displayNameChanged(ThemePiece source);
	}

	/**
	 * Blank implementation of the {@link ThemePieceListener} interface.
	 */
	public class ThemePieceAdapter implements ThemePieceListener
	{
		@Override
		public void displayNameChanged(ThemePiece source)
		{
		}

		@Override
		public void onToggled(ThemePiece source)
		{
		}
	}
}
