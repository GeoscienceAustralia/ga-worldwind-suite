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
package au.gov.ga.worldwind.androidremote.client.ui.menu;

import au.gov.ga.worldwind.androidremote.client.ui.ItemModelFragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Represents an object that adds menu options to the Android menu for
 * {@link ItemModelFragment}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ItemModelFragmentMenuProvider
{
	/**
	 * @return Does this fragment have an options menu?
	 */
	boolean hasOptionsMenu();

	/**
	 * Create the options menu as children of the given menu.
	 * 
	 * @param menu
	 * @param inflater
	 */
	void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

	/**
	 * Called when an menu option is selected.
	 * 
	 * @param item
	 * @return Return false to allow normal menu processing to proceed, true to
	 *         consume it here.
	 */
	boolean onOptionsItemSelected(MenuItem item);
}
