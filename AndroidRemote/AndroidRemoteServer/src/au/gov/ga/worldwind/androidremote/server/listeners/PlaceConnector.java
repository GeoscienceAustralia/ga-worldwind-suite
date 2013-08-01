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
package au.gov.ga.worldwind.androidremote.server.listeners;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import au.gov.ga.worldwind.androidremote.server.listeners.list.PlaceListListener;
import au.gov.ga.worldwind.androidremote.server.view.orbit.AndroidInputProvider;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.FlyHomeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.FingerMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlacesPlayingMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlayPlacesMessage;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModelListener;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPanel;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPlayingListener;

/**
 * Helper class that connects the {@link PlacesPanel} with the places
 * {@link ItemModel}. Listens for changes on both to keep things in sync.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlaceConnector implements CommunicatorListener, PlacesPlayingListener
{
	private final Communicator communicator;
	private final PlacesPanel placesPanel;
	private final AndroidInputProvider inputProvider;
	private final ItemModel placesModel;
	private final PlaceListListener placeListListener;
	private boolean listening = false;
	private final ListSelectionListener placeListSelectionListener;

	public PlaceConnector(Communicator communicator, final PlacesPanel placesPanel, AndroidInputProvider inputProvider)
	{
		this.communicator = communicator;
		this.placesPanel = placesPanel;
		this.inputProvider = inputProvider;

		placesModel = new ItemModel(2, communicator);
		placesModel.addListener(placeItemModelListener);
		placeListListener = new PlaceListListener(placesModel, placesPanel.getModel());
		placesPanel.addPlacesPlayingListener(this);

		placeListSelectionListener = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				int index = placesPanel.getList().getSelectedIndex();
				if (index >= 0)
				{
					Item item = placesModel.getRoot().getChild(index);
					if (item != null)
					{
						placesModel.selectItem(item);
					}
				}
			}
		};
	}

	@Override
	public void stateChanged(State newState)
	{
		boolean connected = newState == State.CONNECTED;
		placeListListener.clear();
		if (connected)
		{
			placeListListener.sendRefreshedMessage();
		}
		listenToListModel(connected);
	}

	protected void listenToListModel(boolean listen)
	{
		if (listening != listen)
		{
			if (listen)
			{
				placesPanel.getModel().addListDataListener(placeListListener);
				placesPanel.getList().getSelectionModel().addListSelectionListener(placeListSelectionListener);
			}
			else
			{
				placesPanel.getModel().removeListDataListener(placeListListener);
				placesPanel.getList().getSelectionModel().removeListSelectionListener(placeListSelectionListener);
			}
			listening = listen;
		}
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (placesPanel.isPlaying())
		{
			if (message instanceof FingerMessage<?> || message instanceof FlyHomeMessage)
			{
				placesPanel.stopPlaces();
			}
		}
		if (message instanceof ItemMessage<?>)
		{
			ItemMessage<?> itemMessage = (ItemMessage<?>) message;
			placesModel.handleMessage(itemMessage);
		}
		else if (message instanceof PlayPlacesMessage)
		{
			if (((PlayPlacesMessage) message).play)
			{
				inputProvider.stopGesture();
				placesPanel.playPlaces();
			}
			else
			{
				placesPanel.stopPlaces();
			}
		}
	}

	@Override
	public void playingChanged(boolean playing)
	{
		communicator.sendMessage(new PlacesPlayingMessage(playing));
	}

	private final ItemModelListener placeItemModelListener = new ItemModelListener()
	{
		@Override
		public void itemsRefreshed(Item root, boolean remote)
		{
		}

		@Override
		public void itemsAdded(Item item, Item parent, int index, boolean remote)
		{
		}

		@Override
		public void itemSelected(Item item, boolean remote)
		{
			if (remote)
			{
				inputProvider.stopGesture();
				int index = item.indexWithinParent();
				placesPanel.stopPlaces();
				placesPanel.flyTo((PlacesPanel.ListItem) placesPanel.getModel().get(index));
				placesPanel.getList().setSelectedIndex(index);
			}
		}

		@Override
		public void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote)
		{
		}

		@Override
		public void itemOpacityChanged(Item item, float opacity, boolean remote)
		{
		}

		@Override
		public void itemEnabled(Item item, boolean enabled, boolean remote)
		{
		}
	};
}
