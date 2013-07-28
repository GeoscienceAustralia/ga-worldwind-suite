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
package au.gov.ga.worldwind.androidremote.shared.model;

/**
 * Represents an object that listens for changes to the {@link ItemModel}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ItemModelListener
{
	/**
	 * Model's items have been refreshed (ie the root changed).
	 * 
	 * @param root
	 *            Root item
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemsRefreshed(Item root, boolean remote);

	/**
	 * Item has been added to the model.
	 * 
	 * @param item
	 *            Added item
	 * @param parent
	 *            Item's parent
	 * @param index
	 *            Child index of item within the parent
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemsAdded(Item item, Item parent, int index, boolean remote);

	/**
	 * Item has been removed from the model.
	 * 
	 * @param item
	 *            Item that was removed.
	 * @param oldIndicesToRoot
	 *            Old tree index path before the item was removed.
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote);

	/**
	 * Item has been enabled/disabled.
	 * 
	 * @param item
	 *            Item changed
	 * @param enabled
	 *            Enabled/disabled state.
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemEnabled(Item item, boolean enabled, boolean remote);

	/**
	 * Item has been selected.
	 * 
	 * @param item
	 *            Selected item.
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemSelected(Item item, boolean remote);

	/**
	 * Item's opacity value has changed.
	 * 
	 * @param item
	 *            Item changed.
	 * @param opacity
	 *            Opacity value.
	 * @param remote
	 *            Did this event get caused by a message received from the
	 *            remote device?
	 */
	void itemOpacityChanged(Item item, float opacity, boolean remote);
}
