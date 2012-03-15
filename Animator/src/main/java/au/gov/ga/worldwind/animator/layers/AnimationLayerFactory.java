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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.layers.misc.FogLayerFactory;
import au.gov.ga.worldwind.animator.layers.sky.Skysphere;
import au.gov.ga.worldwind.common.layers.LayerFactory;

/**
 * An extension of the {@link LayerFactory} that adds support for
 * animation-specific layer types.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class AnimationLayerFactory extends LayerFactory
{

	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		String layerType = WWXML.getText(domElement, "@layerType");
		if (Skysphere.LAYER_TYPE.equals(layerType))
		{
			return new Skysphere(domElement, params);
		}
		if (FogLayerFactory.LAYER_TYPE.equals(layerType))
		{
			return FogLayerFactory.createFromDefinition(domElement, params);
		}
		
		return super.createFromLayerDocument(domElement, params);
	}
	
}
