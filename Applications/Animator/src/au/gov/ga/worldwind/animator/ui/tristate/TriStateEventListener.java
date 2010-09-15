package au.gov.ga.worldwind.animator.ui.tristate;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;

/**
 * A listener that detects events occurring on a {@link TriStateCheckBoxModel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface TriStateEventListener
{

	/**
	 * Triggered when the state of a {@link TriStateCheckBoxModel} changes
	 */
	void stateChanged(TriStateCheckBoxModel source, State oldState, State newState);
	
}
