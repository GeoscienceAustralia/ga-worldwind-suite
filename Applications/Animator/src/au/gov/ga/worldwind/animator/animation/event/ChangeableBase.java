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
	
	@Override
	public void copyChangeListenersTo(Changeable changeable)
	{
		for(AnimationEventListener listener : this.changeListeners)
		{
			changeable.addChangeListener(listener);
		}
	}
	
	@Override
	public void clearChangeListeners()
	{
		this.changeListeners.clear();
	}
	
	/**
	 * @return The (ordered) list of registered change listeners
	 */
	public List<AnimationEventListener> getChangeListeners()
	{
		return changeListeners;
	}
	
	@Override
	public void fireAddEvent(Object value)
	{
		fireEvent(Type.ADD, value);
	}
	
	@Override
	public void fireRemoveEvent(Object value)
	{
		fireEvent(Type.REMOVE, value);
	}
	
	@Override
	public void fireChangeEvent(Object value)
	{
		fireEvent(Type.CHANGE, value);
	}

	@Override
	public void fireEvent(Type type, Object value)
	{
		AnimationEvent event = createEvent(type, null, value);
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			changeListeners.get(i).receiveAnimationEvent(event);
		}
		
	}

	/**
	 * Create an {@link AnimationEvent} for this instance of the given type.
	 * <p/>
	 * Subclasses should override this method and implement it to return richer subclasses 
	 * of the {@link AnimationEvent} interface specific to their animation object class.
	 */
	protected AnimationEvent createEvent(Type type, AnimationEvent cause, Object value)
	{
		return new AnimationEventImpl(this, type, cause, value);
	}
}
