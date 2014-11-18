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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSunPositionLongitudeParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.LatLon;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.sun.SunPositionService;

/**
 * Longitude parameter of the {@link SunPositionAnimatable}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@EditableParameter(bound = true, minValue = -180.0, maxValue = 180.0)
public class SunPositionLongitudeParameter extends SunPositionParameterBase
{
	private static final long serialVersionUID = 2596594405242447479L;
	
	SunPositionLongitudeParameter()
	{
		super();
	}

	public SunPositionLongitudeParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		double value = SunPositionService.INSTANCE.getConstant().longitude.degrees;
		return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
	}

	@Override
	protected void doApplyValue(double value)
	{
		LatLon latlon = LatLon.fromDegrees(SunPositionService.INSTANCE.getConstant().latitude.degrees, value);
		SunPositionService.INSTANCE.setConstant(latlon);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getSunPositionLongitudeElementName();
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		return new SunPositionLongitudeParameter(name, animation);
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getSunPositionLongitudeParameterNameKey());
	}
}
