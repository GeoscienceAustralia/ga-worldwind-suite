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
 * Message sent from the server to the client containing a single image frame
 * for the remote view.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RemoteViewMessage implements Message<RemoteViewMessage>
{
	/**
	 * Image byte buffer.
	 */
	public final ByteBuffer buffer;

	@SuppressWarnings("unused")
	private RemoteViewMessage()
	{
		this(null);
	}

	public RemoteViewMessage(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.REMOTE_VIEW;
	}

	@Override
	public int getLength()
	{
		return 4 + 4 + 4 + 4 + buffer.limit();
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.intToBytes(this.buffer.limit(), buffer);
		this.buffer.rewind();
		buffer.put(this.buffer);
	}

	@Override
	public RemoteViewMessage fromBytes(ByteBuffer buffer)
	{
		int limit = MessageIO.bytesToInt(buffer);
		ByteBuffer b = ByteBuffer.allocate(limit);
		buffer.get(b.array());
		return new RemoteViewMessage(b);
	}
}
