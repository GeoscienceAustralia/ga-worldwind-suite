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
package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDepthOfFieldFarParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.application.effects.EffectParameterBase;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link Parameter} which controls the far limit of the
 * {@link DepthOfFieldAnimatableEffect}. Everything beyond this depth is fully blurred.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldFarParameter extends EffectParameterBase
{
	public DepthOfFieldFarParameter(String name, Animation animation, DepthOfFieldAnimatableEffect effect)
	{
		super(name, animation, effect);
	}

	DepthOfFieldFarParameter()
	{
		super();
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getDepthOfFieldFarParameterNameKey());
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return new BasicBezierParameterValue(animation.getView().getFarClipDistance(), animation.getCurrentFrame(),
				this);
	}

	@Override
	protected void doApplyValue(double value)
	{
		((DepthOfFieldAnimatableEffect) getEffect()).getEffect().setFar(value);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getDepthOfFieldFarElementName();
	}

	@Override
	public double getDefaultValue(int frame)
	{
		//TODO is this right, or should it be retrieved from the camera?
		return animation.getView().getFarClipDistance();
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		DepthOfFieldAnimatableEffect parameterEffect = (DepthOfFieldAnimatableEffect) context.getValue(constants.getCurrentEffectKey());
		Validate.notNull(parameterEffect,
				"No effect found in the context. Expected one under the key '" + constants.getCurrentEffectKey() + "'.");

		return new DepthOfFieldFarParameter(name, animation, parameterEffect);
	}
}
