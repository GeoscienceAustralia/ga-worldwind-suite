package au.gov.ga.worldwind.animator.animation.event;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * An interface for objects that can change. Allows change listeners to be attatched
 * and notified of changes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 *
 */
public interface Changeable
{

	/**
	 * Add the provided listener to this class's list of change listeners.
	 * 
	 * @param changeListener The listener to add
	 */
	void addChangeListener(AnimationEventListener changeListener);
	
	/**
	 * Remove the provided listener to this class's list of change listeners.
	 * 
	 * @param changeListener The listener to remove
	 */
	void removeChangeListener(AnimationEventListener changeListener);
	
	/**
	 * Copy all change listeners from this to another Changeable.
	 * 
	 * @param changeable Changeable to copy change listeners to
	 */
	void copyChangeListenersTo(Changeable changeable);
	
	/**
	 * Remove all change listeners.
	 */
	void clearChangeListeners();
	
	/**
	 * Fire a event of type {@link AnimationEvent.Type#ADD}
	 * <p/>
	 * Should be invoked when this object is being added to some parent container (e.g. a {@link KeyFrame} is being added to an {@link Animation})
	 */
	void fireAddEvent(Object value);
	
	/**
	 * Fire a event of type {@link AnimationEvent.Type#REMOVE}
	 * <p/>
	 * Should be invoked when this object is being removed from some parent container (e.g. a {@link KeyFrame} is being removed from an {@link Animation})
	 */
	void fireRemoveEvent(Object value);
	
	/**
	 * Fire a event of type {@link AnimationEvent.Type#CHANGE}
	 * <p/>
	 * Should be invoked when some property of this object is being changed (e.g. the value of a {@link ParameterValue} is changed)
	 */
	void fireChangeEvent(Object value);
	
	/**
	 * Fire a event of the provided type
	 */
	void fireEvent(AnimationEvent.Type type, Object value);
}
