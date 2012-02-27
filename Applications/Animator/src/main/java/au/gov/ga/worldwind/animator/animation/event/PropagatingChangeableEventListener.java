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
package au.gov.ga.worldwind.animator.animation.event;

import java.util.List;

/**
 * A convenience base class that implements both the {@link Changeable} and {@link AnimationEventListener}
 * interfaces.
 * <p/>
 * This base implements the {@link #receiveAnimationEvent(AnimationEvent)} method to wrap the event in one
 * specific to the implementing class, and propagate it to this classes registered listeners.
 * <p/>
 * By default, a standard {@link AnimationEvent} will be created when propagating. To change this, override the {@link #createEvent}
 * method to return a specific event instance.
 * <p/>
 * To handle 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class PropagatingChangeableEventListener extends ChangeableBase implements Changeable, AnimationEventListener
{

	@Override
	public final void receiveAnimationEvent(AnimationEvent event)
	{
		boolean propagateEvent = handleEvent(event);
		if (!propagateEvent)
		{
			return;
		}
		
		AnimationEvent newEvent = createEvent(null, event, null);
		List<AnimationEventListener> listeners = getChangeListeners();
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).receiveAnimationEvent(newEvent);
		}
	}
	
	/**
	 * Handle the provided animation event. 
	 * <p/>
	 * Provided as a hook so that subclasses can perform some processing around
	 * an event before it is propagated to this class's listeners.
	 * <p/>
	 * The return value controls whether the event is to be propagated or not. <code>true</code>
	 * indicates the event should be propagated, <code>false</code> indicates it shouldn't.
	 * 
	 * @param event The event to handle
	 * 
	 * @return <code>true</code> if the event is to be propagated to this registered listeners, <code>false</code> otherwise.
	 */
	protected boolean handleEvent(AnimationEvent event)
	{
		return true;
	}

}
