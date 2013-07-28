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

/**
 * Represents an object that listens to a {@link Communicator} for state changes
 * and received messages.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CommunicatorListener
{
	/**
	 * {@link Communicator}'s state has changed.
	 * 
	 * @param newState
	 *            New communicator state.
	 */
	void stateChanged(Communicator.State newState);

	/**
	 * {@link Communicator} received a message.
	 * 
	 * @param message
	 *            Message received.
	 */
	void receivedMessage(Message<?> message);
}
