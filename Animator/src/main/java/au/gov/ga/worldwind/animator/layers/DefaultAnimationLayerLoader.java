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
package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * The default animation layer loader. Delegates to an injected {@link LayerFactory} instance for layer instantiation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public class DefaultAnimationLayerLoader implements AnimationLayerLoader
{
	/** 
	 * The layer factory instance to use.
	 * <p/>
	 * Defaults to an instance of the {@link LayerFactory}.
	 * <p/>
	 * Can be overridden with the {@link #setLayerFactory(BasicLayerFactory)} method.
	 */
	private static Factory layerFactory;
	
	/**
	 * @return The layer factory instance to use for creating layers
	 */
	public Factory getLayerFactory()
	{
		if (layerFactory == null)
		{
			layerFactory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
		}
		return layerFactory;
	}
	
	/**
	 * @param layerFactory The layer factory instance to use for creating layers
	 */
	public void setLayerFactory(BasicLayerFactory layerFactory)
	{
		DefaultAnimationLayerLoader.layerFactory = layerFactory;
	}
	
	/**
	 * Load the layer identified by the provided identifier.
	 * 
	 * @param identifier The identifier that specifies the layer to open
	 * 
	 * @return The layer identified by the provided identifier, or <code>null</code> if an error occurred during layer load
	 */
	@Override
	public Layer loadLayer(LayerIdentifier identifier)
	{
		try
		{
			URL sourceUrl = null;
			try
			{
				sourceUrl = new URL(identifier.getLocation());
			}
			catch (MalformedURLException e)
			{
				throw new IllegalArgumentException("Unable to locate Layer '"+ identifier.getName() +"' at provided location '" + identifier.getLocation() + "'", e);
			}
			Layer loadedLayer = loadLayer(sourceUrl);
			if (loadedLayer == null)
			{
				return loadedLayer;
			}
			loadedLayer.setName(identifier.getName());
			return loadedLayer;
		}
		catch (Throwable t)
		{
			ExceptionLogger.logException(t);
			return null;
		}
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL.
	 */
	@Override
	public Layer loadLayer(String url)
	{
		URL sourceUrl = null;
		try
		{
			sourceUrl = new URL(url);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Unable to locate Layer at provided location '" + url + "'", e);
		}
		return loadLayer(sourceUrl);
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL, or <code>null</code> if an error occurred during layer loading.
	 */
	@Override
	public Layer loadLayer(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		try
		{
			Element element = XMLUtil.getElementFromSource(url);
			
			AVList params = new AVListImpl();
			params.setValue(AVKeyMore.CONTEXT_URL, url);
			
			Layer result = (Layer)getLayerFactory().createFromConfigSource(element, params);
			result.setValue(AVKeyMore.CONTEXT_URL, url);
			result.setEnabled(true);
			
			return result;
		}
		catch (Throwable e)
		{
			ExceptionLogger.logException(e);
			return null;
		}
	}

}

