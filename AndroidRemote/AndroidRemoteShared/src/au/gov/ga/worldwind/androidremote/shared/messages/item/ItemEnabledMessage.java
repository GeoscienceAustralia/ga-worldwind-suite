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

/**
 * {@link ItemMessage} implementation that is sent when an item is
 * enabled/disabled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemEnabledMessage implements ItemMessage<ItemEnabledMessage>
{
	/**
	 * @see #getModelId()
	 */
	public final int modelId;
	/**
	 * Index path in the tree of the item that has been enabled/disabled.
	 */
	public final int[] treeIndexPath;
	/**
	 * Has this item been enabled or disabled?
	 */
	public final boolean enabled;

	@SuppressWarnings("unused")
	private ItemEnabledMessage()
	{
		this(-1, null, false);
	}

	public ItemEnabledMessage(int modelId, int[] treeIndexPath, boolean enabled)
	{
		this.modelId = modelId;
		this.treeIndexPath = treeIndexPath;
		this.enabled = enabled;
	}

	@Override
	public int getLength()
	{
		return 4 + 4 + 4 * treeIndexPath.length + 1;
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
		buffer.put((byte) (enabled ? 1 : 0));
	}

	@Override
	public ItemEnabledMessage fromBytes(ByteBuffer buffer)
	{
		int modelId = MessageIO.bytesToInt(buffer);
		int treeIndexPathLength = MessageIO.bytesToInt(buffer);
		int[] treeIndexPath = new int[treeIndexPathLength];
		for (int i = 0; i < treeIndexPathLength; i++)
		{
			treeIndexPath[i] = MessageIO.bytesToInt(buffer);
		}
		boolean enabled = buffer.get() != 0;
		return new ItemEnabledMessage(modelId, treeIndexPath, enabled);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ITEM_ENABLED;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}
}
