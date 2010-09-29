package au.gov.ga.worldwind.animator.animation.elevation;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;

/**
 * An interface for an {@link Animatable} object that allows control over the elevation model of an animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface AnimatableElevation extends Animatable
{

	/**
	 * @return The elevation model this instance is controlling
	 */
	VerticalExaggerationElevationModel getElevationModel();
	
	
	
}
