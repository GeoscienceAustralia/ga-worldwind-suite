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
			return nextKeyFrame;
		}
		
		// If there is no next key value, return the previous one
		if (nextKeyFrame == null)
		{
			return previousKeyFrame;
		}
		
		// Otherwise, 
		
		return null;
	}
	
}
