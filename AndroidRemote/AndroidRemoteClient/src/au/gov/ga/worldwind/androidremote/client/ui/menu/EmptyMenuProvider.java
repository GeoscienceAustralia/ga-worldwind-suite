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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Simple {@link ItemModelFragmentMenuProvider} implementation that provides no
 * menu items.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EmptyMenuProvider implements ItemModelFragmentMenuProvider
{
	@Override
	public boolean hasOptionsMenu()
	{
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return false;
	}
}
