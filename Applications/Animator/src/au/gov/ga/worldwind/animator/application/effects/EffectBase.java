package au.gov.ga.worldwind.animator.application.effects;

import gov.nasa.worldwind.avlist.AVList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.common.util.Validate;

public abstract class EffectBase extends AnimatableBase implements Effect
{
	protected final List<Parameter> parameters = new ArrayList<Parameter>();
	
	public EffectBase(String name, Animation animation)
	{
		super(name, animation);
	}
	
	protected EffectBase()
	{
		super();
		setName(getDefaultName());
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public void addParameter(EffectParameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		Validate.isTrue(this.equals(parameter.getEffect()), "Parameter is not linked to the correct layer. Expected '"
				+ this + "'.");
		parameters.add(parameter);

		parameter.addChangeListener(this);
	}

	@Override
	protected void doApply()
	{
		for (Parameter parameter : parameters)
		{
			Validate.isTrue(parameter instanceof EffectParameter, "Incorrect Parameter type"); //should never occur
			((EffectParameter) parameter).apply();
		}
	}

	@Override
	protected final AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		EffectBase effect = createEffectFromXml(name, animation, enabled, element, version, context);
		context.setValue(constants.getCurrentEffectKey(), effect);
		return effect;
	}

	/**
	 * Create a new instance of this effect, and set up effect from the provided
	 * XML element.
	 * 
	 * @param name
	 *            Effect name from XML (could be null; if null, use default
	 *            name)
	 * @param animation
	 *            Animation to associate with this effect
	 * @param element
	 *            XML element containing this effect
	 * @param version
	 *            Animation file version
	 * @param context
	 *            The context needed to de-serialise the object.
	 * @return A new instance of this effect
	 */
	protected abstract EffectBase createEffectFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context);
}
