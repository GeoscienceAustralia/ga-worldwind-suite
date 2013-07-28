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
package au.gov.ga.worldwind.androidremote.client.ui.menu;

import au.gov.ga.worldwind.androidremote.client.R;
import au.gov.ga.worldwind.androidremote.client.ui.ItemModelFragment;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlacesPlayingMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlayPlacesMessage;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * {@link ItemModelFragmentMenuProvider} implementation for the places
 * {@link ItemModelFragment}. Displays a play/stop menu option that allows
 * playback of the places list.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlacesMenuProvider implements ItemModelFragmentMenuProvider, CommunicatorListener
{
	private static final int OPTION_PLAY_STOP = 321423;

	private final Communicator communicator;
	private boolean playing = false;
	private MenuItem item;

	public PlacesMenuProvider(Communicator communicator)
	{
		this.communicator = communicator;
		communicator.addListener(this);
	}

	@Override
	public boolean hasOptionsMenu()
	{
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		item = menu.add(0, OPTION_PLAY_STOP, 0, R.string.play);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		updateMenuItem();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case OPTION_PLAY_STOP:
			this.item = item;
			communicator.sendMessage(new PlayPlacesMessage(!playing));
			return true;
		}
		return false;
	}

	private void updateMenuItem()
	{
		if (item == null)
			return;

		item.setIcon(playing ? R.drawable.media_playback_stop : R.drawable.media_playback_start);
		item.setTitle(playing ? R.string.stop : R.string.play);
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof PlacesPlayingMessage)
		{
			playing = ((PlacesPlayingMessage) message).playing;
			updateMenuItem();
		}
	}

	@Override
	public void stateChanged(State state)
	{
		playing = false;
	}
}
