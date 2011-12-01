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
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

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
