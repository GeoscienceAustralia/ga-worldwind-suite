/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
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
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.CodependantHelper;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Base implementation of the {@link Parameter} interface.
 * <p/>
 * Provides basic implementation of many {@link Parameter} methods, along with
 * some helper methods to assist in implementing {@link Parameter} subclasses.
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
	 * The default value to use in the case that no {@link KeyFrame}s can be
	 * found with a value for this {@link Parameter}
	 */
	private double defaultValue = 0.0;

	/** The animation this parameter is associated with */
	protected Animation animation;

	/** The name of this parameter (can be used for display purposes) */
	private String name;

	private final CodependantHelper codependantHelper;

	/**
	 * Constructor. Initialises the mandatory {@link Animation} parameter.
	 */
	public ParameterBase(String name, Animation animation)
	{
		Validate.notNull(name, "A name is required");
		Validate.notNull(animation, "An animation is required");
		this.name = name;
		this.animation = animation;
		codependantHelper = new CodependantHelper(this, this);
	}

	/**
	 * Constructor. For use during de-serialisation.
	 */
	protected ParameterBase()
	{
		codependantHelper = new CodependantHelper(this, this);
	}

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

		codependantHelper.setCodependantEnabled(enabled);

		if (changed)
		{
			fireChangeEvent(enabled);
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
	public void connectCodependantEnableable(Enableable enableable)
	{
		codependantHelper.addCodependantEnableable(enableable);
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

		codependantHelper.setCodependantArmed(armed);

		if (changed)
		{
			fireChangeEvent(armed);
		}
	}

	@Override
	public boolean isAllChildrenArmed()
	{
		return true;
	}

	@Override
	public boolean hasArmedChildren()
	{
		return false;
	}

	@Override
	public void connectCodependantArmable(Armable armable)
	{
		codependantHelper.addCodependantArmable(armable);
	}

	/**
	 * Set the default value of this {@link Parameter}. This is the value this
	 * {@link Parameter} will use if no recorded {@link KeyFrame}s contain
	 * information about this {@link Parameter}. It is also the value that the
	 * parameter will be set to at the beginning of the animation, before any
	 * key frames have been recorded.
	 * 
	 * @param value
	 *            The default value of this this {@link Parameter}
	 */
	protected final void setDefaultValue(double value)
	{
		this.defaultValue = value;
	}

	@Override
	public double getDefaultValue(int frame)
	{
		return this.defaultValue;
	}

	@Override
	public final void applyValueIfEnabled(double value, int frame)
	{
		if (isEnabled())
		{
			doApplyValue(value);
		}
		else
		{
			doApplyValue(getDefaultValue(animation.getCurrentFrame()));
		}
	}

	@Override
	public final void applyValueAnyway(double value)
	{
		doApplyValue(value);
	}

	/**
	 * Invoked when the {@link #applyValue(double)} method is called and the
	 * parameter is enabled.
	 */
	protected abstract void doApplyValue(double value);

	@Override
	public final ParameterValue getValueAtFrame(int frame)
	{
		// Interpolate between the two surrounding frames
		KeyFrame previousKeyFrame = animation.getKeyFrameWithParameterBeforeFrame(this, frame, true);
		KeyFrame nextKeyFrame = animation.getKeyFrameWithParameterAfterFrame(this, frame, false);
		return calculateInterpolatedParameterValue(frame, previousKeyFrame, nextKeyFrame);
	}

	@Override
	public ParameterValue[] getValuesBetweenFrames(int startFrame, int endFrame, ParameterValue[] array)
	{
		Validate.isTrue(startFrame <= endFrame, "End frame must not be less than start frame");

		if (array == null || array.length < (endFrame - startFrame + 1))
		{
			array = new ParameterValue[endFrame - startFrame + 1];
		}

		KeyFrame previousKeyFrame = null;
		KeyFrame nextKeyFrame = null;
		for (int frame = startFrame; frame <= endFrame; frame++)
		{
			//if the frame is outside of the bounds of previous and next, then recalculate
			if (frame == startFrame || (nextKeyFrame != null && frame >= nextKeyFrame.getFrame()))
			{
				previousKeyFrame = animation.getKeyFrameWithParameterBeforeFrame(this, frame, true);
				nextKeyFrame = animation.getKeyFrameWithParameterAfterFrame(this, frame, false);
			}
			array[frame - startFrame] = calculateInterpolatedParameterValue(frame, previousKeyFrame, nextKeyFrame);
		}

		return array;
	}

	/**
	 * Invoked by the {@link ParameterBase#getValueAtFrame(int)} and
	 * {@link ParameterBase#getValuesBetweenFrames(int, int, ParameterValue[])}
	 * functions.
	 */
	protected ParameterValue calculateInterpolatedParameterValue(int frame, KeyFrame previousKeyFrame,
			KeyFrame nextKeyFrame)
	{
		// If no key values exist, return the default value
		if (previousKeyFrame == null && nextKeyFrame == null)
		{
			return ParameterValueFactory.createParameterValue(this, getDefaultValue(frame), frame);
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

		//don't need to interpolate if at either end
		if (percent <= 0)
		{
			return previousKeyFrame.getValueForParameter(this);
		}
		if (percent >= 1)
		{
			return nextKeyFrame.getValueForParameter(this);
		}

		Interpolator<Vector2> interpolator =
				InterpolatorFactory.getInterpolator(previousKeyFrame.getValueForParameter(this),
						nextKeyFrame.getValueForParameter(this));
		double interpolatedValue = interpolator.computeValue(percent).y;

		return ParameterValueFactory.createParameterValue(this, interpolatedValue, frame);
	}

	/**
	 * Calculate where the <code>target</code> lies on the interval
	 * <code>[start,end]</code> as a percentage of the interval.
	 * 
	 * @param start
	 *            The start of the interval
	 * @param end
	 *            The end of the interval
	 * @param target
	 *            The target point on the interval for which the percentage is
	 *            required
	 * 
	 * @return where the <code>target</code> lies on the interval
	 *         <code>[start,end]</code> as a percentage of the interval.
	 */
	private static double calculatePercentOfInterval(int start, int end, int target)
	{
		return ((double) (target - start)) / ((double) (end - start));
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

	@Override
	public Animation getAnimation()
	{
		return animation;
	}

	@Override
	public void connectCodependantParameter(Parameter parameter)
	{
		connectCodependantArmable(parameter);
		connectCodependantEnableable(parameter);
	}

	@Override
	public final Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();

		Element result = WWXML.appendElement(parent, getXmlElementName(constants));
		Element parameterElement = WWXML.appendElement(result, constants.getParameterElementName());

		WWXML.setTextAttribute(parameterElement, constants.getParameterAttributeName(), getName());
		WWXML.setDoubleAttribute(parameterElement, constants.getParameterAttributeDefaultValue(), defaultValue);
		WWXML.setBooleanAttribute(parameterElement, constants.getParameterAttributeEnabled(), enabled);

		List<KeyFrame> keyFrames = getKeyFramesWithThisParameter();
		for (KeyFrame keyFrame : keyFrames)
		{
			parameterElement.appendChild(keyFrame.getValueForParameter(this).toXml(parameterElement, version));
		}

		saveParameterToXml(result, parameterElement, version);

		return result;
	}

	@Override
	public final Parameter fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");

		AnimationIOConstants constants = version.getConstants();
		Element parameterElement = WWXML.getElement(element, "./" + constants.getParameterElementName(), null);
		Validate.notNull(parameterElement, "<" + constants.getParameterElementName() + "> element not found");

		switch (version)
		{
		case VERSION020:
		{
			Validate.isTrue(context.hasKey(constants.getAnimationKey()), "An animation is required in context.");

			Animation animation = (Animation) context.getValue(constants.getAnimationKey());
			String name =
					WWXML.getText(parameterElement, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeName());
			ParameterBase result = createParameterFromXml(name, animation, element, parameterElement, version, context);

			result.setDefaultValue(WWXML.getDouble(parameterElement,
					ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeDefaultValue(), null));
			result.setEnabled(WWXML.getBoolean(parameterElement,
					ATTRIBUTE_PATH_PREFIX + constants.getParameterAttributeEnabled(), null));

			// Create a parameter value for each child element
			// Insert it as a key frame (relies on key frames being merged)
			context.setValue(constants.getParameterValueOwnerKey(), result);
			Element[] parameterValueElements =
					WWXML.getElements(parameterElement, constants.getParameterValueElementName(), null);
			if (parameterValueElements != null)
			{
				for (Element e : parameterValueElements)
				{
					ParameterValue v = ParameterValueFactory.fromXml(e, version, context);
					result.animation.insertKeyFrame(new KeyFrameImpl(v.getFrame(), v), false);
				}
			}

			return result;
		}
		}
		return null;
	}

	/**
	 * @return XML element name to use when converting this parameter to XML
	 *         (the &lt;parameter&gt; element's parent)
	 */
	protected abstract String getXmlElementName(AnimationIOConstants constants);

	/**
	 * Create a new instance of this parameter, and set up parameter from the
	 * provided XML element.
	 * 
	 * @param name
	 *            Parameter name from XML (could be null; if null, use default
	 *            name)
	 * @param animation
	 *            Animation to associate with this parameter
	 * @param element
	 *            XML element containing this parameter
	 * @param parameterElement
	 *            Child &lt;parameter&gt; XML element
	 * @param version
	 *            Animation file version
	 * @param context
	 *            The context needed to de-serialise the object.
	 * @return A new instance of this parameter
	 */
	protected abstract ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context);

	/**
	 * Save any extra data in this parameter to XML. This does nothing by
	 * default; subclasses can override if they contain extra data to serialize.
	 * 
	 * @param element
	 *            XML element to save data to
	 * @param version
	 *            Animation file version
	 */
	protected void saveParameterToXml(Element element, Element parameterElement, AnimationFileVersion version)
	{
	}

	/**
	 * @return name if not null, otherwise defaultName
	 */
	protected static String nameOrDefaultName(String name, String defaultName)
	{
		if (name != null)
		{
			return name;
		}
		return defaultName;
	}

	@Override
	protected AnimationEvent createEvent(Type type, AnimationEvent cause, Object value)
	{
		return new ParameterEventImpl(this, type, cause, value);
	}
}
