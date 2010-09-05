package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.ChangeableBase;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An abstract base class implementation of the {@link Animatable} interface.
 * <p/>
 * Provides convenience implementations of common methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class AnimatableBase extends ChangeableBase implements Animatable
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

	/**
	 * Constructor. For de-serialising. Not for general use.
	 */
	protected AnimatableBase()
	{
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
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		// Propagate the change upwards
		List<ChangeListener> listeners = getChangeListeners();
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).stateChanged(e);
		}
	}

}
