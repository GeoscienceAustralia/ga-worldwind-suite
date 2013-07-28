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
package au.gov.ga.worldwind.androidremote.shared.messages.item;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.MessageId;
import au.gov.ga.worldwind.androidremote.shared.model.Item;

/**
 * {@link ItemMessage} implementation sent when the root Item changes, or when
 * the devices connect for the first time. Contains the the full tree hierarchy.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemsRefreshedMessage implements ItemMessage<ItemsRefreshedMessage>
{
	/**
	 * @see #getModelId()
	 */
	public final int modelId;
	/**
	 * Root item.
	 */
	public final Item root;

	@SuppressWarnings("unused")
	private ItemsRefreshedMessage()
	{
		this(-1, null);
	}

	public ItemsRefreshedMessage(int modelId, Item root)
	{
		this.modelId = modelId;
		this.root = root;
	}

	@Override
	public int getLength()
	{
		return 4 + 1 + (root == null ? 0 : root.getLength());
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.intToBytes(modelId, buffer);
		buffer.put((byte) (root == null ? 0 : 1));
		if (root != null)
		{
			root.toBytes(buffer);
		}
	}

	@Override
	public ItemsRefreshedMessage fromBytes(ByteBuffer buffer)
	{
		int modelId = MessageIO.bytesToInt(buffer);
		boolean rootExists = buffer.get() != 0;
		Item root = rootExists ? Item.fromBytes(buffer) : null;
		return new ItemsRefreshedMessage(modelId, root);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ITEMS_REFRESHED;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}
}
