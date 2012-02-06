package au.gov.ga.worldwind.animator.ui.tristate;

import au.gov.ga.worldwind.animator.ui.tristate.TriStateCheckBox.State;

/**
 * A base class for implementations of the {@link TriStateCheckBoxModel}.
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class TriStateCheckBoxModelBase implements TriStateCheckBoxModel
{

	@Override
	public State getCurrentState()
	{
		if (isChecked())
		{
			return State.CHECKED;
		}
		if (isUnchecked())
		{
			return State.UNCHECKED;
		}
		return State.PARTIAL;
	}

	@Override
	public void iterateState()
	{
		setCurrentState(getCurrentState().nextState());
	}

}
