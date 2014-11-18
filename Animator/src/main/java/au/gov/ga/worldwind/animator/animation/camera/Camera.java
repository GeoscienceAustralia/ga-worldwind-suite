/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.animator.animation.Animatable;
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
	 * @return The parameter that represents the roll of the camera
	 */
	Parameter getRoll();

	/**
	 * @return The parameter that represents the field-of-view of the camera
	 */
	Parameter getFieldOfView();

	/**
	 * @return Whether the camera clipping parameters are active or not
	 */
	boolean isClippingParametersActive();

	/**
	 * Sets the clipping parameters to be active or not.
	 * <p/>
	 * If the status changes, this can result in key frame parameters being
	 * removed entirely from the animation.
	 */
	void setClippingParametersActive(boolean active);

	/**
	 * @return The parameter that represents the near clipping distance of the
	 *         camera
	 */
	Parameter getNearClip();

	/**
	 * @return The parameter that represents the near clipping distance of the
	 *         camera
	 */
	Parameter getFarClip();

	/**
	 * @return The eye position of the camera in the provided frame range
	 *         (inclusive)
	 */
	Position[] getEyePositionsBetweenFrames(int startFrame, int endFrame);

	/**
	 * @return The lookat position of the camera in the provided frame range
	 *         (inclusive)
	 */
	Position[] getLookatPositionsBetweenFrames(int startFrame, int endFrame);

	/**
	 * Return the eye position of the camera at the provided frame
	 * 
	 * @param frame
	 *            The frame at which the eye position is required
	 * 
	 * @return The eye position of the camera at the provided frame
	 */
	Position getEyePositionAtFrame(int frame);

	/**
	 * Return the look-at position of the camera at the provided frame
	 * 
	 * @param frame
	 *            The frame at which the eye position is required
	 * 
	 * @return The eye position of the camera at the provided frame
	 */
	Position getLookatPositionAtFrame(int frame);

	/**
	 * Smooths the camera's eye speed through the animation.
	 * <p/>
	 * This may result in key frames related to the camera being re-adjusted to
	 * provide a smooth camera transition.
	 */
	void smoothEyeSpeed();

	/**
	 * Copy parameters and other globals from the provided camera to this
	 * camera. Called when swapping between normal and stereo cameras.
	 * 
	 * @param camera
	 *            Camera to copy state from
	 */
	void copyStateFrom(Camera camera);

}
