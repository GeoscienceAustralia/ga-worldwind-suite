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
package au.gov.ga.worldwind.androidremote.server.listeners.list;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.viewer.panels.places.Place;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPanel;

/**
 * Listener class that listens for changes on the Places list model, and
 * converts them to {@link ItemModel} events.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PlaceListListener implements ListDataListener
{
	private final ItemModel model;
	private final ListModel listModel;

	public PlaceListListener(ItemModel model, ListModel listModel)
	{
		this.model = model;
		this.listModel = listModel;
	}

	public void clear()
	{
		model.setRoot(null);
	}

	public void sendRefreshedMessage()
	{
		Item root = new Item();
		for (int i = 0; i < listModel.getSize(); i++)
		{
			Object o = listModel.getElementAt(i);
			Item child = objectToItem(o);
			root.addChild(child);
			child.setParent(root);
		}
		model.setRoot(root);
	}

	protected Item objectToItem(Object o)
	{
		Item item = new Item();
		PlacesPanel.ListItem listItem = (PlacesPanel.ListItem) o;
		Place place = listItem.place;
		item.setName(place.getLabel());
		item.setEnabled(place.isVisible());
		item.setLeaf(true);
		return item;
	}

	@Override
	public void intervalAdded(ListDataEvent e)
	{
		for (int i = e.getIndex0(); i <= e.getIndex1(); i++)
		{
			Object o = listModel.getElementAt(i);
			model.addItem(objectToItem(o), model.getRoot(), i);
		}
	}

	@Override
	public void intervalRemoved(ListDataEvent e)
	{
		for (int i = e.getIndex0(); i <= e.getIndex1(); i++)
		{
			model.removeItem(model.getRoot().getChild(e.getIndex0()));
		}
	}

	@Override
	public void contentsChanged(ListDataEvent e)
	{
		sendRefreshedMessage();
	}
}
