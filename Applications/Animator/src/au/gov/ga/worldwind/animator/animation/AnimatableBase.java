package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	 * The list of registered change listeners
	 */
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	
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
	public void addChangeListener(ChangeListener changeListener)
	{
		if (changeListener == null)
		{
			return;
		}
		this.changeListeners.add(changeListener);
	}
	
	@Override
	public void removeChangeListener(ChangeListener changeListener)
	{
		if (changeListener == null)
		{
			return;
		}
		this.changeListeners.remove(changeListener);
	}
	
	@Override
	public void notifyChange()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener listener : changeListeners)
		{
			listener.stateChanged(event);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		/// Propagate the change upwards
		for (ChangeListener listener : changeListeners)
		{
			listener.stateChanged(e);
		}
	}

}
