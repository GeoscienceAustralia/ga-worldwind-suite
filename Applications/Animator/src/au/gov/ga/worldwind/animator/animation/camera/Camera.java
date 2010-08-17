/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.math.vector.Vector1;

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
	 * @return The parameter that represents the latitude of the camera 'eye'
	 */
	Parameter<Vector1> getEyeLat();
	
	/**
	 * @return The parameter that represents the longitude of the camera 'eye'
	 */
	Parameter<Vector1> getEyeLon();
	
	/**
	 * @return The parameter that represents the elevation of the camera 'eye'
	 */
	Parameter<Vector1> getEyeElevation();

	/**
	 * @return The parameter that represents the latitude of the camera 'look-at' point
	 */
	Parameter<Vector1> getLookAtLat();
	
	/**
	 * @return The parameter that represents the longitude of the camera 'look-at' point
	 */
	Parameter<Vector1> getLookAtLon();
	
	/**
	 * @return The parameter that represents the elevation of the camera 'look-at' point
	 */
	Parameter<Vector1> getLookAtElevation();
	
}
