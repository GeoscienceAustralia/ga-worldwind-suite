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

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.globes.ElevationModel;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A factory class that can load an {@link ElevationModel} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationElevationLoader
{
	
	/**
	 * The factory to use for loading elevation models.
	 * <p/>
	 * Defaults to an instance of {@link ElevationModelFactory}. Can be overridden
	 * with the {@link #setElevationFactory(Factory)} method
	 */
	private static Factory elevationFactory;

	public static Factory getElevationFactory()
	{
		if (elevationFactory == null)
		{
			elevationFactory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
		}
		return elevationFactory;
	}
	
	public static void setElevationFactory(Factory factory)
	{
		elevationFactory = factory;
	}
	
	/**
	 * Load an elevation model identified by the provided identifier.
	 */
	public static ElevationModel loadElevationModel(ElevationModelIdentifier identifier)
	{
		ElevationModel result = loadElevationModel(identifier.getLocation());
		result.setName(identifier.getName());
		return result;
	}
	
	/**
	 * Load an elevation model from a definition file at the provided URL location
	 */
	public static ElevationModel loadElevationModel(String url)
	{
		try
		{
			return loadElevationModel(new URL(url));
		}
		catch (MalformedURLException e)
		{
			ExceptionLogger.logException(e);
			throw new IllegalArgumentException("Unable to locate elevation model at provided location '" + url + "'", e);
		}
	}
	
	/**
	 * Load an elevation model from a definition file at the provided URL location.
	 */
	public static ElevationModel loadElevationModel(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		Element element = XMLUtil.getElementFromSource(url);
		
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, url);
		
		ElevationModel result = (ElevationModel)getElevationFactory().createFromConfigSource(element, params);
		if (result == null)
		{
			throw new IllegalArgumentException("Unable to load elevation model from location '" + url + "'");
		}
		
		result.setValue(AVKeyMore.CONTEXT_URL, url);
		
		return result;
	}
}
