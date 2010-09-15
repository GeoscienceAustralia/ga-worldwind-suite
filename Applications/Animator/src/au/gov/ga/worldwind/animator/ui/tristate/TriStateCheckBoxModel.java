package au.gov.ga.worldwind.animator.ui.tristate;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;

/**
 * The model interface for the {@link TriStateCheckBox} component. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface TriStateCheckBoxModel
{

	/**
	 * @return Whether this model is in the 'checked' state
	 */
	public boolean isChecked();
	
	/**
	 * @return Whether this model is in the 'unchecked' state
	 */
	public boolean isUnchecked();
	
	/**
	 * @return Whether this model is in the 'partially checked' state
	 */
	public boolean isPartiallyChecked();
	
	/**
	 * Set the current state of the model
	 */
	public void setCurrentState(State state);

	/**
	 * @return The current state of the model
	 */
	public State getCurrentState();
	
	/**
	 * Move from the current state into the next state
	 */
	public void iterateState();
	
}
