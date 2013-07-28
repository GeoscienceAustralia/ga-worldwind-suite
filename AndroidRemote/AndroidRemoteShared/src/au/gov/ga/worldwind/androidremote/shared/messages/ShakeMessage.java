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
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Message sent to the server when the client device is shaken.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShakeMessage implements Message<ShakeMessage>
{
	@Override
	public MessageId getId()
	{
		return MessageId.SHAKE;
	}

	@Override
	public int getLength()
	{
		return 0;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
	}

	@Override
	public ShakeMessage fromBytes(ByteBuffer buffer)
	{
		return this;
	}
}
