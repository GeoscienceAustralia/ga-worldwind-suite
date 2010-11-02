/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeElevationParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeLatParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeLonParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatElevationParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatLatParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatLonParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.math.vector.Vector3;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

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
	 * Constructor used for de-serialising. Not for general use.
	 */
	protected CameraImpl(){super();}

	/**
	 * Initialise the camera parameters
	 * 
	 * @param animation
	 */
	private void initialiseParameters(Animation animation)
	{
		eyeLat = new EyeLatParameter(animation);
		eyeLon = new EyeLonParameter(animation);
		eyeElevation = new EyeElevationParameter(animation);

		lookAtLat = new LookatLatParameter(animation);
		lookAtLon = new LookatLonParameter(animation);
		lookAtElevation = new LookatElevationParameter(animation);
		
		// Add this camera as a change listener to the camera parameters
		for (Parameter p : getParameters())
		{
			p.addChangeListener(this);
		}
	}

	@Override
	protected void doApply(AnimationContext animationContext, int frame)
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
		return Position.fromDegrees(eyeLat.getValueAtFrame(frame).getValue(),
									eyeLon.getValueAtFrame(frame).getValue(),
									animationContext.unapplyZoomScaling(eyeElevation.getValueAtFrame(frame).getValue()));
	}

	@Override
	public Position getLookatPositionAtFrame(AnimationContext animationContext, int frame)
	{
		return Position.fromDegrees(lookAtLat.getValueAtFrame(frame).getValue(), 
									lookAtLon.getValueAtFrame(frame).getValue(), 
									animationContext.unapplyZoomScaling(lookAtElevation.getValueAtFrame(frame).getValue()));
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
		if (parameters == null || parameters.isEmpty())
		{
			parameters = new ArrayList<Parameter>(6);
			parameters.add(eyeLat);
			parameters.add(eyeLon);
			parameters.add(eyeElevation);
			parameters.add(lookAtLat);
			parameters.add(lookAtLon);
			parameters.add(lookAtElevation);
		}
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
				double x = eyeLat.getValueAtFrame(frame).getValue();
				double y = eyeLon.getValueAtFrame(frame).getValue();
				double z = eyeElevation.getValueAtFrame(frame).getValue();
				
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


	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getCameraElementName());
		
		WWXML.setTextAttribute(result, constants.getCameraAttributeName(), getName());

		Element eyeLatElement = WWXML.appendElement(result, constants.getCameraEyeLatElementName());
		eyeLatElement.appendChild(eyeLat.toXml(eyeLatElement, version));
		
		Element eyeLonElement = WWXML.appendElement(result, constants.getCameraEyeLonElementName());
		eyeLonElement.appendChild(eyeLon.toXml(eyeLonElement, version));
		
		Element eyeElevationElement = WWXML.appendElement(result, constants.getCameraEyeElevationElementName());
		eyeElevationElement.appendChild(eyeElevation.toXml(eyeElevationElement, version));
		
		Element lookAtLatElement = WWXML.appendElement(result, constants.getCameraLookatLatElementName());
		lookAtLatElement.appendChild(lookAtLat.toXml(lookAtLatElement, version));
		
		Element lookAtLonElement = WWXML.appendElement(result, constants.getCameraLookatLonElementName());
		lookAtLonElement.appendChild(lookAtLon.toXml(lookAtLonElement, version));
		
		Element lookAtElevationElement = WWXML.appendElement(result, constants.getCameraLookatElevationElementName());
		lookAtElevationElement.appendChild(lookAtElevation.toXml(lookAtElevationElement, version));
		
		return result;
	}


	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				Validate.isTrue(context.hasKey(constants.getAnimationKey()), "An animation is required in context.");
				
				CameraImpl result = new CameraImpl();
				result.animation = (Animation)context.getValue(constants.getAnimationKey());
				result.setName(WWXML.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getCameraAttributeName()));
				
				result.eyeLat = new EyeLatParameter().fromXml(WWXML.getElement(element, constants.getCameraEyeLatElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				result.eyeLon = new EyeLonParameter().fromXml(WWXML.getElement(element, constants.getCameraEyeLonElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				result.eyeElevation = new EyeElevationParameter().fromXml(WWXML.getElement(element, constants.getCameraEyeElevationElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				
				result.lookAtLat = new LookatLatParameter().fromXml(WWXML.getElement(element, constants.getCameraLookatLatElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				result.lookAtLon = new LookatLonParameter().fromXml(WWXML.getElement(element, constants.getCameraLookatLonElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				result.lookAtElevation = new LookatElevationParameter().fromXml(WWXML.getElement(element, constants.getCameraLookatElevationElementName()+ "/" + constants.getParameterElementName(), null), version, context);
				
				return result;
			}
		}
		
		return null;
	}

}
