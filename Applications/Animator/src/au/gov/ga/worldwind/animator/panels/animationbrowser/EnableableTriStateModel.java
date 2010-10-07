package au.gov.ga.worldwind.animator.panels.animationbrowser;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBoxModel;
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;
import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBoxModelBase;
import au.gov.ga.worldwind.animator.util.Enableable;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A {@link TriStateCheckBoxModel} backed by an {@link Enableable} object
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
final class EnableableTriStateModel extends TriStateCheckBoxModelBase implements TriStateCheckBoxModel
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