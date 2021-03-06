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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDepthOfFieldFocusParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

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
 * A {@link Parameter} which controls the focus distance of the
 * {@link DepthOfFieldAnimatableEffect}. Everything at this depth is in focus.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldFocusParameter extends EffectParameterBase
{
	public DepthOfFieldFocusParameter(String name, Animation animation, DepthOfFieldAnimatableEffect effect)
	{
		super(name, animation, effect);
	}

	DepthOfFieldFocusParameter()
	{
		super();
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getDepthOfFieldFocusParameterNameKey());
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return new BasicBezierParameterValue(getDefaultValue(animation.getCurrentFrame()), animation.getCurrentFrame(),
				this);
	}

	@Override
	protected void doApplyValue(double value)
	{
		((DepthOfFieldAnimatableEffect) getEffect()).getEffect().setFocus(value);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getDepthOfFieldFocusElementName();
	}

	@Override
	public double getDefaultValue(int frame)
	{
		Position eyePosition = animation.getCamera().getEyePositionAtFrame(frame);
		Position lookAtPosition = animation.getCamera().getLookatPositionAtFrame(frame);
		Vec4 eyePoint = animation.getView().getGlobe().computePointFromPosition(eyePosition);
		Vec4 lookAtPoint = animation.getView().getGlobe().computePointFromPosition(lookAtPosition);
		return eyePoint.distanceTo3(lookAtPoint);
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		DepthOfFieldAnimatableEffect parameterEffect = (DepthOfFieldAnimatableEffect) context.getValue(constants.getCurrentEffectKey());
		Validate.notNull(parameterEffect,
				"No effect found in the context. Expected one under the key '" + constants.getCurrentEffectKey() + "'.");

		return new DepthOfFieldFocusParameter(name, animation, parameterEffect);
	}
}
