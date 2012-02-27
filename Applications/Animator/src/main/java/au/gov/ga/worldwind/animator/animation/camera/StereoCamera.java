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

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An extended {@link Camera} interface that supports extra parameters
 * associated with stereo animation rendering.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface StereoCamera extends Camera
{
	/**
	 * @return The parameter that represents the stereo camera focal length
	 */
	Parameter getFocalLength();

	/**
	 * @return The parameter that represents the stereo camera eye separation
	 */
	Parameter getEyeSeparation();

	/**
	 * @return Is this camera using the dynamic stereo mode?
	 */
	boolean isDynamicStereo();

	/**
	 * Set this camera to use the dynamic stereo mode. This means that the focal
	 * length and eye separation are calculated dynamically based on eye
	 * distance and camera pitch.
	 * 
	 * @param dynamicStereo
	 */
	void setDynamicStereo(boolean dynamicStereo);
}
