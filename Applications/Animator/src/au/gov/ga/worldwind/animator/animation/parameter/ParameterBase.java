/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.math.vector.Vector;

/**
 * Base implementation of the {@link Parameter} interface.
 * <p/>
 * Provides basic implementation of many {@link Parameter} methods, along with some 
 * helper methods to assist in implementing {@link Parameter} subclasses.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class ParameterBase<V extends Vector<V>> implements Parameter<V>
{

	/** Whether this parameter is currently enabled */
	private boolean enabled;

	/** 
	 * The default value to use in the case that no {@link KeyFrame}s can be found
	 * with a value for this {@link Parameter}
	 */
	private ParameterValue<V> defaultValue;
	
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
	public final void setDefaultValue(ParameterValue<V> value)
	{
		this.defaultValue = value;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final ParameterValue<V> getValueAtFrame(AnimationContext context, int frame)
	{
		KeyFrame previousKeyFrame = context.getKeyFrameWithParameterBeforeFrame(this, frame);
		KeyFrame nextKeyFrame = context.getKeyFrameWithParameterAfterFrame(this, frame);
		
		// If no key values exist, return the default value
		if (previousKeyFrame == null && nextKeyFrame == null)
		{
			return defaultValue;
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
		
		// Otherwise, use the configured interpolator to interpolate between the two values
		double percent = calculatePercentOfInterval(previousKeyFrame.getFrame(), nextKeyFrame.getFrame(), frame);
		initialiseInterpolator(previousKeyFrame.getValueForParameter(this), nextKeyFrame.getValueForParameter(this));
		V interpolatedValue = getInterpolator().computeValue(percent);
		return new BasicParameterValue<V>(interpolatedValue, this);
	}

	/**
	 * Perform any required initialisation of the configured interpolator
	 * 
	 * @param startValue The value to use as the start of the interpolation (0%)
	 * @param endValue The value to use as the end of the interpolation (100%)
	 */
	protected abstract void initialiseInterpolator(ParameterValue<V> startValue, ParameterValue<V> endValue);

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
	
}
