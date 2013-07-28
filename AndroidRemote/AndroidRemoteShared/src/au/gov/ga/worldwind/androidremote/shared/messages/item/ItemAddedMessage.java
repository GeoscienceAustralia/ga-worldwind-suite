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
 * {@link ItemMessage} implementation that is sent when an {@link Item} is
 * added.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemAddedMessage implements ItemMessage<ItemAddedMessage>
{
	/**
	 * @see #getModelId()
	 */
	public final int modelId;
	/**
	 * Added item.
	 */
	public final Item item;
	/**
	 * Index path in the tree where this item has been added.
	 */
	public final int[] treeIndexPath;

	@SuppressWarnings("unused")
	private ItemAddedMessage()
	{
		this(-1, null, null);
	}

	public ItemAddedMessage(int modelId, Item item, int[] treeIndexPath)
	{
		this.modelId = modelId;
		this.item = item;
		this.treeIndexPath = treeIndexPath;
	}

	@Override
	public int getLength()
	{
		return 4 + item.getLength() + 4 + 4 * treeIndexPath.length;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.intToBytes(modelId, buffer);
		item.toBytes(buffer);
		MessageIO.intToBytes(treeIndexPath.length, buffer);
		for (int t : treeIndexPath)
		{
			MessageIO.intToBytes(t, buffer);
		}
	}

	@Override
	public ItemAddedMessage fromBytes(ByteBuffer buffer)
	{
		int modelId = MessageIO.bytesToInt(buffer);
		Item item = Item.fromBytes(buffer);
		int treeIndexPathLength = MessageIO.bytesToInt(buffer);
		int[] treeIndexPath = new int[treeIndexPathLength];
		for (int i = 0; i < treeIndexPathLength; i++)
		{
			treeIndexPath[i] = MessageIO.bytesToInt(buffer);
		}
		return new ItemAddedMessage(modelId, item, treeIndexPath);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ITEM_ADDED;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}
}
