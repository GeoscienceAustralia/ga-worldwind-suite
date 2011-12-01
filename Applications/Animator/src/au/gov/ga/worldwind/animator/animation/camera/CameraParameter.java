package au.gov.ga.worldwind.animator.animation.camera;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraEyeLatNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraEyeLonNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraEyeZoomNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraFarClipNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraLookatLatNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraLookatLonNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraLookatZoomNameKey;
import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraNearClipNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.view.ClipConfigurableView;

abstract class CameraParameter extends ParameterBase
{
	private static final long serialVersionUID = 20100922L;

	protected static final String DEFAULT_PARAMETER_NAME = "Render Camera - Param";

	protected CameraParameter()
	{
		super();
	}

	public CameraParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	protected View getView()
	{
		return getAnimation().getWorldWindow().getView();
	}

	protected void redraw()
	{
		getAnimation().getWorldWindow().redraw();
	}

	protected Position getCurrentEyePosition()
	{
		return getView().getCurrentEyePosition();
	}

	protected Position getCurrentLookatPosition()
	{
		Vec4 center = getView().getCenterPoint();
		Globe globe = getView().getGlobe();
		if (center == null || globe == null)
		{
			return null;
		}
		return globe.computePositionFromPoint(center);
	}

	protected void applyCameraPositions(Position eyePosition, Position lookAtPosition)
	{
		View view = getView();
		view.stopMovement();
		view.setOrientation(eyePosition, lookAtPosition);
		redraw();
	}

	/**
	 * Parameter for eye latitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class EyeLatParameter extends CameraParameter
	{
		public EyeLatParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public EyeLatParameter(Animation animation)
		{
			this(null, animation);
		}

		EyeLatParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraEyeLatNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentEyePosition().getLatitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition =
					Position.fromDegrees(value, currentEyePosition.longitude.degrees, currentEyePosition.elevation);
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraEyeLatElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new EyeLatParameter(name, animation);
		}
	}

	/**
	 * Parameter for eye longitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class EyeLonParameter extends CameraParameter
	{
		public EyeLonParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public EyeLonParameter(Animation animation)
		{
			this(null, animation);
		}

		EyeLonParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraEyeLonNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentEyePosition().getLongitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition =
					Position.fromDegrees(currentEyePosition.latitude.degrees, value, currentEyePosition.elevation);
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());

		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraEyeLonElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new EyeLonParameter(name, animation);
		}
	}

	/**
	 * Parameter for eye elevation
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "km")
	static class EyeElevationParameter extends CameraParameter
	{
		public EyeElevationParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public EyeElevationParameter(Animation animation)
		{
			this(null, animation);
		}

		EyeElevationParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraEyeZoomNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = animation.applyZoomScaling(getCurrentEyePosition().getElevation());
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition =
					Position.fromDegrees(currentEyePosition.latitude.degrees, currentEyePosition.longitude.degrees,
							getAnimation().unapplyZoomScaling(value));
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraEyeElevationElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new EyeElevationParameter(name, animation);
		}
	}

	/**
	 * Parameter for look-at latitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class LookatLatParameter extends CameraParameter
	{
		public LookatLatParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public LookatLatParameter(Animation animation)
		{
			this(null, animation);
		}

		LookatLatParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraLookatLatNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentLookatPosition().getLatitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition =
					Position.fromDegrees(value, currentLookatPosition.longitude.degrees,
							currentLookatPosition.elevation);
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);

		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraLookatLatElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new LookatLatParameter(name, animation);
		}
	}

	/**
	 * Parameter for look-at longitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class LookatLonParameter extends CameraParameter
	{
		public LookatLonParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public LookatLonParameter(Animation animation)
		{
			this(null, animation);
		}

		LookatLonParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraLookatLonNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = getCurrentLookatPosition().getLongitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition =
					Position.fromDegrees(currentLookatPosition.latitude.degrees, value, currentLookatPosition.elevation);
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraLookatLonElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new LookatLonParameter(name, animation);
		}
	}

	/**
	 * Parameter for look-at elevation
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "km")
	static class LookatElevationParameter extends CameraParameter
	{
		public LookatElevationParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public LookatElevationParameter(Animation animation)
		{
			this(null, animation);
		}

		LookatElevationParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraLookatZoomNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			double value = animation.applyZoomScaling(getCurrentLookatPosition().getElevation());
			return ParameterValueFactory.createParameterValue(this, value, animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition =
					Position.fromDegrees(currentLookatPosition.latitude.degrees,
							currentLookatPosition.longitude.degrees, getAnimation().unapplyZoomScaling(value));
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraLookatElevationElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			// TODO Auto-generated method stub
			return new LookatElevationParameter(name, animation);
		}
	}

	@SuppressWarnings("serial")
	@EditableParameter(units = "m")
	static class NearClipParameter extends CameraParameter
	{
		public NearClipParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public NearClipParameter(Animation animation)
		{
			this(null, animation);
		}

		NearClipParameter()
		{
			super();
		}

		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraNearClipNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			return ParameterValueFactory.createParameterValue(this, getView().getNearClipDistance(),
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			if (!(getView() instanceof ClipConfigurableView))
			{
				return;
			}
			ClipConfigurableView view = (ClipConfigurableView) getView();

			// Only apply if key frames have been stored. Otherwise, let the view auto-calculate them.
			if (getKeyFramesWithThisParameter().isEmpty() || !(isEnabled()))
			{
				view.setAutoCalculateNearClipDistance(true);
				return;
			}
			view.setNearClipDistance(value);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraNearClipElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new NearClipParameter(name, animation);
		}
	}

	@SuppressWarnings("serial")
	@EditableParameter(units = "m")
	static class FarClipParameter extends CameraParameter
	{
		public FarClipParameter(String name, Animation animation)
		{
			super(name, animation);
		}

		public FarClipParameter(Animation animation)
		{
			this(null, animation);
		}

		FarClipParameter()
		{
			super();
		}
		
		@Override
		protected String getDefaultName()
		{
			return getMessageOrDefault(getCameraFarClipNameKey(), DEFAULT_PARAMETER_NAME);
		}

		@Override
		public ParameterValue getCurrentValue()
		{
			return ParameterValueFactory.createParameterValue(this, getView().getFarClipDistance(),
					animation.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			if (!(getView() instanceof ClipConfigurableView))
			{
				return;
			}
			ClipConfigurableView view = (ClipConfigurableView) getView();

			// Only apply if key frames have been stored. Otherwise, let the view auto-calculate them.
			if (getKeyFramesWithThisParameter().isEmpty() || !(isEnabled()))
			{
				view.setAutoCalculateFarClipDistance(true);
				return;
			}
			view.setFarClipDistance(value);
		}

		@Override
		protected String getXmlElementName(AnimationIOConstants constants)
		{
			return constants.getCameraFarClipElementName();
		}

		@Override
		protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
				Element parameterElement, AnimationFileVersion version, AVList context)
		{
			return new FarClipParameter(name, animation);
		}
	}
}