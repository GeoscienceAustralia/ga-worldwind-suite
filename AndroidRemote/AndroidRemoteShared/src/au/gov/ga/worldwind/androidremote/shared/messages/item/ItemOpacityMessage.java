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
 * {@link ItemMessage} implementation that is sent when an Item's opacity value
 * changes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ItemOpacityMessage implements ItemMessage<ItemOpacityMessage>
{
	/**
	 * @see #getModelId()
	 */
	public final int modelId;
	/**
	 * Index path in the tree for the item whose opacity has changed.
	 */
	public final int[] treeIndexPath;
	/**
	 * Item's new opacity value.
	 */
	public final float opacity;

	@SuppressWarnings("unused")
	private ItemOpacityMessage()
	{
		this(-1, null, -1);
	}

	public ItemOpacityMessage(int modelId, int[] treeIndexPath, float opacity)
	{
		this.modelId = modelId;
		this.treeIndexPath = treeIndexPath;
		this.opacity = opacity;
	}

	@Override
	public int getLength()
	{
		return 4 + 4 + 4 * treeIndexPath.length + 4;
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
		MessageIO.floatToBytes(opacity, buffer);
	}

	@Override
	public ItemOpacityMessage fromBytes(ByteBuffer buffer)
	{
		int modelId = MessageIO.bytesToInt(buffer);
		int treeIndexPathLength = MessageIO.bytesToInt(buffer);
		int[] treeIndexPath = new int[treeIndexPathLength];
		for (int i = 0; i < treeIndexPathLength; i++)
		{
			treeIndexPath[i] = MessageIO.bytesToInt(buffer);
		}
		float opacity = MessageIO.bytesToFloat(buffer);
		return new ItemOpacityMessage(modelId, treeIndexPath, opacity);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ITEM_OPACITY;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}
}
