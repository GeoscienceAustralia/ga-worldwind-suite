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

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface for parameters that control properties of an {@link AnimatableEffect}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface EffectParameter extends Parameter
{
	/**
	 * @return The {@link AnimatableEffect} this parameter is associated with
	 */
	AnimatableEffect getEffect();
	
	/**
	 * Apply this parameter's state to it's associated {@link AnimatableEffect} for the current frame
	 */
	void apply();
}
