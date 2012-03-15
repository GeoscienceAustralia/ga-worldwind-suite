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
package au.gov.ga.worldwind.tiler.shapefile;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * Helper class for temporarily storing shapefile shape attributes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Attributes
{
	private final Map<String, Object> attributeMap = new HashMap<String, Object>();

	/**
	 * Load the attributes from the given {@link Feature} into this object.
	 * 
	 * @param source
	 * @param destinationSchema
	 */
	public void loadAttributes(Feature source, FeatureSchema destinationSchema)
	{
		for (int i = 0; i < destinationSchema.getAttributeCount(); i++)
		{
			if (destinationSchema.getAttributeType(i) != AttributeType.GEOMETRY)
			{
				String name = destinationSchema.getAttributeName(i);
				Object attribute = source.getAttribute(name);
				attributeMap.put(name, attribute);
			}
		}
	}

	/**
	 * Save the attributes from this object into the given {@link Feature}.
	 * 
	 * @param destination
	 */
	public void saveAttributes(Feature destination)
	{
		for (Entry<String, Object> entry : attributeMap.entrySet())
		{
			destination.setAttribute(entry.getKey(), entry.getValue());
		}
	}
}
