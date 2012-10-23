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
 * {@link ItemMessage} implemetation that is sent when an {@link Item} is
 * selected.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemSelectedMessage implements ItemMessage<ItemSelectedMessage>
{
	/**
	 * @see #getModelId()
	 */
	public final int modelId;
	/**
	 * Index path in the tree for the selected item.
	 */
	public final int[] treeIndexPath;

	@SuppressWarnings("unused")
	private ItemSelectedMessage()
	{
		this(-1, null);
	}

	public ItemSelectedMessage(int modelId, int[] treeIndexPath)
	{
		this.modelId = modelId;
		this.treeIndexPath = treeIndexPath;
	}

	@Override
	public int getLength()
	{
		return 4 + 4 + 4 * treeIndexPath.length;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.intToBytes(modelId, buffer);
		MessageIO.intToBytes(treeIndexPath.length, buffer);
		for (int t : treeIndexPath)
		{
			MessageIO.intToBytes(t, buffer);
		}
	}

	@Override
	public ItemSelectedMessage fromBytes(ByteBuffer buffer)
	{
		int modelId = MessageIO.bytesToInt(buffer);
		int treeIndexPathLength = MessageIO.bytesToInt(buffer);
		int[] treeIndexPath = new int[treeIndexPathLength];
		for (int i = 0; i < treeIndexPathLength; i++)
		{
			treeIndexPath[i] = MessageIO.bytesToInt(buffer);
		}
		return new ItemSelectedMessage(modelId, treeIndexPath);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ITEM_SELECTED;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}
}
