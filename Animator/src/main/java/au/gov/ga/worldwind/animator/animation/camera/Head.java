/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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

import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * {@link Animatable} for head rotation/position, such as the rotation
 * information coming from an Oculus Rift.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Head extends Animatable
{
	/**
	 * Calculate the head rotation at the given frame.
	 * 
	 * @param frame
	 * @return Head rotation at frame
	 */
	Quaternion getRotationAtFrame(int frame);

	/**
	 * Calculate an array of head rotations for each frame in the given range.
	 * 
	 * @param startFrame
	 * @param endFrame
	 * @return Head rotations for the range
	 */
	Quaternion[] getRotationsBetweenFrames(int startFrame, int endFrame);

	/**
	 * Calculate the head position at the given frame.
	 * 
	 * @param frame
	 * @return Head position at frame
	 */
	Vec4 getPositionAtFrame(int frame);

	/**
	 * Calculate an array of head positions for each frame in the given range.
	 * 
	 * @param startFrame
	 * @param endFrame
	 * @return Head positions for the range
	 */
	Vec4[] getPositionsBetweenFrames(int startFrame, int endFrame);

	/**
	 * @return Parameter for the x-component of the head rotation quaternion
	 */
	Parameter getRotationX();

	/**
	 * @return Parameter for the y-component of the head rotation quaternion
	 */
	Parameter getRotationY();

	/**
	 * @return Parameter for the z-component of the head rotation quaternion
	 */
	Parameter getRotationZ();

	/**
	 * @return Parameter for the w-component of the head rotation quaternion
	 */
	Parameter getRotationW();

	/**
	 * @return Parameter for the x-component of the head position vector
	 */
	Parameter getPositionX();

	/**
	 * @return Parameter for the y-component of the head position vector
	 */
	Parameter getPositionY();

	/**
	 * @return Parameter for the z-component of the head position vector
	 */
	Parameter getPositionZ();
}
