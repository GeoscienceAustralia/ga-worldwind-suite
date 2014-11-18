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

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadPositionXParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadPositionYParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadPositionZParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadRotationWParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadRotationXParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadRotationYParameterNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getHeadRotationZParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueType;
import au.gov.ga.worldwind.animator.view.AnimatorView;

/**
 * Parameter used by the {@link Head} animatable.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
abstract class HeadParameter extends ParameterBase
{
	private static final long serialVersionUID = 20100922L;

	protected static final String DEFAULT_PARAMETER_NAME = "Head - Param";

	protected HeadParameter()
	{
		super();
	}

	public HeadParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	/**
	 * @return The current {@link View} being used by the Animator
	 */
	protected AnimatorView getView()
	{
		return (AnimatorView) getAnimation().getView();
	}

	/**
	 * Queue a redraw on the {@link WorldWindow}
	 */
	protected void redraw()
	{
		getAnimation().getWorldWindow().redraw();
	}

	protected Quaternion getCurrentRotation()
	{
		return getView().getHeadRotation();
	}

	protected Vec4 getCurrentPosition()
	{
		return getView().getHeadPosition();
	}

	protected void applyRotation(Quaternion rotation)
	{
		AnimatorView view = getView();
		view.setHeadRotation(rotation);
		redraw();
	}

	protected void applyPosition(Vec4 position)
	{
		AnimatorView view = getView();
		view.setHeadPosition(position);
		redraw();
	}

	/**
	 * Parameter for rotation x
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class RotationXParameter extends HeadParameter
	{
		public RotationXParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public RotationXParameter(Animation animation)
		{
			this(null, animation);
		}

		RotationXParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadRotationXParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentRotation().x;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Quaternion rotation = getCurrentRotation();
			rotation = new Quaternion(value, rotation.y, rotation.z, rotation.w);
			applyRotation(rotation);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadRotationXElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new RotationXParameter(name, animation);
		}
	}

	/**
	 * Parameter for rotation y
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class RotationYParameter extends HeadParameter
	{
		public RotationYParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public RotationYParameter(Animation animation)
		{
			this(null, animation);
		}

		RotationYParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadRotationYParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentRotation().y;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Quaternion rotation = getCurrentRotation();
			rotation = new Quaternion(rotation.x, value, rotation.z, rotation.w);
			applyRotation(rotation);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadRotationYElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new RotationYParameter(name, animation);
		}
	}

	/**
	 * Parameter for rotation z
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class RotationZParameter extends HeadParameter
	{
		public RotationZParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public RotationZParameter(Animation animation)
		{
			this(null, animation);
		}

		RotationZParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadRotationZParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentRotation().z;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Quaternion rotation = getCurrentRotation();
			rotation = new Quaternion(rotation.x, rotation.y, value, rotation.w);
			applyRotation(rotation);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadRotationZElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new RotationZParameter(name, animation);
		}
	}

	/**
	 * Parameter for rotation w
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class RotationWParameter extends HeadParameter
	{
		public RotationWParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public RotationWParameter(Animation animation)
		{
			this(null, animation);
		}

		RotationWParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadRotationWParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentRotation().w;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Quaternion rotation = getCurrentRotation();
			rotation = new Quaternion(rotation.x, rotation.y, rotation.z, value);
			applyRotation(rotation);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadRotationWElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new RotationWParameter(name, animation);
		}
	}

	/**
	 * Parameter for position x
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class PositionXParameter extends HeadParameter
	{
		public PositionXParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public PositionXParameter(Animation animation)
		{
			this(null, animation);
		}

		PositionXParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadPositionXParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentPosition().x;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Vec4 position = getCurrentPosition();
			position = new Vec4(value, position.y, position.z);
			applyPosition(position);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadPositionXElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new PositionXParameter(name, animation);
		}
	}

	/**
	 * Parameter for position y
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class PositionYParameter extends HeadParameter
	{
		public PositionYParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public PositionYParameter(Animation animation)
		{
			this(null, animation);
		}

		PositionYParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadPositionYParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentPosition().y;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Vec4 position = getCurrentPosition();
			position = new Vec4(position.x, value, position.z);
			applyPosition(position);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadPositionYElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new PositionYParameter(name, animation);
		}
	}

	/**
	 * Parameter for position z
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	static class PositionZParameter extends HeadParameter
	{
		public PositionZParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public PositionZParameter(Animation animation)
		{
			this(null, animation);
		}

		PositionZParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getHeadPositionZParameterNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentPosition().z;
			return ParameterValueFactory.createParameterValue(ParameterValueType.LINEAR, this, value,
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Vec4 position = getCurrentPosition();
			position = new Vec4(position.x, position.y, value);
			applyPosition(position);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getHeadPositionZElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new PositionZParameter(name, animation);
		}
	}
}
