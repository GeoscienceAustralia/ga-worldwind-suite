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
package au.gov.ga.worldwind.androidremote.server;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.ve.VerticalExaggerationMessage;
import au.gov.ga.worldwind.viewer.settings.Settings;

/**
 * Helper class that synchronizes the vertical exaggeration between the server
 * and the Android remote.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class VerticalExaggerationSender implements PropertyChangeListener, CommunicatorListener
{
	private final WorldWindow wwd;
	private final Communicator communicator;
	private boolean settingExaggeration = false;

	public VerticalExaggerationSender(WorldWindow wwd, Communicator communicator)
	{
		this.wwd = wwd;
		this.communicator = communicator;
		wwd.getSceneController().addPropertyChangeListener(AVKey.VERTICAL_EXAGGERATION, this);
		communicator.addListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(AVKey.VERTICAL_EXAGGERATION) && !settingExaggeration)
		{
			double exaggeration = (Double) evt.getNewValue();
			communicator.sendMessage(new VerticalExaggerationMessage((float) exaggeration));
		}
	}

	@Override
	public void stateChanged(State newState)
	{
		if (newState == State.CONNECTED)
		{
			communicator.sendMessage(new VerticalExaggerationMessage((float) wwd.getSceneController()
					.getVerticalExaggeration()));
		}
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof VerticalExaggerationMessage)
		{
			float exaggeration = ((VerticalExaggerationMessage) message).exaggeration;
			settingExaggeration = true;
			Settings.get().setVerticalExaggeration(exaggeration);
			wwd.redraw();
			settingExaggeration = false;
		}
	}
}
