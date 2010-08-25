/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.math.vector.Vector3;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.animator.util.message.MessageSourceAccessor;

/**
 * A default implementation of the {@link Camera} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class CameraImpl extends AnimatableBase implements Camera
{
	private static final long serialVersionUID = 20100819L;

	private static final String DEFAULT_CAMERA_NAME = "Render Camera";
	private static final String DEFAULT_PARAMETER_NAME = "Render Camera - Param";

	private Parameter eyeLat;
	private Parameter eyeLon;
	private Parameter eyeElevation;

	private Parameter lookAtLat;
	private Parameter lookAtLon;
	private Parameter lookAtElevation;

	private Collection<Parameter> parameters;
	
	private Animation animation;
	
	/**
	 * Constructor. Initialises the camera parameters.
	 */
	public CameraImpl(Animation animation)
	{
		super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraNameKey(), DEFAULT_CAMERA_NAME));

		Validate.notNull(animation, "An animation instance is required");
		this.animation = animation;
		
		initialiseParameters(animation);
	}


	/**
	 * Initialise the camera parameters
	 * 
	 * @param animation
	 */
	@SuppressWarnings("serial")
	private void initialiseParameters(Animation animation)
	{
		eyeLat =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraEyeLatNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value = context.getView().getEyePosition().getLatitude().getDegrees();
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		eyeLon =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraEyeLonNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value = context.getView().getEyePosition().getLongitude().getDegrees();
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		eyeElevation =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraEyeZoomNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value = context.applyZoomScaling(context.getView().getEyePosition().getElevation());
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		lookAtLat =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraLookatLatNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value = ((OrbitView) context.getView()).getCenterPosition().getLatitude().getDegrees();
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		lookAtLon =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraLookatLonNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value = ((OrbitView) context.getView()).getCenterPosition().getLongitude().getDegrees();
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		lookAtElevation =
				new ParameterBase(MessageSourceAccessor.get().getMessage(
						AnimationMessageConstants.getCameraLookatZoomNameKey(), DEFAULT_PARAMETER_NAME), animation)
				{
					@Override
					public ParameterValue getCurrentValue(AnimationContext context)
					{
						double value =
								context.applyZoomScaling(((OrbitView) context.getView()).getCenterPosition()
										.getElevation());
						return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
					}
				};

		parameters = new ArrayList<Parameter>(6);
		parameters.add(eyeLat);
		parameters.add(eyeLon);
		parameters.add(eyeElevation);
		parameters.add(lookAtLat);
		parameters.add(lookAtLon);
		parameters.add(lookAtElevation);
	}


	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		Position eye = getEyePositionAtFrame(animationContext, frame);

		Position center = getLookatPositionAtFrame(animationContext, frame);

		View view = animationContext.getView();
		view.stopMovement();
		view.setOrientation(eye, center);
	}

	@Override
	public Position getEyePositionAtFrame(AnimationContext animationContext, int frame)
	{
		return Position.fromDegrees(eyeLat.getValueAtFrame(animationContext, frame).getValue(),
									eyeLon.getValueAtFrame(animationContext, frame).getValue(),
									animationContext.unapplyZoomScaling(eyeElevation.getValueAtFrame(animationContext, frame).getValue()));
	}

	@Override
	public Position getLookatPositionAtFrame(AnimationContext animationContext, int frame)
	{
		return Position.fromDegrees(lookAtLat.getValueAtFrame(animationContext, frame).getValue(), 
									lookAtLon.getValueAtFrame(animationContext, frame).getValue(), 
									animationContext.unapplyZoomScaling(lookAtElevation.getValueAtFrame(animationContext, frame).getValue()));
	}

	@Override
	public String getRestorableState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Parameter getEyeLat()
	{
		return eyeLat;
	}

	@Override
	public Parameter getEyeLon()
	{
		return eyeLon;
	}

	@Override
	public Parameter getEyeElevation()
	{
		return eyeElevation;
	}

	@Override
	public Parameter getLookAtLat()
	{
		return lookAtLat;
	}

	@Override
	public Parameter getLookAtLon()
	{
		return lookAtLon;
	}

	@Override
	public Parameter getLookAtElevation()
	{
		return lookAtElevation;
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public void smoothEyeSpeed(AnimationContext context)
	{
		// TODO: This assumes that eye parameters are always set together. Is this a safe assumption?
		List<KeyFrame> eyeKeyFrames = eyeLat.getKeyFramesWithThisParameter();
		
		// There needs to be at least two key frames for smoothing to apply
		int numberOfKeys = eyeKeyFrames.size();
		if (numberOfKeys <= 1)
		{
			return;
		}
		
		// Calculate the cumulative distance at each key frame
		double[] cumulativeDistance = new double[numberOfKeys - 1];
		for (int i = 0; i < numberOfKeys - 1; i++)
		{
			int firstFrame = eyeKeyFrames.get(i).getFrame();
			int lastFrame = eyeKeyFrames.get(i + 1).getFrame();
			
			cumulativeDistance[i] = i == 0 ? 0 : cumulativeDistance[i - 1];
			
			Vector3 vStart = null;
			for (int frame = firstFrame; frame <= lastFrame; frame++)
			{
				double x = eyeLat.getValueAtFrame(context, frame).getValue();
				double y = eyeLon.getValueAtFrame(context, frame).getValue();
				double z = eyeElevation.getValueAtFrame(context, frame).getValue();
				
				Vector3 vEnd = new Vector3(x, y, z);
				if (vStart != null)
				{
					cumulativeDistance[i] += vStart.subtract(vEnd).distance();
				}
				vStart = vEnd;
			}
		}

		// Calculate where to put the new frames
		int[] newFrames = new int[numberOfKeys];
		int firstFrame = eyeKeyFrames.get(0).getFrame();
		int lastFrame = eyeKeyFrames.get(numberOfKeys - 1).getFrame();
		
		newFrames[0] = firstFrame;
		newFrames[numberOfKeys - 1] = lastFrame;

		for (int i = 1; i < numberOfKeys - 1; i++)
		{
			newFrames[i] = (int) Math.round(Math.abs((firstFrame - lastFrame + 1) * cumulativeDistance[i - 1] / cumulativeDistance[numberOfKeys - 2]));
		}

		// Fix any frames that have been swapped over by mistake
		for (int i = 0; i < newFrames.length - 1; i++)
		{
			if (newFrames[i] >= newFrames[i + 1])
			{
				newFrames[i + 1] = newFrames[i] + 1;
			}
		}
		// Make sure the last frame is correct. If not, adjust it and re-adjust previous frames.
		if (newFrames[numberOfKeys - 1] != lastFrame)
		{
			newFrames[numberOfKeys - 1] = lastFrame;
			for (int i = newFrames.length - 1; i > 0; i--)
			{
				if (newFrames[i] <= newFrames[i - 1])
				{
					newFrames[i - 1] = newFrames[i] - 1;
				}
			}
		}
		if (newFrames[0] != firstFrame)
		{
			throw new IllegalStateException("Expected new first frame to be '" + firstFrame + "' but is '" + newFrames[0] + "'");
		}

		// Insert new key frames for eye parameters. This may involve creating new key frames so that
		// other animatable objects aren't affected
		for (int i = 0; i < eyeKeyFrames.size(); i++)
		{
			KeyFrame oldKeyFrame = eyeKeyFrames.get(i);
			
			Collection<ParameterValue> eyeValues = oldKeyFrame.getValuesForParameters(getParameters());
			KeyFrame newKeyFrame = new KeyFrameImpl(newFrames[i], eyeValues);
			
			oldKeyFrame.removeValuesForParameters(getParameters());
			
			animation.insertKeyFrame(newKeyFrame);
		}
		animation.removeEmptyKeyFrames();
	}

}
