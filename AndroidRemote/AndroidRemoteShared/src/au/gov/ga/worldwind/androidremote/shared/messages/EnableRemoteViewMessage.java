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
package au.gov.ga.worldwind.androidremote.shared.messages;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Message sent by the client to the server to enable remote view messages.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EnableRemoteViewMessage implements Message<EnableRemoteViewMessage>
{
	/**
	 * Should remote view be enabled?
	 */
	public final boolean enabled;
	/**
	 * Width of the client's screen.
	 */
	public final int width;
	/**
	 * Height of the client's screen.
	 */
	public final int height;

	@SuppressWarnings("unused")
	private EnableRemoteViewMessage()
	{
		this(false, -1, -1);
	}

	public EnableRemoteViewMessage(boolean enabled, int width, int height)
	{
		this.enabled = enabled;
		this.width = width;
		this.height = height;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.ENABLE_REMOTE_VIEW;
	}

	@Override
	public int getLength()
	{
		return 1 + 4 + 4;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		buffer.put((byte) (enabled ? 1 : 0));
		MessageIO.intToBytes(width, buffer);
		MessageIO.intToBytes(height, buffer);
	}

	@Override
	public EnableRemoteViewMessage fromBytes(ByteBuffer buffer)
	{
		boolean enabled = buffer.get() != 0;
		int width = MessageIO.bytesToInt(buffer);
		int height = MessageIO.bytesToInt(buffer);
		return new EnableRemoteViewMessage(enabled, width, height);
	}

}
