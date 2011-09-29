package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * An interface for builders that can yield an initialised {@link LayerParameter} given a {@link Layer}
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
		
		new SimpleLayerParameterBuilder(LayerOpacityParameter.class, TiledImageLayer.class)
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
