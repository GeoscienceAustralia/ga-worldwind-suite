package au.gov.ga.worldwind.animator.animation.event;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.event.AnimationEvent.Type;


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
	private List<AnimationEventListener> changeListeners = new ArrayList<AnimationEventListener>();

	@Override
	public void addChangeListener(AnimationEventListener changeListener)
	{
		if (changeListener == null)
		{
			return;
		}
		this.changeListeners.add(changeListener);
	}
	
	@Override
	public void removeChangeListener(AnimationEventListener changeListener)
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
	public List<AnimationEventListener> getChangeListeners()
	{
		return changeListeners;
	}
	
	@Override
	public void fireAddEvent()
	{
		fireEvent(Type.ADD);
	}
	
	@Override
	public void fireRemoveEvent()
	{
		fireEvent(Type.REMOVE);
	}
	
	@Override
	public void fireChangeEvent()
	{
		fireEvent(Type.CHANGE);
	}

	@Override
	public void fireEvent(Type type)
	{
		AnimationEvent event = createEvent(type, null);
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			changeListeners.get(i).signalEvent(event);
		}
		
	}

	/**
	 * Create an {@link AnimationEvent} for this instance of the given type.
	 * <p/>
	 * Subclasses should override this method and implement it to return richer subclasses 
	 * of the {@link AnimationEvent} interface specific to their animation object class.
	 */
	protected AnimationEvent createEvent(Type type, AnimationEvent cause)
	{
		return new AnimationEventImpl(type, this, cause);
	}
}
