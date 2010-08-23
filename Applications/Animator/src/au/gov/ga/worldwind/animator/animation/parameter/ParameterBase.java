/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.math.vector.Vector2;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * Base implementation of the {@link Parameter} interface.
 * <p/>
 * Provides basic implementation of many {@link Parameter} methods, along with some 
 * helper methods to assist in implementing {@link Parameter} subclasses.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class ParameterBase implements Parameter
{
	private static final long serialVersionUID = 20100819L;

	/** Whether this parameter is currently enabled */
	private boolean enabled = true;

	/** 
	 * The default value to use in the case that no {@link KeyFrame}s can be found
	 * with a value for this {@link Parameter}
	 */
	private double defaultValue = 0.0;
	
	/** The animation this parameter is associated with */
	private Animation animation;

	/** The name of this parameter (can be used for display purposes) */
	private String name;
	
	/**
	 * Constructor. Initialises the mandatory {@link Animation} parameter.
	 */
	public ParameterBase(String name, Animation animation)
	{
		Validate.notNull(name, "A name is required");
		Validate.notNull(animation, "An animation is required");
		this.name = name;
		this.animation = animation;
	}
	
	@Override
	public final boolean isEnabled()
	{
		return enabled;
	}
	
	@Override
	public final void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	@Override
	public final void setDefaultValue(double value)
	{
		this.defaultValue = value;
	}
	
	@Override
	public final ParameterValue getValueAtFrame(AnimationContext context, int frame)
	{
		KeyFrame previousKeyFrame = context.getKeyFrameWithParameterBeforeFrame(this, frame);
		KeyFrame nextKeyFrame = context.getKeyFrameWithParameterAfterFrame(this, frame);
		
		// If no key values exist, return the default value
		if (previousKeyFrame == null && nextKeyFrame == null)
		{
			return ParameterValueFactory.createParameterValue(this, defaultValue, frame);
		}
		
		// If there is no previous key value, return the next one
		if (previousKeyFrame == null) 
		{
			return nextKeyFrame.getValueForParameter(this);
		}
		
		// If there is no next key value, return the previous one
		if (nextKeyFrame == null)
		{
			return previousKeyFrame.getValueForParameter(this);
		}
		
		// Otherwise, use an interpolator to interpolate between the two values
		double percent = calculatePercentOfInterval(previousKeyFrame.getFrame(), nextKeyFrame.getFrame(), frame);
		Interpolator<Vector2> interpolator = InterpolatorFactory.getInterpolator(previousKeyFrame.getValueForParameter(this), nextKeyFrame.getValueForParameter(this));
		double interpolatedValue = interpolator.computeValue(percent).y;
		
		return ParameterValueFactory.createParameterValue(this, interpolatedValue, frame);
	}

	/**
	 * Calculate where the <code>target</code> lies on the interval <code>[start,end]</code>
	 * as a percentage of the interval.
	 * 
	 * @param start The start of the interval
	 * @param end The end of the interval
	 * @param target The target point on the interval for which the percentage is required
	 * 
	 * @return where the <code>target</code> lies on the interval <code>[start,end]</code>
	 * as a percentage of the interval.
	 */
	private static double calculatePercentOfInterval(int start, int end, int target)
	{
		return (target - start)/(end - start);
	}
	
	@Override
	public final String getRestorableState()
	{
		// TODO Implement me!
		return null;
	}

	@Override
	public final void restoreState(String stateInXml)
	{
		// TODO Implement me!

	}
	
	@Override
	public ParameterValue getValueAtKeyFrameBeforeFrame(int frame)
	{
		KeyFrame keyFrame = animation.getKeyFrameWithParameterBeforeFrame(this, frame);
		if (keyFrame == null)
		{
			return null;
		}
		return keyFrame.getValueForParameter(this);
	}
	
	@Override
	public ParameterValue getValueAtKeyFrameAfterFrame(int frame)
	{
		KeyFrame keyFrame = animation.getKeyFrameWithParameterAfterFrame(this, frame);
		if (keyFrame == null)
		{
			return null;
		}
		return keyFrame.getValueForParameter(this);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		Validate.notNull(name, "A name is required");
		this.name = name;
		
	}
}
