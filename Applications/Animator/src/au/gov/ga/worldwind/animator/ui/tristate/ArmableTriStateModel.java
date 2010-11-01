package au.gov.ga.worldwind.animator.ui.tristate;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;
import au.gov.ga.worldwind.animator.util.Armable;
import au.gov.ga.worldwind.animator.util.Validate;

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
