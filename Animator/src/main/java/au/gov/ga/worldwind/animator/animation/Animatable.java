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
package au.gov.ga.worldwind.animator.animation;

import java.io.Serializable;
import java.util.Collection;

import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * An interface for objects that can be animated within an {@link Animation}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Animatable extends AnimationObject, Serializable, Nameable, XmlSerializable<Animatable>,
		AnimationEventListener, Changeable, Enableable, Armable
{
	/**
	 * @return The {@link Animation} that this {@link Animatable} is associated
	 *         with
	 */
	Animation getAnimation();

	/**
	 * Apply this object's behaviour/changes to the 'world' for the current
	 * frame
	 * 
	 * @param animationContext
	 *            The context in which the animation is executing
	 */
	void apply();

	/**
	 * @return The collection of all parameters associated with this animatable
	 *         object
	 */
	Collection<Parameter> getParameters();

	/**
	 * @return The collection of all <em>enabled</em> parameters associated with
	 *         this animatable object
	 */
	Collection<Parameter> getEnabledParameters();

	/**
	 * @return The collection of all parameters associated with this animatable
	 *         that are <em>armed</em>
	 */
	Collection<Parameter> getArmedParameters();

	/**
	 * @return The collection of all parameters associated with this animatable
	 *         that are both <em>enabled</em> and <em>armed</em>
	 */
	Collection<Parameter> getEnabledArmedParameters();

	/**
	 * Calls {@link Armable#connectCodependantArmable(Armable)} and
	 * {@link Enableable#connectCodependantEnableable(Enableable)} on this,
	 * passing animatable.
	 */
	void connectCodependantAnimatable(Animatable animatable);
}
