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

import java.util.List;

import au.gov.ga.worldwind.tiler.ribbon.LineSimplifier;
import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;
import au.gov.ga.worldwind.tiler.util.LatLon;

/**
 * Default implementation that uses a list of pipe-separated lat-lons stored on
 * the context
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
		if (context.getPathFile() != null)
		{
			if (!context.getPathFile().exists())
			{
				System.err.println("Path file does not exist");
			}
			else
			{
				List<LatLon> latlons =
						LineSimplifier.simplify(context.getPathFile(), context.getPathFileSourceSRS(),
								context.getPathFileTargetSRS(), context.getPathSimplifyTolerance());
				if (latlons != null)
				{
					StringBuffer result = new StringBuffer();
					appendLine(result, level, "<Path>");
					for (LatLon latlon : latlons)
					{
						appendLine(result, level + 1, "<LatLon units=\"degrees\" latitude=\"" + latlon.getLatitude()
								+ "\" longitude=\"" + latlon.getLongitude() + "\"/>");
					}
					appendLine(result, level, "</Path>");
					return result.toString();
				}
			}
		}

		PathFilePathElementCreator pathFileCreator = new PathFilePathElementCreator();
		if (pathFileCreator.getPathFile(context).exists())
		{
			return pathFileCreator.getElementString(level, context);
		}

		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		for (String latLonPair : context.getPathLatLons())
		{
			String[] latLon = latLonPair.split("|");
			appendLine(result, level + 1, "<LatLon units=\"degrees\" latitude=\"" + latLon[0] + "\" longitude=\""
					+ latLon[1] + "\"/>");
		}
		appendLine(result, level, "</Path>");
		return result.toString();
	}

}
