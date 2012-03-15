/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.ui.tristate;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;

/**
 * The model interface for the {@link TriStateCheckBox} component. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
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
