package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.camera.CameraImpl;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An implementation of the {@link Animation} interface for animations using the
 * WorldWind SDK.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class WorldWindAnimationImpl implements Animation
{
	/** The default number of frames for an animation */
	private static final int DEFAULT_FRAME_COUNT = 100;
	
	/** Map of <code>frame -> key frame</code>, ordered by frame, for quick lookup of key frames */
	private SortedMap<Integer, KeyFrame> keyFrameMap = new TreeMap<Integer, KeyFrame>();
	
	/** The number of frames in this animation */
	private int frameCount;
	
	/** The render parameters for this animation */
	private RenderParameters renderParameters;
	
	/** The camera to use for rendering the animation */
	private Camera renderCamera;
	
	/** The list of animatable objects in this animation */
	private List<Animatable> animatableObjects = new ArrayList<Animatable>();
	
	/** Whether or not zoom scaling should be applied */
	private boolean zoomRequired = true;
	
	/** The current frame of the animation */
	private int currentFrame = 0;
	
	/** The worldwind window */
	private WorldWindow worldWindow;
	
	/**
	 * Constructor. Initialises default values.
	 */
	public WorldWindAnimationImpl(WorldWindow worldWindow)
	{
		Validate.notNull(worldWindow, "A world window is required");
		this.worldWindow = worldWindow;
		this.frameCount = DEFAULT_FRAME_COUNT;
		this.renderParameters = new RenderParameters();
		this.renderCamera = new CameraImpl(this);
		this.animatableObjects.add(renderCamera);
	}
	
	@Override
	public int getKeyFrameCount()
	{
		return keyFrameMap.size();
	}
	
	@Override
	public boolean hasKeyFrames()
	{
		return !keyFrameMap.isEmpty();
	}
	
	@Override
	public List<KeyFrame> getKeyFrames()
	{
		return new ArrayList<KeyFrame>(keyFrameMap.values());
	}
	
	@Override
	public KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame)
	{
		// Note: The ArrayList is used to achieve good random access performance so we can iterate backwards.
		// This is not required for the forward-looking version of this method
		List<KeyFrame> candidateKeys = new ArrayList<KeyFrame>(keyFrameMap.headMap(frame).values());
		for (int i = candidateKeys.size()-1; i >= 0; i--)
		{
			KeyFrame candidateKey = candidateKeys.get(i);
			if (candidateKey.hasValueForParameter(p))
			{
				return candidateKey;
			}
		}
		return null;
	}

	@Override
	public KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame)
	{
		Collection<KeyFrame> candidateKeys = keyFrameMap.tailMap(frame + 1).values();
		for (KeyFrame candidateKey : candidateKeys)
		{
			if (candidateKey.hasValueForParameter(p))
			{
				return candidateKey;
			}
		}
		return null;
	}

	@Override
	public KeyFrame getFirstKeyFrame()
	{
		List<KeyFrame> keyFrames = getKeyFrames();
		return keyFrames.isEmpty() ? null : keyFrames.get(0);
	}
	
	@Override
	public int getFrameOfFirstKeyFrame()
	{
		return keyFrameMap.isEmpty() ? 0 : keyFrameMap.firstKey();
	}
	
	@Override
	public KeyFrame getLastKeyFrame()
	{
		List<KeyFrame> keyFrames = getKeyFrames();
		return keyFrames.isEmpty() ? null : keyFrames.get(keyFrames.size() - 1);
	}
	
	@Override
	public int getFrameOfLastKeyFrame()
	{
		return keyFrameMap.isEmpty() ? 0 : keyFrameMap.lastKey();
	}
	
	@Override
	public Collection<Parameter> getAllParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getParameters());
		}
		return result;
	}

	@Override
	public Collection<Parameter> getEnabledParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getEnabledParameters());
		}
		return result;
	}

	@Override
	public Camera getCamera()
	{
		return this.renderCamera;
	}
	
	@Override
	public Collection<Animatable> getAnimatableObjects()
	{
		return this.animatableObjects;
	}

	@Override
	public int getFrameCount()
	{
		return this.frameCount;
	}

	@Override
	public void setFrameCount(int newCount)
	{
		// If we are decreasing the frame count, remove any keyframes after the new frame count
		if (newCount < frameCount)
		{
			this.keyFrameMap = this.keyFrameMap.headMap(newCount);
		}
		this.frameCount = newCount;
		
	}

	@Override
	public void applyFrame(int frame)
	{
		setCurrentFrame(frame);
		AnimationContext context = createAnimationContext();
		for (Animatable animatable : animatableObjects)
		{
			animatable.apply(context, frame);
		}
	}

	@Override
	public void recordKeyFrame(int frame)
	{
		recordKeyFrame(frame, getEnabledParameters());
		
	}

	@Override
	public void recordKeyFrame(int frame, Collection<Parameter> parameters)
	{
		setCurrentFrame(frame);
		AnimationContext animationContext = createAnimationContext();
		Collection<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
		for (Parameter parameter : parameters)
		{
			parameterValues.add(parameter.getCurrentValue(animationContext));
		}
		
		// If there are no parameter values to record, do nothing
		if (parameterValues.isEmpty())
		{
			return;
		}
		
		insertKeyFrame(new KeyFrameImpl(frame, parameterValues));
		
		Logging.logger().log(Level.FINER, "WorldWindAnimationImpl::recordKeyFrame - Recorded frame " + frame + ": " + this.keyFrameMap.get(frame));
	}
	
	@Override
	public void insertKeyFrame(KeyFrame keyFrame)
	{
		if (keyFrame == null)
		{
			return;
		}
		
		setCurrentFrame(keyFrame.getFrame());
		
		// If a key frame already exists at this frame, merge the parameter values
		// Otherwise, create a new key frame
		if (this.keyFrameMap.containsKey(keyFrame.getFrame()))
		{
			KeyFrame existingFrame = this.keyFrameMap.get(keyFrame.getFrame());
			existingFrame.addParameterValues(keyFrame.getParameterValues());
		} 
		else
		{
			this.keyFrameMap.put(keyFrame.getFrame(), keyFrame);
		}
		
		// Smooth the key frames around this one
		smoothKeyFrames(keyFrame);
	}
	
	/**
	 * Smooth the transition into- and out-of the provided key frame
	 * <p/>
	 * Applies value smoothing to the bezier values of the provided key frames, 
	 * as well as those in key frames on either side of the frame.
	 * 
	 * @param keyFrame
	 */
	private void smoothKeyFrames(KeyFrame keyFrame)
	{
		for (ParameterValue parameterValue : keyFrame.getParameterValues())
		{
			if (parameterValue instanceof BezierParameterValue)
			{
				((BezierParameterValue)parameterValue).smooth();
			}
			
			// Smooth the previous value for this parameter
			KeyFrame previousFrame = getKeyFrameWithParameterBeforeFrame(parameterValue.getOwner(), keyFrame.getFrame());
			if (previousFrame != null)
			{
				ParameterValue previousValue = previousFrame.getValueForParameter(parameterValue.getOwner());
				if (previousValue instanceof BezierParameterValue)
				{
					((BezierParameterValue)previousValue).smooth();
				}
				
			}
			
			// Smooth the next value for this parameter
			KeyFrame nextFrame = getKeyFrameWithParameterAfterFrame(parameterValue.getOwner(), keyFrame.getFrame());
			if (nextFrame != null)
			{
				ParameterValue nextValue = nextFrame.getValueForParameter(parameterValue.getOwner());
				if (nextValue instanceof BezierParameterValue)
				{
					((BezierParameterValue)nextValue).smooth();
				}
				
			}
			
		}
	}
	
	/**
	 * @return An animation context that reflects the current state of the animation
	 */
	private AnimationContext createAnimationContext()
	{
		return new AnimationContextImpl(this);
	}

	@Override
	public RenderParameters getRenderParameters()
	{
		return renderParameters;
	}

	@Override
	public boolean isZoomScalingRequired()
	{
		return zoomRequired;
	}

	@Override
	public void setZoomScalingRequired(boolean zoomScalingRequired)
	{
		this.zoomRequired = zoomScalingRequired;
	}

	@Override
	public int getCurrentFrame()
	{
		return this.currentFrame;
	}

	@Override
	public void setCurrentFrame(int frame)
	{
		this.currentFrame = frame;
	}

	/**
	 * @return The WorldWindow used in this animation 
	 */
	public WorldWindow getWorldWindow()
	{
		return this.worldWindow;
	}

	@Override
	public boolean hasKeyFrame(int frame)
	{
		return this.keyFrameMap.containsKey(frame);
	}

	@Override
	public boolean hasKeyFrame(Parameter p)
	{
		for (KeyFrame keyFrame : getKeyFrames())
		{
			if (keyFrame.hasValueForParameter(p))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public KeyFrame getKeyFrame(int frame)
	{
		return this.keyFrameMap.get(frame);
	}
	
}
