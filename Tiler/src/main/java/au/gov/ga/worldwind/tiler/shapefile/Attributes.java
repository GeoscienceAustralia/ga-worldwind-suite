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

import java.io.IOException;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Helper class for temporarily storing shapefile shape attributes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Attributes
{
	protected final Object[] values;

	public Attributes(SimpleFeatureType schema)
	{
		values = new Object[schema.getAttributeCount() - 1];
	}

	/**
	 * Load the attributes from the given {@link Feature} into this object.
	 * 
	 * @param row
	 * @throws IOException
	 */
	public void loadAttributes(SimpleFeature feature) throws IOException
	{
		for (int i = 0; i < values.length; i++)
		{
			values[i] = feature.getAttribute(i + 1);
		}
	}

	/**
	 * Save the attributes from this object into the given {@link Feature}.
	 * 
	 * @param feature
	 */
	public void saveAttributes(SimpleFeature feature)
	{
		for (int i = 0; i < values.length; i++)
		{
			feature.setAttribute(i + 1, values[i]);
		}
	}
}
