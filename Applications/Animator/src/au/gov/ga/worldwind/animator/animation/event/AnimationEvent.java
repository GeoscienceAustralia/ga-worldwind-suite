package au.gov.ga.worldwind.animator.animation.event;


/**
 * A super-interface for events that can occur within an animation.
 * <p/>
 * An event has:
 * <ol>
 *  <li>an owner (the object that signalled the event);
 *  <li>a type (that signals the type of action that occurred to trigger the event);
 *  <li>a value (the value that was acted on in the action that triggered the event); and
 *  <li>a cause (the event that caused this event to be triggered)
 * </ol>
 * <p/>
 * Animation events support a causal chain that can be queried by listeners interested in particular event types.
 * For example, a key frame display might be interested in any event that involves key frames.
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
	AnimationEvent getRootCause();
	
	/**
	 * @return The type of event this instance represents
	 */
	Type getType();
	
	/**
	 * @return The owner of <em>this</em> event. This is the object that signalled the event.
	 */
	Object getOwner();
	
	/**
	 * @return The value associated with this event. This is the object that was added/changed/removed to trigger the event.
	 * Will usually be <code>null</code> in all but the ultimate cause in the causal chain.
	 */
	Object getValue();
	
	/**
	 * @return Whether this event is of the provided type
	 */
	boolean isOfType(Type type);
	
	/**
	 * The valid types of events
	 */
	public static enum Type
	{
		ADD, REMOVE, CHANGE, OTHER;
	}

	
}
