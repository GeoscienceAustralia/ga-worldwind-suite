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
