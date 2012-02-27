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
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link TriStateCheckBoxModel} backed by an {@link Armable}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ArmableTriStateModel extends TriStateCheckBoxModelBase implements TriStateCheckBoxModel
{

	private Armable value;
	
	public ArmableTriStateModel(Armable value)
	{
		Validate.notNull(value, "An Armable value is required");
		this.value = value;
	}

	@Override
	public boolean isChecked()
	{
		return value.isArmed() && value.isAllChildrenArmed();
	}

	@Override
	public boolean isUnchecked()
	{
		return !value.isArmed() && !value.hasArmedChildren();
	}

	@Override
	public boolean isPartiallyChecked()
	{
		return (value.isArmed() || value.hasArmedChildren()) && !value.isAllChildrenArmed();
	}

	@Override
	public void setCurrentState(State state)
	{
		switch (state)
		{
			case CHECKED: value.setArmed(true); break;
			case UNCHECKED: value.setArmed(false); break;
		}
	}

}
