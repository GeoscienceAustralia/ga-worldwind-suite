package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.avlist.AVList;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

public abstract class StereoCameraParameter extends CameraParameter
{
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

	@EditableParameter
	public static class FocalLengthParameter extends StereoCameraParameter
	{
		public FocalLengthParameter(Animation animation)
		{
			super(MessageSourceAccessor.get()
					.getMessage(AnimationMessageConstants.getCameraFocalLengthNameKey(),
							DEFAULT_PARAMETER_NAME), animation);
		}

		FocalLengthParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getView().getCurrentFocalLength();
			return ParameterValueFactory.createParameterValue(this, value,
					context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			getView().getParameters().setFocalLength(value);
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new FocalLengthParameter();
		}
	}

	@EditableParameter
	public static class EyeSeparationParameter extends StereoCameraParameter
	{
		public EyeSeparationParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(
					AnimationMessageConstants.getCameraEyeSeparationNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		EyeSeparationParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getView().getCurrentEyeSeparation();
			return ParameterValueFactory.createParameterValue(this, value,
					context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			getView().getParameters().setEyeSeparation(value);
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new EyeSeparationParameter();
		}
	}
}
