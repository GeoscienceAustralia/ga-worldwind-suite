package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An abstract base class implementation of the {@link Animatable} interface.
 * <p/>
 * Provides convenience implementations of common methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class AnimatableBase implements Animatable
{
	private static final long serialVersionUID = 20100823L;

	/** The name of this {@link Animatable} object */
	private String name; 

	/**
	 * Constructor. Initialises the name of the animatable object.
	 * 
	 * @param name
	 */
	public AnimatableBase(String name)
	{
		Validate.notNull(name, "A name must be provided");
		this.name = name;
	}

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

}
