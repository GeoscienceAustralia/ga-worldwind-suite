package au.gov.ga.worldwind.animator.animation.event;

/**
 * An interface for clients that want to be notified of {@link AnimationEvent}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface AnimationEventListener
{

	/**
	 * Signal for this listener to respond to the supplied event
	 */
	void signalEvent(AnimationEvent event);
	
}
