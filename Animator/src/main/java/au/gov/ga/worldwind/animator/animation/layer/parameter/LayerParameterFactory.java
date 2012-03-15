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
package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A factory class for creating {@link LayerParameter}s from XML elements
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerParameterFactory
{
	
	/** A map of element name -> instance for use as factories in creating {@link LayerParameter}s */
	private static Map<String, LayerParameter> factoryMap = new HashMap<String, LayerParameter>();
	static
	{
		// Add additional LayerParameters here as they are created
		factoryMap.put(LayerParameter.Type.OPACITY.name().toLowerCase(), instantiate(LayerOpacityParameter.class));
		factoryMap.put(LayerParameter.Type.NEAR.name().toLowerCase(), instantiate(FogNearFactorParameter.class));
		factoryMap.put(LayerParameter.Type.FAR.name().toLowerCase(), instantiate(FogFarFactorParameter.class));
		factoryMap.put(LayerParameter.Type.OUTLINE_OPACITY.name().toLowerCase(), instantiate(ShapeOutlineOpacityParameter.class));
	}
	
	/**
	 * Create an instance of the {@link LayerParameter} that corresponds to the provided XML element in the 
	 * given file version.
	 * 
	 * @param element The element to de-serialise from
	 * @param version The version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return an instance of the {@link LayerParameter} that corresponds to the provided XML element, or <code>null</code>
	 * if one cannot be found
	 */
	public static LayerParameter fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version is required");
		Validate.notNull(context, "A context is required");
		
		LayerParameter layerFactory = getLayerFactory(element);
		
		if (layerFactory == null)
		{
			return null;
		}
		
		return (LayerParameter)layerFactory.fromXml(element, version, context);
	}

	private static LayerParameter getLayerFactory(Element element)
	{
		return factoryMap.get(element.getNodeName());
	}

	/**
	 * @return The list of {@link LayerParameter}s that can be applied to the provided layer in the provided animation
	 */
	public static LayerParameter[] createDefaultParametersForLayer(Animation animation, Layer targetLayer)
	{
		if (animation == null || targetLayer == null)
		{
			return new LayerParameter[0];
		}
		
		List<LayerParameter> result = new ArrayList<LayerParameter>();
		for (LayerParameterBuilder builder : LayerParameterBuilder.BUILDERS)
		{
			LayerParameter parameterForLayer = builder.createParameterForLayer(animation, targetLayer);
			if (parameterForLayer != null)
			{
				result.add(parameterForLayer);
			}
		}
		return result.toArray(new LayerParameter[0]);
	}
	
	
	/**
	 * Instantiate the provided class using the default constructor.
	 * 
	 * @param clazz The class to instantiate
	 * 
	 * @return The instantiated object
	 */
	private static LayerParameter instantiate(Class<? extends LayerParameter> clazz)
	{
		Validate.notNull(clazz, "A class must be provided");
		
		try
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return (LayerParameter)constructor.newInstance();
		} 
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " does not declare a no-arg constructor.");
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Exception while instantiating class " + clazz.getSimpleName() + ".", e.getCause());
		}
	}
	
}
