package au.gov.ga.worldwind.animator.animation.event;

/**
 * A super-interface for events that can occur within an animation.
 * <p/>
 * Supports a causal chain that can be queried by listeners interested in particular event types.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationEvent
{

	/**
	 * Gets the event that triggered this event to be fired (if any).
	 * 
	 * @return the event that triggered this event to be fired, or <code>null</code> if one does not exist.
	 */
	AnimationEvent getCause();
	
	/**
	 * Gets the <em>first</em> event in the causal chain of the given class.
	 * <p/>
	 * If this event is of the provided type, will return this event.
	 * <p/>
	 * If there is no event in the causal chain of the given class, will return <code>null</code>.
	 * 
	 * @param clazz The class of event to retrieve from the causal chain.
	 * 
	 * @return the <em>first</em> event in the causal chain of the given class, or <code>null</code> if no event
	 * of the given class exists in the causal chain.
	 */
	AnimationEvent getCauseOfClass(Class<? extends AnimationEvent> clazz);
	
	/**
	 * Gets the last event in the causal chain.
	 * 
	 * @return the ultimate cause in the causal chain.
	 */
	AnimationEvent getUltimateCause();
	
	/**
	 * @return The type of event this instance represents
	 */
	Type getType();
	
	/**
	 * @return The source of <em>this</em> event. May be <code>null</code>.
	 */
	Object getSource();
	
	/**
	 * The valid types of events
	 */
	public static enum Type
	{
		ADD, REMOVE, CHANGE, OTHER;
	}
}
