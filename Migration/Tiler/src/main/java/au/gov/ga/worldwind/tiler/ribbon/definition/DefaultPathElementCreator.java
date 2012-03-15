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
package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * Default implementation that uses a list of pipe-separated lat-lons stored on the context
 */
public class DefaultPathElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Path";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		for (String latLonPair : context.getPathLatLons())
		{
			String[] latLon = latLonPair.split("|");
			appendLine(result, level+1, "<LatLon units=\"degrees\" latitude=\"" + latLon[0] + "\" longitude=\"" + latLon[1] + "\"/>" );
		}
		appendLine(result, level, "</Path>");
		return result.toString();
	}

}
