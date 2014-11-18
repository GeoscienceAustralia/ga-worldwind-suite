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
package au.gov.ga.worldwind.animator.animation.camera;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadAnimatableNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.PositionXParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.PositionYParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.PositionZParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.RotationWParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.RotationXParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.RotationYParameter;
import au.gov.ga.worldwind.animator.animation.camera.HeadParameter.RotationZParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.view.AnimatorView;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Implementation of the {@link Head} animatable.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HeadImpl extends AnimatableBase implements Head
{
	private static final long serialVersionUID = 5510322598222413040L;

	protected static final String DEFAULT_HEAD_NAME = "Head";

	private Parameter rotationX;
	private Parameter rotationY;
	private Parameter rotationZ;
	private Parameter rotationW;
	private Parameter positionX;
	private Parameter positionY;
	private Parameter positionZ;

	protected Collection<Parameter> parameters;

	/**
	 * Constructor. Initialises the camera parameters.
	 */
	public HeadImpl(Animation animation)
	{
		this(null, animation);
	}

	protected HeadImpl(String name, Animation animation)
	{
		super(name, animation);
		initialiseParameters(animation);
		connectCodependants();
		connectAsListener();
	}

	/**
	 * Constructor used for de-serialising. Not for general use.
	 */
	protected HeadImpl()
	{
	}

	@Override
	protected String getDefaultName()
	{
		return getMessageOrDefault(getHeadAnimatableNameKey(), DEFAULT_HEAD_NAME);
	}

	/**
	 * Initialise the rotation parameters
	 * 
	 * @param animation
	 */
	protected void initialiseParameters(Animation animation)
	{
		rotationX = new RotationXParameter(animation);
		rotationY = new RotationYParameter(animation);
		rotationZ = new RotationZParameter(animation);
		rotationW = new RotationWParameter(animation);
		positionX = new PositionXParameter(animation);
		positionY = new PositionYParameter(animation);
		positionZ = new PositionZParameter(animation);
	}

	protected void connectCodependants()
	{
		rotationX.connectCodependantParameter(rotationY);
		rotationX.connectCodependantParameter(rotationZ);
		rotationX.connectCodependantParameter(rotationW);
		rotationX.connectCodependantParameter(positionX);
		rotationX.connectCodependantParameter(positionY);
		rotationX.connectCodependantParameter(positionZ);
	}

	protected void connectAsListener()
	{
		for (Parameter p : getParameters())
		{
			p.addChangeListener(this);
		}
	}

	@Override
	protected void doApply()
	{
		if (!animation.hasKeyFrame(this))
		{
			return;
		}

		int frame = getAnimation().getCurrentFrame();
		Quaternion rotation = getRotationAtFrame(frame);
		Vec4 position = getPositionAtFrame(frame);

		View view = animation.getView();
		if (view instanceof AnimatorView)
		{
			((AnimatorView) view).setHeadRotation(rotation);
			((AnimatorView) view).setHeadPosition(position);
		}
	}

	@Override
	public Quaternion getRotationAtFrame(int frame)
	{
		return new Quaternion(rotationX.getValueAtFrame(frame).getValue(), rotationY.getValueAtFrame(frame).getValue(),
				rotationZ.getValueAtFrame(frame).getValue(), rotationW.getValueAtFrame(frame).getValue());
	}

	@Override
	public Quaternion[] getRotationsBetweenFrames(int startFrame, int endFrame)
	{
		Validate.isTrue(startFrame <= endFrame, "End frame must not be less than start frame");

		ParameterValue[] xValues = rotationX.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] yValues = rotationY.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] zValues = rotationZ.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] wValues = rotationW.getValuesBetweenFrames(startFrame, endFrame, null);

		Quaternion[] result = new Quaternion[xValues.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = new Quaternion(xValues[i].getValue(), yValues[i].getValue(), zValues[i].getValue(),
					wValues[i].getValue());
		}

		return result;
	}

	@Override
	public Vec4 getPositionAtFrame(int frame)
	{
		return new Vec4(positionX.getValueAtFrame(frame).getValue(), positionY.getValueAtFrame(frame).getValue(),
				positionZ.getValueAtFrame(frame).getValue());
	}

	@Override
	public Vec4[] getPositionsBetweenFrames(int startFrame, int endFrame)
	{
		Validate.isTrue(startFrame <= endFrame, "End frame must not be less than start frame");

		ParameterValue[] xValues = positionX.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] yValues = positionY.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] zValues = positionZ.getValuesBetweenFrames(startFrame, endFrame, null);

		Vec4[] result = new Vec4[xValues.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = new Vec4(xValues[i].getValue(), yValues[i].getValue(), zValues[i].getValue());
		}

		return result;
	}

	@Override
	public Parameter getRotationX()
	{
		return rotationX;
	}

	@Override
	public Parameter getRotationY()
	{
		return rotationY;
	}

	@Override
	public Parameter getRotationZ()
	{
		return rotationZ;
	}

	@Override
	public Parameter getRotationW()
	{
		return rotationW;
	}

	@Override
	public Parameter getPositionX()
	{
		return positionX;
	}

	@Override
	public Parameter getPositionY()
	{
		return positionY;
	}

	@Override
	public Parameter getPositionZ()
	{
		return positionZ;
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		if (parameters == null || parameters.isEmpty())
		{
			parameters = new ArrayList<Parameter>(8);
			parameters.add(rotationX);
			parameters.add(rotationY);
			parameters.add(rotationZ);
			parameters.add(rotationW);
			parameters.add(positionX);
			parameters.add(positionY);
			parameters.add(positionZ);
		}
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getHeadElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		HeadImpl head = new HeadImpl(name, animation);
		setupHeadFromXml(head, element, version, context);
		return head;
	}

	protected void setupHeadFromXml(HeadImpl head, Element element, AnimationFileVersion version,
			AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		XPath xpath = XPathFactory.newInstance().newXPath();

		head.parameters = null;
		head.rotationX = new RotationXParameter().fromXml(
				WWXML.getElement(element, constants.getHeadRotationXElementName(), xpath), version, context);
		head.rotationY = new RotationYParameter().fromXml(
				WWXML.getElement(element, constants.getHeadRotationYElementName(), xpath), version, context);
		head.rotationZ = new RotationZParameter().fromXml(
				WWXML.getElement(element, constants.getHeadRotationZElementName(), xpath), version, context);
		head.rotationW = new RotationWParameter().fromXml(
				WWXML.getElement(element, constants.getHeadRotationWElementName(), xpath), version, context);
		head.positionX = new PositionXParameter().fromXml(
				WWXML.getElement(element, constants.getHeadPositionXElementName(), xpath), version, context);
		head.positionY = new PositionYParameter().fromXml(
				WWXML.getElement(element, constants.getHeadPositionYElementName(), xpath), version, context);
		head.positionZ = new PositionZParameter().fromXml(
				WWXML.getElement(element, constants.getHeadPositionZElementName(), xpath), version, context);

		head.connectCodependants();
		head.connectAsListener();
	}
}
