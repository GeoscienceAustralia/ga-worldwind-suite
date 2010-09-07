package au.gov.ga.worldwind.animator.animation.layer;

import gov.nasa.worldwind.layers.AbstractLayer;
import au.gov.ga.worldwind.animator.animation.Animatable;

/**
 * An interface for layers that can be animated in the Animator application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface AnimatableLayer extends Animatable
{
	/**
	 * @return The layer associated with this animatable layer
	 */
	AbstractLayer getLayer();
}
