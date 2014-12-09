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

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.messages.EnableRemoteViewMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.ExitMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.FlyHomeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.IpAddressesMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.LocationMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.RemoteViewMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.ShakeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.DownMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.MoveMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.UpMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemAddedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemEnabledMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemOpacityMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemRemovedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemSelectedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemsRefreshedMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlacesPlayingMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.places.PlayPlacesMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.ve.VerticalExaggerationMessage;

/**
 * An enum containing all message implementations that can be sent. Used to
 * construct new messages from a byte buffer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum MessageId
{
	DOWN(DownMessage.class),
	MOVE(MoveMessage.class),
	UP(UpMessage.class),
	SHAKE(ShakeMessage.class),
	ENABLE_REMOTE_VIEW(EnableRemoteViewMessage.class),
	REMOTE_VIEW(RemoteViewMessage.class),
	IP_ADDRESSES(IpAddressesMessage.class),
	VERTICAL_EXAGGERATION(VerticalExaggerationMessage.class),

	ITEMS_REFRESHED(ItemsRefreshedMessage.class),
	ITEM_ADDED(ItemAddedMessage.class),
	ITEM_REMOVED(ItemRemovedMessage.class),
	ITEM_ENABLED(ItemEnabledMessage.class),
	ITEM_OPACITY(ItemOpacityMessage.class),
	ITEM_SELECTED(ItemSelectedMessage.class),

	PLAY_PLACES(PlayPlacesMessage.class),
	PLACES_PLAYING(PlacesPlayingMessage.class),
	FLY_HOME(FlyHomeMessage.class),
	
	LOCATION(LocationMessage.class),

	EXIT(ExitMessage.class);

	/**
	 * Class that implements this message.
	 */
	public final Class<? extends Message<?>> messageClass;
	private final Constructor<? extends Message<?>> classConstructor;

	private MessageId(Class<? extends Message<?>> messageClass)
	{
		this.messageClass = messageClass;
		try
		{
			classConstructor = messageClass.getDeclaredConstructor();
			classConstructor.setAccessible(true);
		}
		catch (Exception e)
		{
			throw new RuntimeException(messageClass.getSimpleName() + " must have an accessible default contructor");
		}
	}

	public int getId()
	{
		return ordinal();
	}

	/**
	 * Create a new instance of this message's class from the given byte buffer.
	 * 
	 * @param buffer
	 * @return New instance of this message's class created with data from the
	 *         byte buffer.
	 */
	public Message<?> fromBytes(ByteBuffer buffer)
	{
		try
		{
			Message<?> message = classConstructor.newInstance();
			return message.fromBytes(buffer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
