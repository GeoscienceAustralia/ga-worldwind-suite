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
 * Represents an object that is used to communicate {@link Message}s between
 * devices.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Communicator
{
	public enum State
	{
		NONE, //doing nothing
		LISTEN, //listening for incoming connections
		CONNECTING, //now initiating an outgoing connection
		CONNECTED //now connected to a remote device
	}

	/**
	 * @return This communicator's state.
	 */
	State getState();

	/**
	 * Send a message using this communicator's connection.
	 * 
	 * @param message
	 *            Message to send.
	 */
	void sendMessage(Message<?> message);

	/**
	 * Add a listener to this communicator.
	 * 
	 * @param listener
	 *            Listener to add.
	 */
	void addListener(CommunicatorListener listener);

	/**
	 * Remove a listener from this communicator.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	void removeListener(CommunicatorListener listener);
}
