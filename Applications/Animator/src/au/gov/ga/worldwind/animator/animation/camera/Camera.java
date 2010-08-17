/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.math.vector.Vector3;

/**
 * A {@link Camera} is an {@link Animatable} object defined by an eye location and a look-at position.
 * <p/>
 * It is used to define and control the camera position inside the WorldWind world. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface Camera extends Animatable
{
	/**
	 * @return The parameter that represents the position of the camera 'eye'
	 */
	Parameter<Vector3> getEyePosition();
	
	/**
	 * @return The parameter that represents the position of the camera 'look-at' point
	 */
	Parameter<Vector3> getLookAtPosition();
}
