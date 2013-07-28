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
package au.gov.ga.worldwind.androidremote.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link Communicator} interface. Stores a list
 * of {@link CommunicatorListener}s, and the current state of the communicator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractCommunicator implements Communicator
{
	protected final List<CommunicatorListener> listeners = new ArrayList<CommunicatorListener>();
	protected State state = State.NONE;

	@Override
	public void addListener(CommunicatorListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(CommunicatorListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public State getState()
	{
		return state;
	}

	/**
	 * Set this communicator's state. Notifies listeners of the state change.
	 * 
	 * @param state
	 *            State to set to.
	 */
	protected void setState(State state)
	{
		this.state = state;
		fireStateChanged(state);
	}

	/**
	 * Notify listeners of a state change.
	 * 
	 * @param state
	 *            State changed to.
	 */
	protected void fireStateChanged(State state)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).stateChanged(state);
		}
	}

	/**
	 * Notify listeners of a received message.
	 * 
	 * @param message
	 */
	protected void fireReceivedMessage(Message<?> message)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).receivedMessage(message);
		}
	}
}
