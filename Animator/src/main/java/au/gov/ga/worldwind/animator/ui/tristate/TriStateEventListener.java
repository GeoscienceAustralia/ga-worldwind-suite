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
 * A listener that detects events occurring on a {@link DefaultTriStateCheckBoxModel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface TriStateEventListener
{

	/**
	 * Triggered when the state of a {@link DefaultTriStateCheckBoxModel} changes
	 */
	void stateChanged(DefaultTriStateCheckBoxModel source, State oldState, State newState);
	
}
