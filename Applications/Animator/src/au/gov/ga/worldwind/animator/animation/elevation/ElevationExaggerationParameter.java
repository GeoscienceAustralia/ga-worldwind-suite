package au.gov.ga.worldwind.animator.animation.elevation;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;

/**
 * A {@link Parameter} used to control the exaggeration of a specific {@link ElevationExaggeration}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface ElevationExaggerationParameter extends Parameter
{

	/**
	 * @return The exaggeration this parameter is controlling
	 */
	ElevationExaggeration getElevationExaggeration();
	
	/**
	 * Apply this parameter's state to it's associated {@link ElevationExaggeration} for the given frame
	 * 
	 * @param animationContext The context in which the animation is executing
	 * @param frame The current frame of the animation
	 */
	void apply(AnimationContext animationContext, int frame);
	
}
