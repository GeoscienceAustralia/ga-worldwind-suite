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

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.io.InputStream;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.kml.KMLLayer;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Helper class for loading layers from XML. Uses the layer factory and
 * elevation model factory to attempt to instanciate layers from XML layer
 * definitions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerLoader
{
	public static LoadedLayer load(URL sourceUrl, InputStream stream) throws Exception
	{
		String lowerCaseUrl = sourceUrl.toString().toLowerCase();
		boolean isKml = lowerCaseUrl.endsWith(".kml");
		boolean isKmz = lowerCaseUrl.endsWith(".kmz");

		AVList params = new AVListImpl();
		if (sourceUrl != null)
		{
			params.setValue(AVKeyMore.CONTEXT_URL, sourceUrl);
		}

		if (isKml || isKmz)
		{
			KMLLayer layer = new KMLLayer(sourceUrl, stream, params);
			return new LoadedLayer(layer, params);
		}
		else
		{
			Element element = XMLUtil.getElementFromSource(stream);
			return loadFromElement(element, sourceUrl, params);
		}
	}

	public static LoadedLayer loadFromElement(Element element, URL sourceUrl, AVList params) throws Exception
	{
		URL legend = XMLUtil.getURL(element, "Legend", sourceUrl);
		URL query = XMLUtil.getURL(element, "Query", sourceUrl);

		if (params == null)
			params = new AVListImpl();

		Object o = null;
		Exception exception = null;

		//if (o == null)
		{
			try
			{
				Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
				o = factory.createFromConfigSource(element, params);
			}
			catch (Exception e)
			{
				//if (exception == null)
				exception = e;
			}
		}

		if (o == null)
		{
			try
			{
				Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
				o = factory.createFromConfigSource(element, params);
			}
			catch (Exception e)
			{
				if (exception == null)
					exception = e;
			}
		}

		if (o == null)
		{
			if (exception != null)
				throw exception;
			throw new Exception("Error reading file");
		}

		LoadedLayer loaded = new LoadedLayer(o, params);
		loaded.setLegendURL(legend);
		loaded.setQueryURL(query);
		return loaded;
	}
}
