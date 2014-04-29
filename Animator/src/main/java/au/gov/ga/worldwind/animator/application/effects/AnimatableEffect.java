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
package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.common.effects.Effect;
import au.gov.ga.worldwind.common.render.FrameBuffer;

/**
 * Interface that defines an {@link Animatable} full-screen effect.
 * {@link AnimatableEffect}s are bound by the scene controller, and are rendered in screen
 * space. Each {@link AnimatableEffect} implementation should contain a
 * {@link FrameBuffer} that has a depth texture, and the effect shaders should
 * write to the gl_FragDepth variable so that the fragment depth is passed
 * through the effect chain.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface AnimatableEffect extends Animatable, Effect
{
	/**
	 * Add the provided parameter to this effect
	 * 
	 * @param parameter
	 *            The parameter to add to the effect
	 */
	void addParameter(EffectParameter parameter);

	/**
	 * Create a new instance of this {@link AnimatableEffect}, associated with the
	 * provided {@link Animation}.
	 * 
	 * @param animation
	 *            Animation to associated new Effect with
	 * @return New instance of this Effect
	 */
	AnimatableEffect createWithAnimation(Animation animation);

	/**
	 * @return The default name for this effect.
	 */
	String getDefaultName();

	/**
	 * @param constants
	 * @return The effect's XML element name.
	 */
	String getXmlElementName(AnimationIOConstants constants);
}
