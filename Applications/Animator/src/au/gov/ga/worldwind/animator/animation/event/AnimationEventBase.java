package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A base implementation of the {@link AnimationEvent} interface that provides
 * default implementations of most methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class AnimationEventBase implements AnimationEvent
{

	private AnimationEvent cause;
	
	private Type type;
	
	private Object source;
	
	/**
	 * @param eventType Optional. If not provided, the <code>type</code> of the provided <code>cause</code> will be used.
	 * @param source Optional.
	 * @param cause Optional. If not provided, a <code>type</code> must be.
	 */
	public AnimationEventBase(Type eventType, Object source, AnimationEvent cause)
	{
		Validate.isTrue(eventType != null || cause != null, "If no cause is provided, a type must be.");
		
		this.type = eventType;
		this.source = source;
		this.cause = cause;
		
		if (this.type == null && this.cause != null)
		{
			this.type = cause.getType();
		}
	}
	
	@Override
	public AnimationEvent getCause()
	{
		return cause;
	}

	@Override
	public AnimationEvent getCauseOfClass(Class<? extends AnimationEvent> clazz)
	{
		if (getClass().isAssignableFrom(clazz))
		{
			return this;
		}
		if (cause == null)
		{
			return null;
		}
		return cause.getCauseOfClass(clazz);
	}

	@Override
	public AnimationEvent getUltimateCause()
	{
		if (cause == null)
		{
			return this;
		}
		return cause.getUltimateCause();
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public Object getSource()
	{
		return source;
	}
	
	@Override
	public String toString()
	{
		return "Event: {" + getClass().getSimpleName() + ", " + type + ", " + source + ", " + cause + "}"; 
	}

}
