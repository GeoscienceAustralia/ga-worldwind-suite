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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getSunPositionAnimatableNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * Basic implementation of {@link SunPositionAnimatable}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SunPositionAnimatableImpl extends AnimatableBase implements SunPositionAnimatable
{
	private static final long serialVersionUID = 4496802797920181732L;

	private final List<Parameter> parameters = new ArrayList<Parameter>();

	private Parameter typeParameter;
	private Parameter latitudeParameter;
	private Parameter longitudeParameter;
	private Parameter timeParameter;

	public SunPositionAnimatableImpl(String name, Animation animation)
	{
		super(name, animation);
		initializeParameters();
	}

	@SuppressWarnings("unused")
	private SunPositionAnimatableImpl()
	{
	}

	protected void initializeParameters()
	{
		if (typeParameter == null || latitudeParameter == null || longitudeParameter == null || timeParameter == null)
		{
			typeParameter = new SunPositionTypeParameter(null, animation);
			latitudeParameter = new SunPositionLatitudeParameter(null, animation);
			longitudeParameter = new SunPositionLongitudeParameter(null, animation);
			timeParameter = new SunPositionTimeParameter(null, animation);

			typeParameter.setArmed(false);
			typeParameter.setEnabled(false);
			latitudeParameter.setArmed(false);
			latitudeParameter.setEnabled(false);
			longitudeParameter.setArmed(false);
			longitudeParameter.setEnabled(false);
			timeParameter.setArmed(false);
			timeParameter.setEnabled(false);
		}

		parameters.clear();
		parameters.add(typeParameter);
		parameters.add(latitudeParameter);
		parameters.add(longitudeParameter);
		parameters.add(timeParameter);
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		return parameters;
	}

	@Override
	protected void doApply()
	{
		for (Parameter parameter : parameters)
		{
			((SunPositionParameter) parameter).apply();
		}
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getSunPositionElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		XPath xpath = WWXML.makeXPath();

		SunPositionAnimatableImpl animatable = new SunPositionAnimatableImpl(name, animation);

		animatable.typeParameter = new SunPositionTypeParameter().fromXml(WWXML.getElement(element,
				constants.getSunPositionTypeElementName(), xpath), version, context);
		animatable.latitudeParameter = new SunPositionLatitudeParameter().fromXml(WWXML.getElement(element,
				constants.getSunPositionLatitudeElementName(), xpath), version, context);
		animatable.longitudeParameter = new SunPositionLongitudeParameter().fromXml(WWXML.getElement(element,
				constants.getSunPositionLongitudeElementName(), xpath), version, context);
		animatable.timeParameter = new SunPositionTimeParameter().fromXml(WWXML.getElement(element,
				constants.getSunPositionTimeElementName(), xpath), version, context);

		animatable.initializeParameters();

		return animatable;
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getSunPositionAnimatableNameKey());
	}

	@Override
	public Parameter getType()
	{
		return typeParameter;
	}

	@Override
	public Parameter getLatitude()
	{
		return latitudeParameter;
	}

	@Override
	public Parameter getLongitude()
	{
		return longitudeParameter;
	}

	@Override
	public Parameter getTime()
	{
		return timeParameter;
	}
}
