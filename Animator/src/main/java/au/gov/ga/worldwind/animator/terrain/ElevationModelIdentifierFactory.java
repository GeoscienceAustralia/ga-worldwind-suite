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
package au.gov.ga.worldwind.animator.terrain;

import static au.gov.ga.worldwind.animator.util.Util.isBlank;
import gov.nasa.worldwind.globes.ElevationModel;

import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A factory class to create instances of {@link ElevationModelIdentifier}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ElevationModelIdentifierFactory
{

	/**
	 * Creates a new {@link ElevationModelIdentifier} that identifies the provided {@link ElevationModel}
	 */
	public static ElevationModelIdentifier createFromElevationModel(ElevationModel model)
	{
		if (model == null)
		{
			return null;
		}
		
		String modelName = model.getName();
		if (isBlank(modelName))
		{
			return null;
		}
		
		URL modelUrl = (URL)model.getValue(AVKeyMore.CONTEXT_URL);
		if (modelUrl == null)
		{
			return null;
		}
		
		return new ElevationModelIdentifierImpl(modelName, modelUrl.toExternalForm());
	}
	
	public static ElevationModelIdentifier createFromDefinition(URL definitionLocation)
	{
		if (definitionLocation == null || !isModelDefinition(definitionLocation))
		{
			return null;
		}
		
		String name = getNameFromDefinition(definitionLocation);
		if (isBlank(name))
		{
			name = getNameFromUrlLocation(definitionLocation);
		}
		
		return new ElevationModelIdentifierImpl(name, definitionLocation.toExternalForm());
	}

	private static boolean isModelDefinition(URL definitionLocation)
	{
		Document definitionDocument = XMLUtil.openDocument(definitionLocation);
		if (definitionDocument == null)
		{
			return false;
		}
		
		Element elevationModelElement = XMLUtil.getElement(definitionDocument.getDocumentElement(), "/ElevationModel", null);
		if (elevationModelElement == null)
		{
			return false;
		}
		
		return true;
	}

	private static String getNameFromUrlLocation(URL definitionLocation)
	{
		String locationName = definitionLocation.toExternalForm();
		int startIndex = locationName.lastIndexOf("/");
		int endIndex = locationName.lastIndexOf(".");
		
		return locationName.substring(startIndex + 1, endIndex);
	}

	private static String getNameFromDefinition(URL definitionLocation)
	{
		Document definitionDocument = XMLUtil.openDocument(definitionLocation);
		if (definitionDocument == null)
		{
			return null;
		}
		
		Element nameElement = XMLUtil.getElement(definitionDocument.getDocumentElement(), "/ElevationModel/ElevationModel/DisplayName", null);
		if (nameElement == null || nameElement.getFirstChild() == null)
		{
			return null;
		}
		
		return nameElement.getFirstChild().getNodeValue();
	}
	
}
