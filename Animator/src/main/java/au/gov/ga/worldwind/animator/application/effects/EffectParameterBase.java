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

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Base class for most {@link EffectParameter} implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class EffectParameterBase extends ParameterBase implements EffectParameter
{
	/** The effect this parameter is associated with */
	private Effect effect;

	/**
	 * @param name
	 *            The name of this parameter
	 * @param animation
	 *            The animation associated with this parameter
	 * @param effect
	 *            The effect associated with this parameter
	 */
	public EffectParameterBase(String name, Animation animation, Effect effect)
	{
		super(name, animation);
		Validate.notNull(effect, "An effect is required");
		this.effect = effect;
	}

	/**
	 * Constructor used for deserialization. Not for general consumption.
	 */
	protected EffectParameterBase()
	{
		super();
	}

	@Override
	public Effect getEffect()
	{
		return effect;
	}

	protected void setEffect(Effect effect)
	{
		this.effect = effect;
	}

	@Override
	public void apply()
	{
		int frame = animation.getCurrentFrame();
		ParameterValue value = getValueAtFrame(frame);
		applyValueIfEnabled(value.getValue(), frame);
	}
}
