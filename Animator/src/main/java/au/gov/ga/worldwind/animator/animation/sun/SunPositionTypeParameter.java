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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSunPositionTypeParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.sun.SunPositionService;
import au.gov.ga.worldwind.common.util.Util;

/**
 * Type parameter of the {@link SunPositionAnimatable}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@EditableParameter(bound = true, minValue = 0.0, maxValue = 3.0, units = "0=position,1=behind,2=realtime,3=time")
public class SunPositionTypeParameter extends SunPositionParameterBase
{
	private static final long serialVersionUID = -1523627692815409214L;

	SunPositionTypeParameter()
	{
		super();
	}

	public SunPositionTypeParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		double value = SunPositionService.INSTANCE.getType().ordinal();
		return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
	}

	@Override
	protected void doApplyValue(double value)
	{
		int ordinal = Util.clamp((int) value, 0, 3);
		SunPositionService.INSTANCE.setType(SunPositionService.SunPositionType.values()[ordinal]);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getSunPositionTypeElementName();
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		return new SunPositionTypeParameter(name, animation);
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getSunPositionTypeParameterNameKey());
	}
}
