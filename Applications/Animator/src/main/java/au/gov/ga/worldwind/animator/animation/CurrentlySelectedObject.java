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
package au.gov.ga.worldwind.animator.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * A static context class that holds a reference to the currently selected 
 * animation object.
 * <p/>
 * Listeners can be attached to listen for changes in the currently selected object.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CurrentlySelectedObject
{

	private static AnimationObject currentObject;
	
	private static Object objectLock = new Object();
	
	private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	/**
	 * @return The currently selected animation object, or <code>null</code> if no object is currently selected
	 */
	public static AnimationObject get()
	{
		synchronized (objectLock)
		{
			return currentObject;
		}
	}
	
	/**
	 * Set the currently selected object and notify listeners of the change
	 */
	public static void set(AnimationObject o)
	{
		synchronized (objectLock)
		{
			AnimationObject previousObject = currentObject;
			boolean changed = o != currentObject;
			
			currentObject = o;
			
			if (changed)
			{
				fireChangeEvent(previousObject);
			}
		}
	}
	
	/**
	 * Register a listener to be notified of changes to the currently selected object
	 */
	public static void addChangeListener(ChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}
		listeners.add(listener);
	}

	/**
	 * Removed the provided change listener from the list of registered listeners
	 */
	public static void removeChangeListener(ChangeListener listener)
	{
		listeners.remove(listener);
	}
	
	private static void fireChangeEvent(AnimationObject previouslySelectedObject)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).selectedObjectChanged(currentObject, previouslySelectedObject);
		}
	}
	
	/**
	 * An interface for listeners who want to be notified of a change
	 * to the currently selected animation object. 
	 */
	public interface ChangeListener
	{
		void selectedObjectChanged(AnimationObject currentlySelectedObject, AnimationObject previouslySelectedObject);
	}
	
}
