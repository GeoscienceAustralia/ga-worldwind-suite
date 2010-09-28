package au.gov.ga.worldwind.animator.animation.layer;

import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;

/**
 * An interface for layers that can be animated in the Animator application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimatableLayer extends Animatable
{
	/**
	 * @return The layer associated with this animatable layer
	 */
	Layer getLayer();
	
	/**
	 * Add the provided parameter to this layer
	 * <p/>
	 * Supports only one parameter of each type for each layer.
	 * <p/> 
	 * If a parameter of the same type is already registered on this layer,
	 * the new parameter will replace the old. 
	 *  
	 * @param parameter The parameter to add to the layer
	 */
	void addParameter(LayerParameter parameter);
	
	/**
	 * Get the parameter of the given type associated with this layer, if
	 * one exists.
	 * 
	 * @param type The type of parameter to retrieve
	 * 
	 * @return the parameter of the given type associated with this layer, or <code>null</code>
	 * if one does not exist.
	 */
	LayerParameter getParameterOfType(LayerParameter.Type type);
	
	/**
	 * @return An identifier that identifies the layer associated with this animatable layer
	 */
	LayerIdentifier getLayerIdentifier();
}
