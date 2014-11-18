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

import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for builders that can yield an initialised {@link LayerParameter} given a {@link Layer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface LayerParameterBuilder
{
	/**
	 * @return A Parameter primed for the provided target {@link Layer}, or <code>null</code> if this builder
	 * is not applicable for the given layer.
	 */
	LayerParameter createParameterForLayer(Animation animation, Layer targetLayer);
	
	@SuppressWarnings("unchecked")
	public static LayerParameterBuilder[] BUILDERS = {
		
		new SimpleLayerParameterBuilder(LayerOpacityParameter.class, Layer.class)
		{
			@Override
			protected LayerParameter doCreateParameter(Animation animation, Layer targetLayer)
			{
				return new LayerOpacityParameter(animation, targetLayer);
			}
		},
		
		new SimpleLayerParameterBuilder(FogFarFactorParameter.class, FogLayer.class)
		{
			@Override
			protected LayerParameter doCreateParameter(Animation animation, Layer targetLayer)
			{
				return new FogFarFactorParameter(animation, (FogLayer)targetLayer);
			}
		},
		
		new SimpleLayerParameterBuilder(FogNearFactorParameter.class, FogLayer.class)
		{
			@Override
			protected LayerParameter doCreateParameter(Animation animation, Layer targetLayer)
			{
				return new FogNearFactorParameter(animation, (FogLayer)targetLayer);
			}
		},
		
	};
	
	/**
	 * A base builder class for simple Layer->Parameter mapping rules
	 */
	public static abstract class SimpleLayerParameterBuilder implements LayerParameterBuilder
	{
		private Class<? extends Layer>[] targetLayers;
		private Class<? extends LayerParameter> targetParameter;
		
		public SimpleLayerParameterBuilder(Class<? extends LayerParameter> targetParameter, Class<? extends Layer>... targetLayers)
		{
			this.targetParameter = targetParameter;
			this.targetLayers = targetLayers;
		}
		
		public Class<? extends LayerParameter> getLayerParameterType()
		{
			return targetParameter;
		}
		
		@Override
		public LayerParameter createParameterForLayer(Animation animation, Layer targetLayer)
		{
			for (Class<? extends Layer> layerType : targetLayers)
			{
				if (layerType.isAssignableFrom(targetLayer.getClass()))
				{
					return doCreateParameter(animation, targetLayer);
				}
			}
			return null;
		}

		protected abstract LayerParameter doCreateParameter(Animation animation, Layer targetLayer);
	}
}
