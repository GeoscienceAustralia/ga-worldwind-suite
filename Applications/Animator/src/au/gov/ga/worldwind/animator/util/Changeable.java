package au.gov.ga.worldwind.animator.util;

import javax.swing.event.ChangeListener;

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
	void addChangeListener(ChangeListener changeListener);
	
	/**
	 * Remove the provided listener to this class's list of change listeners.
	 * 
	 * @param changeListener The listener to remove
	 */
	void removeChangeListener(ChangeListener changeListener);
	
	/**
	 * Notify this object's registered change listeners of a change to this object's state
	 */
	void notifyChange();
}
