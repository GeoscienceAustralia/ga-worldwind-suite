/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import java.io.Serializable;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * A {@link Parameter} represents a single animatable property of some {@link Animatable} object (e.g. Camera position, layer opacity etc.).
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Parameter extends AnimationObject, Serializable, Nameable, XmlSerializable<Parameter>, AnimationEventListener, Changeable, Enableable
{
	
	/**
	 * Return whether this parameter is currently enabled for the current {@link Animation}.
	 * <p/>
	 * Only enabled parameters will be recorded when creating new key frames.
	 * 
	 * @return whether this parameter is currently enabled
	 */
	boolean isEnabled();
	
	/**
	 * Set whether this parameter is currently enabled for the current {@link Animation}.
	 * <p/>
	 * Only enabled parameters will be recorded when creating new key frames.
	 * 
	 * @param enabled Whether this parameter is currently enabled
	 */
	void setEnabled(boolean enabled);
	
	/**
	 * Returns whether this parameter is 'armed' (i.e. whether it should be included in the creation of key frames or not).
	 * <p/>
	 * Different from 'enabled' in that a 'disarmed' parameter is still included in the animation, just not in the creation of key frames.
	 */
	boolean isArmed();
	
	/**
	 * Set whether or not this parameter is 'armed'.
	 * 
	 * @see #isArmed()
	 */
	void setArmed(boolean armed);
	
	/**
	 * Get the current value of this {@link Parameter} in the current {@link AnimationContext}.
	 * 
	 * @param context The context of the current animation
	 * 
	 * @return the current value of this {@link Parameter} in the current {@link AnimationContext}.
	 */
	ParameterValue getCurrentValue(AnimationContext context);
	
	/**
	 * Get the value of this {@link Parameter} at the provided frame.
	 * <p/>
	 * If there are no {@link KeyFrame}s recorded that contain information for this {@link Parameter},
	 * the default {@link ParameterValue} will be returned. If the default value is <code>null</code>, 
	 * this method may return <code>null</code>.
	 * <p/> 
	 * If the provided frame is <em>before</em> the <em>first</em> recorded {@link KeyFrame} that contains information about this
	 * {@link Parameter}, the {@link ParameterValue} recorded on that <em>first</em> {@link KeyFrame} will be returned.
	 * Similarly, if the provided frame is <em>after</em> the <em>last</em> {@link KeyFrame} with a recorded {@link ParameterValue},
	 * the {@link ParameterValue} on that {@link KeyFrame} will be returned.
	 * <p/>
	 * If the provided frame lies between two {@link KeyFrame}s with {@link ParameterValue}s recorded for this {@link Parameter},
	 * an interpolated {@link ParameterValue} will be returned, with interpolation performed using an {@link Interpolator} determined
	 * by the type of parameter value.
	 * 
	 * @param context The context of the current animation
	 * @param frame The frame for which the value of the parameter is required
	 * 
	 * @return The value of this {@link Parameter} at the provided frame.
	 */
	ParameterValue getValueAtFrame(AnimationContext context, int frame);
	
	/**
	 * Set the default value of this {@link Parameter}. This is the value this {@link Parameter} will use if no recorded {@link KeyFrame}s
	 * contain information about this {@link Parameter}. It is also the value that the parameter will be set to at the beginning of the animation, 
	 * before any key frames have been recorded.
	 * 
	 * @param value The default value of this this {@link Parameter}
	 */
	void setDefaultValue(double value);
	
	/**
	 * @return The default value of this {@link Parameter}.
	 * 
	 * @see #setDefaultValue(double)
	 */
	double getDefaultValue();
	
	/**
	 * Apply the provided value to the property this parameter controls.
	 * @param value
	 */
	void applyValue(double value);
	
	/**
	 * Gets the first recorded value of this parameter on a key frame before the provided frame.
	 * <p/>
	 * If one cannot be found, returns <code>null</code>.
	 * 
	 * @param frame The frame before which to retrieve the value
	 * 
	 * @return the first recorded value of this parameter on a key frame before the 
	 * provided frame, or <code>null</code> if one cannot be found
	 */
	ParameterValue getValueAtKeyFrameBeforeFrame(int frame);
	
	/**
	 * Gets the first recorded value of this parameter on a key frame after the provided frame.
	 * <p/>
	 * If one cannot be found, returns <code>null</code>.
	 * 
	 * @param frame The frame after which to retrieve the value
	 * 
	 * @return the first recorded value of this parameter on a key frame after the 
	 * provided frame, or <code>null</code> if one cannot be found
	 */
	ParameterValue getValueAtKeyFrameAfterFrame(int frame);
	
	/**
	 * Gets the ordered list of key frames that contain a recorded value for this parameter
	 * <p/>
	 * If no key frames exist with a value for this parameter, an empty list will be returned.
	 * 
	 * @return The key frames that contain a recorded value for this parameter.
	 */
	List<KeyFrame> getKeyFramesWithThisParameter();
}
