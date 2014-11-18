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
package au.gov.ga.worldwind.animator.animation.sun;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * Abstract base class of parameters used by the {@link SunPositionAnimatable}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class SunPositionParameterBase extends ParameterBase implements SunPositionParameter
{
	private static final long serialVersionUID = 9176617713299468924L;

	protected SunPositionParameterBase()
	{
		super();
	}

	public SunPositionParameterBase(String name, Animation animation)
	{
		super(name, animation);
	}

	@Override
	public void apply()
	{
		if (!isEnabled())
		{
			return;
		}
		int frame = animation.getCurrentFrame();
		ParameterValue value = getValueAtFrame(frame);
		applyValueIfEnabled(value.getValue(), frame);
	}
}
