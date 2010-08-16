/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.Restorable;

import java.io.Serializable;

/**
 * An interface for objects that can be animated within an {@link Animation}
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Animatable extends Serializable, Restorable
{
	/**
	 * Apply this object's behaviour/changes to the 'world' for the given frame
	 * 
	 * @param animationContext The context in which the animation is executing
	 * @param frame The current frame of the animation
	 */
	void apply(AnimationContext animationContext, int frame);
}
