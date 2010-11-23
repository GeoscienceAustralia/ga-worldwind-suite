package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import au.gov.ga.worldwind.animator.animation.event.AnimatableObjectEventImpl;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;
import au.gov.ga.worldwind.animator.animation.event.PropagatingChangeableEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.CodependantHelper;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.common.util.Validate;

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
	
	private final CodependantHelper codependantHelper;
	
	/**
	 * Constructor. Initialises the name of the animatable object.
	 */
	public AnimatableBase(String name)
	{
		Validate.notNull(name, "A name must be provided");
		this.name = name;
		codependantHelper = new CodependantHelper(this, this);
	}

	/**
	 * Constructor. For de-serialising. Not for general use.
	 */
	protected AnimatableBase()
	{
		codependantHelper = new CodependantHelper(this, this);
	}

	@Override
	public final void apply(AnimationContext animationContext, int frame)
	{
		if (isEnabled())
		{
			doApply(animationContext, frame);
		}
	}

	/**
	 * Perform the actions required to apply this {@link Animatable}s state to the 'world'
	 */
	protected abstract void doApply(AnimationContext animationContext, int frame);

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
	 * Used for de-serialisation to prevent parameter 'enabled' status' being overridden
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
}
