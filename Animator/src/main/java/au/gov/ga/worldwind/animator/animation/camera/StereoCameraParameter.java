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
package au.gov.ga.worldwind.animator.animation.camera;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraEyeSeparationNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraFocalLengthNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

/**
 * Contains {@link Parameter}s used by the {@link StereoCamera}
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class StereoCameraParameter extends CameraParameter
{
	private static final long serialVersionUID = 20101111L;

	protected StereoCameraParameter()
	{
		super();
	}

	public StereoCameraParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	@Override
	protected StereoView getView()
	{
		return (StereoView) super.getView();
	}

	/**
	 * Parameter for stereo focal length
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	public static class FocalLengthParameter extends StereoCameraParameter
	{
		public FocalLengthParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public FocalLengthParameter(Animation animation)
		{
			this(null, animation);
		}

		FocalLengthParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraFocalLengthNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getView().getCurrentFocalLength();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			getView().getParameters().setFocalLength(value);
			redraw();
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraFocalLengthElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new FocalLengthParameter(name, animation);
		}
	}

	/**
	 * Parameter for stereo eye separation
	 */
	@SuppressWarnings("serial")
	@EditableParameter
	public static class EyeSeparationParameter extends StereoCameraParameter
	{
		public EyeSeparationParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public EyeSeparationParameter(Animation animation)
		{
			this(null, animation);
		}

		EyeSeparationParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraEyeSeparationNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getView().getCurrentEyeSeparation();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			getView().getParameters().setEyeSeparation(value);
			redraw();
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraEyeSeparationElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new EyeSeparationParameter(name, animation);
		}
	}
}
