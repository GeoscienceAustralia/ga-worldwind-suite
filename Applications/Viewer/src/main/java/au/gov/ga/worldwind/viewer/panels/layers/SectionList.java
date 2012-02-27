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
package au.gov.ga.worldwind.viewer.panels.layers;

import java.util.Collection;

/**
 * A SectionList allows an object to register as a section, and then whenever
 * addAllFromObject is called by that object, the collection is added in the
 * same relative position as the size of the list when registerSectionObject was
 * called. It is either implemented by:
 * <ol>
 * <li>
 * using a dummy object inserted when registerSectionObject is called, and
 * whenever addAllFromObject is called, the index of the dummy object is looked
 * up and the collection is inserted at that position.</li>
 * <li>
 * using sublists within the list.</li>
 * </ol>
 */
public interface SectionList<E>
{
	/**
	 * Register the given object as a section key. This object can then be
	 * passed in the add/remove methods to keep the added objects in the same
	 * section of the list.
	 * 
	 * @param section
	 */
	void registerSectionObject(Object section);

	/**
	 * Add all from the given collection into this list into the given section.
	 * 
	 * @param section
	 *            Section in the list in which to insert objects
	 * @param c
	 *            Collection of objects to add
	 */
	void addAllFromSection(Object section, Collection<? extends E> c);

	/**
	 * Remove all from the given collection from the given section in this list.
	 * 
	 * @param section
	 *            Section in the list from which to remove objects
	 * @param c
	 *            Collection of objects to remove
	 */
	void removeAllFromSection(Object section, Collection<? extends E> c);
}
