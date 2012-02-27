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

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * An {@link AnimationWriter} that writes an {@link Animation} in the <em>version 2</em>
 * XML format.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class XmlAnimationWriter implements AnimationWriter
{

	private static final AnimationFileVersion CURRENT_FILE_VERSION = AnimationFileVersion.VERSION020;
	
	/**
	 * @return The current version of the XML file format to use for writing
	 */
	public static AnimationFileVersion getCurrentFileVersion()
	{
		return CURRENT_FILE_VERSION;
	}
	
	@Override
	public void writeAnimation(String fileName, Animation animation) throws IOException
	{
		Validate.notBlank(fileName, "A filename must be provided");
		Validate.notNull(animation, "An animation must be provided");
		
		File outputFile = new File(fileName);
		writeAnimation(outputFile, animation);
	}

	@Override
	public void writeAnimation(File file, Animation animation) throws IOException
	{
		Validate.notNull(file, "A file must be provided");
		Validate.notNull(animation, "An animation must be provided");
		
		Document document = WWXML.createDocumentBuilder(false).newDocument();
		
		Element rootElement = document.createElement("worldWindAnimation");
		document.appendChild(rootElement);
		
		WWXML.setTextAttribute(rootElement, "version", CURRENT_FILE_VERSION.getDisplayName());

		rootElement.appendChild(animation.toXml(rootElement, CURRENT_FILE_VERSION));
		
		XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(file));
	}


}
