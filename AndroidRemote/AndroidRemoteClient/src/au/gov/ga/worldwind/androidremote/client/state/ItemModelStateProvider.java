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
package au.gov.ga.worldwind.androidremote.client.state;

import au.gov.ga.worldwind.androidremote.client.ui.menu.ItemModelFragmentMenuProvider;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;

/**
 * Provides {@link ItemModelState}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ItemModelStateProvider
{
	/**
	 * Get the ItemModelState associated with the given modelId.
	 * 
	 * @param modelId
	 *            Unique id of the model ({@link ItemModel#getId()})
	 * @return ItemModelState for the model id.
	 */
	ItemModelState getItemModelState(int modelId);

	/**
	 * Get the menu provider associated with the given modelId.
	 * 
	 * @param modelId
	 * @return Menu provider for the model id.
	 */
	ItemModelFragmentMenuProvider getMenuProvider(int modelId);
}
