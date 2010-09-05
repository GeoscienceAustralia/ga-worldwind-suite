package au.gov.ga.worldwind.animator.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A convenience base implementation of the {@link Changeable} interface.
 * <p/>
 * Provides a default implementation of the changeable methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class ChangeableBase implements Changeable
{
	/**
	 * The list of registered change listeners
	 */
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	@Override
	public void addChangeListener(ChangeListener changeListener)
	{
		if (changeListener == null)
		{
			return;
		}
		this.changeListeners.add(changeListener);
	}
	
	@Override
	public void removeChangeListener(ChangeListener changeListener)
	{
		if (changeListener == null)
		{
			return;
		}
		this.changeListeners.remove(changeListener);
	}
	
	/**
	 * @return The (ordered) list of registered change listeners
	 */
	public List<ChangeListener> getChangeListeners()
	{
		return changeListeners;
	}
	
	@Override
	public void notifyChange()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			changeListeners.get(i).stateChanged(event);
		}
	}

}
