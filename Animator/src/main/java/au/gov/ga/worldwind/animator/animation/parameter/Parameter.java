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
package au.gov.ga.worldwind.animator.animation.parameter;

import java.io.Serializable;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * A {@link Parameter} represents a single animatable property of some
 * {@link Animatable} object (e.g. Camera position, layer opacity etc.).
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Parameter extends AnimationObject, Serializable, Nameable, XmlSerializable<Parameter>,
		AnimationEventListener, Changeable, Enableable, Armable
{

	/**
	 * @return The animation this parameter is associated with
	 */
	Animation getAnimation();

	/**
	 * Get the current value of this {@link Parameter}.
	 * 
	 * @return the current value of this {@link Parameter} in the current
	 *         {@link AnimationContext}.
	 */
	ParameterValue getCurrentValue();

	/**
	 * Get the value of this {@link Parameter} at the provided frame.
	 * <p/>
	 * If there are no {@link KeyFrame}s recorded that contain information for
	 * this {@link Parameter}, the default {@link ParameterValue} will be
	 * returned.
	 * <p/>
	 * If the provided frame is <em>before</em> the <em>first</em> recorded
	 * {@link KeyFrame} that contains information about this {@link Parameter},
	 * the {@link ParameterValue} recorded on that <em>first</em>
	 * {@link KeyFrame} will be returned. Similarly, if the provided frame is
	 * <em>after</em> the <em>last</em> {@link KeyFrame} with a recorded
	 * {@link ParameterValue}, the {@link ParameterValue} on that
	 * {@link KeyFrame} will be returned.
	 * <p/>
	 * If the provided frame lies between two {@link KeyFrame}s with
	 * {@link ParameterValue}s recorded for this {@link Parameter}, an
	 * interpolated {@link ParameterValue} will be returned, with interpolation
	 * performed using an {@link Interpolator} determined by the type of
	 * parameter value.
	 * 
	 * @param frame
	 *            The frame for which the value of the parameter is required
	 * 
	 * @return The value of this {@link Parameter} at the provided frame.
	 */
	ParameterValue getValueAtFrame(int frame);

	/**
	 * Get the values of this {@link Parameter} between the provided frames,
	 * inclusive. See {@link Parameter#getValueAtFrame(int)}.
	 * 
	 * @param startFrame
	 *            Start frame (inclusive)
	 * @param endFrame
	 *            End frame (inclusive)
	 * @param array
	 *            {@link ParameterValue} array to put values in (if null or
	 *            doesn't contain enough space, a new array is returned)
	 * 
	 * @return {@link ParameterValue}s for the frames provided.
	 */
	ParameterValue[] getValuesBetweenFrames(int startFrame, int endFrame, ParameterValue[] array);

	/**
	 * Get the default value of this parameter. This could be a static value
	 * stored in the parameter, or a dynamic value based on the current state of
	 * the animation/view or the camera parameters.
	 * 
	 * @param context
	 *            The context of the current animation.
	 * @param frame
	 *            The frame for which to retrieve the default value (if dynamic)
	 * @return The default value of this {@link Parameter}.
	 */
	double getDefaultValue(int frame);

	/**
	 * Apply the provided value to the property this parameter controls. If this
	 * parameter is disabled, the default value calculated by
	 * {@link Parameter#getDefaultValue(AnimationContext, int)} is applied.
	 * 
	 * @param value
	 */
	void applyValueIfEnabled(double value, int frame);

	void applyValueAnyway(double value);

	/**
	 * Gets the first recorded value of this parameter on a key frame before the
	 * provided frame.
	 * <p/>
	 * If one cannot be found, returns <code>null</code>.
	 * 
	 * @param frame
	 *            The frame before which to retrieve the value
	 * 
	 * @return the first recorded value of this parameter on a key frame before
	 *         the provided frame, or <code>null</code> if one cannot be found
	 */
	ParameterValue getValueAtKeyFrameBeforeFrame(int frame);

	/**
	 * Gets the first recorded value of this parameter on a key frame after the
	 * provided frame.
	 * <p/>
	 * If one cannot be found, returns <code>null</code>.
	 * 
	 * @param frame
	 *            The frame after which to retrieve the value
	 * 
	 * @return the first recorded value of this parameter on a key frame after
	 *         the provided frame, or <code>null</code> if one cannot be found
	 */
	ParameterValue getValueAtKeyFrameAfterFrame(int frame);

	/**
	 * Gets the ordered list of key frames that contain a recorded value for
	 * this parameter
	 * <p/>
	 * If no key frames exist with a value for this parameter, an empty list
	 * will be returned.
	 * 
	 * @return The key frames that contain a recorded value for this parameter.
	 */
	List<KeyFrame> getKeyFramesWithThisParameter();

	/**
	 * Calls {@link Armable#connectCodependantArmable(Armable)} and
	 * {@link Enableable#connectCodependantEnableable(Enableable)} on this,
	 * passing parameter.
	 */
	void connectCodependantParameter(Parameter parameter);
}
