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
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link TriStateCheckBoxModel} backed by an {@link Enableable} object
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EnableableTriStateModel extends TriStateCheckBoxModelBase implements TriStateCheckBoxModel
{

	private Enableable value; 
	
	public EnableableTriStateModel(Enableable value)
	{
		Validate.notNull(value, "An Enableable value is required");
		this.value = value;
	}

	@Override
	public boolean isChecked()
	{
		return value.isEnabled() && value.isAllChildrenEnabled();
	}

	@Override
	public boolean isUnchecked()
	{
		return !value.isEnabled() && !value.hasEnabledChildren();
	}

	@Override
	public boolean isPartiallyChecked()
	{
		return (value.isEnabled() || value.hasEnabledChildren()) && !value.isAllChildrenEnabled();
	}

	@Override
	public void setCurrentState(State state)
	{
		switch (state)
		{
			case CHECKED: value.setEnabled(true); break;
			case UNCHECKED: value.setEnabled(false); break;
		}
	}
	
}
