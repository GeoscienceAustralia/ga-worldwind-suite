package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.event.AnimatableObjectEventImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.PropagatingChangeableEventListener;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.CodependantHelper;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * An abstract base class implementation of the {@link Animatable} interface.
 * <p/>
 * Provides convenience implementations of common methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public abstract class AnimatableBase extends PropagatingChangeableEventListener implements Animatable
{
	private static final long serialVersionUID = 20100823L;

	/** The name of this {@link Animatable} object */
	private String name;

	/** Whether this animatable is enabled */
	private boolean enabled = true;

	/** Whether or not this animatable is 'armed' */
	private boolean armed = true;

	private final CodependantHelper codependantHelper = new CodependantHelper(this, this);

	protected Animation animation;

	/**
	 * Constructor. Initialises the name of the animatable object.
	 */
	public AnimatableBase(String name, Animation animation)
	{
		name = name != null ? name : getDefaultName();
		Validate.notNull(name, "A name must be provided");
		Validate.notNull(animation, "An animation instance is required");
		this.name = name;
		this.animation = animation;
	}

	/**
	 * Constructor. For de-serialising. Not for general use.
	 */
	protected AnimatableBase()
	{
	}

	@Override
	public final void apply()
	{
		if (isEnabled())
		{
			doApply();
		}
	}

	/**
	 * Perform the actions required to apply this {@link Animatable}s state to
	 * the 'world'
	 */
	protected abstract void doApply();

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		Validate.notNull(name, "A name must be provided");
		this.name = name;
	}

	@Override
	public Collection<Parameter> getEnabledParameters()
	{
		Collection<Parameter> result = new ArrayList<Parameter>();
		for (Iterator<Parameter> parameterIterator = getParameters().iterator(); parameterIterator.hasNext();)
		{
			Parameter parameter = (Parameter) parameterIterator.next();
			if (parameter.isEnabled())
			{
				result.add(parameter);
			}
		}
		return result;
	}

	@Override
	public Collection<Parameter> getArmedParameters()
	{
		Collection<Parameter> result = new ArrayList<Parameter>();
		for (Iterator<Parameter> parameterIterator = getParameters().iterator(); parameterIterator.hasNext();)
		{
			Parameter parameter = (Parameter) parameterIterator.next();
			if (parameter.isArmed())
			{
				result.add(parameter);
			}
		}
		return result;
	}

	@Override
	public Collection<Parameter> getEnabledArmedParameters()
	{
		Collection<Parameter> result = new ArrayList<Parameter>();
		for (Iterator<Parameter> parameterIterator = getParameters().iterator(); parameterIterator.hasNext();)
		{
			Parameter parameter = (Parameter) parameterIterator.next();
			if (parameter.isEnabled() && parameter.isArmed())
			{
				result.add(parameter);
			}
		}
		return result;
	}

	@Override
	protected AnimationEvent createEvent(Type type, AnimationEvent cause, Object value)
	{
		return new AnimatableObjectEventImpl(this, type, cause, value);
	}

	/**
	 * Used for de-serialisation to prevent parameter 'enabled' status' being
	 * overridden
	 */
	protected void setEnabled(boolean enabled, boolean propagate)
	{
		boolean changed = this.enabled != enabled;

		this.enabled = enabled;

		if (!propagate)
		{
			return;
		}

		codependantHelper.setCodependantEnabled(enabled);

		for (Parameter parameter : getParameters())
		{
			parameter.setEnabled(enabled);
		}

		if (changed)
		{
			fireChangeEvent(enabled);
		}
	}

	@Override
	public Animation getAnimation()
	{
		return animation;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		setEnabled(enabled, true);
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public boolean isAllChildrenEnabled()
	{
		return getParameters().size() == getEnabledParameters().size();
	}

	@Override
	public boolean hasEnabledChildren()
	{
		return getEnabledParameters().size() > 0;
	}

	@Override
	public void connectCodependantEnableable(Enableable enableable)
	{
		codependantHelper.addCodependantEnableable(enableable);
	}

	@Override
	public void setArmed(boolean armed)
	{
		boolean changed = this.armed != armed;

		this.armed = armed;

		codependantHelper.setCodependantArmed(armed);

		for (Parameter parameter : getParameters())
		{
			parameter.setArmed(armed);
		}

		if (changed)
		{
			fireChangeEvent(armed);
		}
	}

	@Override
	public boolean isArmed()
	{
		return armed;
	}

	@Override
	public boolean isAllChildrenArmed()
	{
		return getParameters().size() == getArmedParameters().size();
	}

	@Override
	public boolean hasArmedChildren()
	{
		return getArmedParameters().size() > 0;
	}

	@Override
	public void connectCodependantArmable(Armable armable)
	{
		codependantHelper.addCodependantArmable(armable);
	}

	@Override
	public void connectCodependantAnimatable(Animatable animatable)
	{
		connectCodependantArmable(animatable);
		connectCodependantEnableable(animatable);
	}

	@Override
	public final Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
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

			XPath xpath = WWXML.makeXPath();
			Animation animation = (Animation) context.getValue(constants.getAnimationKey());
			String name =
					XMLUtil.getText(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeName(), xpath);
			boolean enabled =
					XMLUtil.getBoolean(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimatableAttributeEnabled(),
							true, xpath);

			AnimatableBase result = createAnimatableFromXml(name, animation, enabled, element, version, context);
			result.setEnabled(enabled, false);

			return result;
		}
		}
		return null;
	}

	@Override
	public final Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();

		Element result = WWXML.appendElement(parent, getXmlElementName(constants));
		WWXML.setTextAttribute(result, constants.getAnimatableAttributeName(), getName());
		WWXML.setBooleanAttribute(result, constants.getAnimatableAttributeEnabled(), isEnabled());

		saveAnimatableToXml(result, version);
		return result;
	}

	/**
	 * @return XML element name to use when converting this animatable to XML
	 */
	protected abstract String getXmlElementName(AnimationIOConstants constants);

	/**
	 * Create a new instance of this animatable, and set up animatable from the
	 * provided XML element.
	 * 
	 * @param name
	 *            Animatable name from XML (could be null; if null, use default
	 *            name)
	 * @param animation
	 *            Animation to associate with this animatable
	 * @param element
	 *            XML element containing this animatable
	 * @param version
	 *            Animation file version
	 * @param context
	 *            The context needed to de-serialise the object.
	 * @return A new instance of this animatable
	 */
	protected abstract AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context);

	/**
	 * @return The default name of this animatable. Called by constructor, so
	 *         don't rely on global variables being setup.
	 */
	protected abstract String getDefaultName();

	/**
	 * Save any extra data in this animatable to XML. This should handle saving
	 * any extra data in the animatable. The default implementation just saves
	 * the animtables {@link Parameter}s.
	 * 
	 * @param element
	 *            XML element to save data to
	 * @param version
	 *            Animation file version
	 */
	protected void saveAnimatableToXml(Element element, AnimationFileVersion version)
	{
		Collection<Parameter> parameters = getParameters();
		for (Parameter parameter : parameters)
		{
			parameter.toXml(element, version);
		}
	}
}
