/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.ParameterEventImpl;
import au.gov.ga.worldwind.animator.animation.event.PropagatingChangeableEventListener;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
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
public abstract class ParameterBase extends PropagatingChangeableEventListener implements Parameter
{
	private static final long serialVersionUID = 20100819L;

	/** Whether this parameter is currently enabled */
	private boolean enabled = true;

	/** Whether this parameter is currently armed */
	private boolean armed = true;
	
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
	
	/**
	 * Constructor. For use during de-serialisation.
	 */
	protected ParameterBase(){}
	
	@Override
	public final boolean isEnabled()
	{
		return enabled;
	}
	
	@Override
	public final void setEnabled(boolean enabled)
	{
		boolean changed = this.enabled != enabled;
		this.enabled = enabled;
		
		if (changed)
		{
			fireChangeEvent(enabled);
		}
	}
	
	@Override
	public boolean isArmed()
	{
		return armed;
	}
	
	@Override
	public void setArmed(boolean armed)
	{
		boolean changed = this.armed != armed;
		this.armed = armed;
		
		if (changed)
		{
			fireChangeEvent(armed);
		}
	}
	
	@Override
	public boolean isAllChildrenEnabled()
	{
		return true;
	}
	
	@Override
	public boolean hasEnabledChildren()
	{
		return false;
	}
	
	@Override
	public final void setDefaultValue(double value)
	{
		this.defaultValue = value;
	}
	
	@Override
	public double getDefaultValue()
	{
		return this.defaultValue;
	}
	
	@Override
	public final void applyValue(double value)
	{
		if (isEnabled())
		{
			doApplyValue(value);
		}
		else
		{
			doApplyValue(getDefaultValue());
		}
	}
	
	/**
	 * Invoked when the {@link #applyValue(double)} method is called and the parameter is enabled.
	 */
	protected abstract void doApplyValue(double value);

	@Override
	public final ParameterValue getValueAtFrame(AnimationContext context, int frame)
	{
		// If the provided value is a keyframe for this parameter, return it's value from the key frame
		KeyFrame keyAtFrame = animation.getKeyFrame(frame);
		if (keyAtFrame != null && keyAtFrame.hasValueForParameter(this))
		{
			return keyAtFrame.getValueForParameter(this);
		}
		
		// Otherwise, interpolate between the two surrounding frames
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
		return ((double)(target - start))/((double)(end - start));
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
	public List<KeyFrame> getKeyFramesWithThisParameter()
	{
		return animation.getKeyFrames(this);
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
	
	protected Animation getAnimation()
	{
		return animation;
	}
	
	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, "parameter");
		
		WWXML.setTextAttribute(result, constants.getParameterAttributeName(), getName());
		WWXML.setDoubleAttribute(result, constants.getParameterAttributeDefaultValue(), defaultValue);
		WWXML.setBooleanAttribute(result, constants.getParameterAttributeEnabled(), enabled);
		
		List<KeyFrame> keyFrames = getKeyFramesWithThisParameter();
		for (KeyFrame keyFrame : keyFrames)
		{
			result.appendChild(keyFrame.getValueForParameter(this).toXml(result, version));
		}
		
		return result;
	}
	
	@Override
	public Parameter fromXml(Element element, AnimationFileVersion version, AVList context)
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
				
				ParameterBase result = createParameter(context);
				result.animation = (Animation)context.getValue(constants.getAnimationKey());
				result.setDefaultValue(WWXML.getDouble(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeDefaultValue(), null));
				result.setName(WWXML.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeName()));
				result.setEnabled(WWXML.getBoolean(element, ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeEnabled(), null));
				
				// Create a parameter value for each child element
				// Insert it as a key frame (relies on key frames being merged)
				context.setValue(constants.getParameterValueOwnerKey(), result);
				Element[] parameterValueElements = WWXML.getElements(element, constants.getParameterValueElementName(), null);
				if (parameterValueElements == null)
				{
					return result;
				}
				
				for (Element e : parameterValueElements)
				{
					ParameterValue v = ParameterValueFactory.fromXml(e, version, context);
					result.animation.insertKeyFrame(new KeyFrameImpl(v.getFrame(), v), false);
				}
				
				return result;
			}
		}
		return null;
	}
	
	/**
	 * @return A new instance of this parameter
	 */
	protected abstract ParameterBase createParameter(AVList context);
	
	@Override
	protected AnimationEvent createEvent(Type type, AnimationEvent cause, Object value)
	{
		return new ParameterEventImpl(this, type, cause, value);
	}
}
