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
package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

/**
 * An interface for types that can be saved to XML
 * <p/>
 * <b>Important:</b> All implementations of {@link XmlSerializable} must provide a no-arg default
 * constructor. This can be private to protect class contracts etc, but it must exist.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface XmlSerializable<S>
{
	/**
	 * Create and return an XML element from this {@link XmlSerializable} object.
	 * <p/>
	 * The returned {@link Element} will be set as a child of the provided parent.
	 * 
	 * @param parent The parent element for this {@link XmlSerializable}
	 * @param version TODO
	 * 
	 * @return The element that represents this object
	 */
	Element toXml(Element parent, AnimationFileVersion version);

	/**
	 * Create and return a new instance of this class from the provided {@link Element}.
	 * 
	 * @param element The element to de-serialise from
	 * @param versionId The ID of the version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return The de-serialised object
	 */
	S fromXml(Element element, AnimationFileVersion versionId, AVList context);
	
	/**
	 * The prefix to use for accessing attributes via XPath
	 */
	String ATTRIBUTE_PATH_PREFIX = "@";
}
