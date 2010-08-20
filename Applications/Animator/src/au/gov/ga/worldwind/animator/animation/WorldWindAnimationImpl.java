package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

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
	
	/** The list of animatable objects in this animation */
	private List<Animatable> animatableObjects = new ArrayList<Animatable>();
	
	/**
	 * Constructor. Initialises default values.
	 */
	public WorldWindAnimationImpl()
	{
		this.frameCount = DEFAULT_FRAME_COUNT;
		this.renderParameters = new RenderParameters();
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
		Collection<KeyFrame> candidateKeys = keyFrameMap.tailMap(frame).values();
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
		Collection<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
		for (Parameter parameter : parameters)
		{
			parameterValues.add(parameter.getCurrentValue(createAnimationContext()));
		}
		
		// If a key frame already exists at this frame, merge the parameter values
		// Otherwise, create a new key frame
		if (this.keyFrameMap.containsKey(frame))
		{
			KeyFrame existingFrame = this.keyFrameMap.get(frame);
			existingFrame.addParameterValues(parameterValues);
		} 
		else
		{
			KeyFrame newFrame = new KeyFrameImpl(frame, parameterValues);
			this.keyFrameMap.put(frame, newFrame);
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

	

}
