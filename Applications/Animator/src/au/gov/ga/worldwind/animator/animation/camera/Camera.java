/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A {@link Camera} is an {@link Animatable} object defined by an eye location
 * and a look-at position.
 * <p/>
 * It is used to define and control the camera position inside the WorldWind
 * world.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface Camera extends Animatable
{
	/**
	 * @return The parameter that represents the latitude of the camera 'eye'
	 */
	Parameter getEyeLat();

	/**
	 * @return The parameter that represents the longitude of the camera 'eye'
	 */
	Parameter getEyeLon();

	/**
	 * @return The parameter that represents the elevation of the camera 'eye'
	 */
	Parameter getEyeElevation();

	/**
	 * @return The parameter that represents the latitude of the camera
	 *         'look-at' point
	 */
	Parameter getLookAtLat();

	/**
	 * @return The parameter that represents the longitude of the camera
	 *         'look-at' point
	 */
	Parameter getLookAtLon();

	/**
	 * @return The parameter that represents the elevation of the camera
	 *         'look-at' point
	 */
	Parameter getLookAtElevation();

	/**
	 * @return The eye position of the camera in the provided frame range (inclusive)
	 */
	Position[] getEyePositionsBetweenFrames(AnimationContext animationContext, int startFrame, int endFrame);

	/**
	 * @return The lookat position of the camera in the provided frame range (inclusive)
	 */
	Position[] getLookatPositionsBetweenFrames(AnimationContext animationContext, int startFrame, int endFrame);
	
	/**
	 * Return the eye position of the camera at the provided frame
	 * 
	 * @param context
	 *            The context in which the animation is running
	 * @param frame
	 *            The frame at which the eye position is required
	 * 
	 * @return The eye position of the camera at the provided frame
	 */
	Position getEyePositionAtFrame(AnimationContext context, int frame);

	/**
	 * Return the look-at position of the camera at the provided frame
	 * 
	 * @param context
	 *            The context in which the animation is running
	 * @param frame
	 *            The frame at which the eye position is required
	 * 
	 * @return The eye position of the camera at the provided frame
	 */
	Position getLookatPositionAtFrame(AnimationContext animationContext, int frame);

	/**
	 * Smooths the camera's eye speed through the animation.
	 * <p/>
	 * This may result in key frames related to the camera being re-adjusted to
	 * provide a smooth camera transition.
	 * 
	 * @param context
	 *            The context of the animation
	 */
	void smoothEyeSpeed(AnimationContext context);

	/**
	 * Copy parameters and other globals from the provided camera to this
	 * camera. Called when swapping between normal and stereo cameras.
	 * 
	 * @param camera
	 *            Camera to copy state from
	 */
	void copyStateFrom(Camera camera);

}
