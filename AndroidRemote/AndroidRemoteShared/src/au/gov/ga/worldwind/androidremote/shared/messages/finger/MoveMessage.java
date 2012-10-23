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
package au.gov.ga.worldwind.androidremote.shared.messages.finger;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Concrete {@link FingerMessage} representing a finger move.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MoveMessage extends FingerMessage<MoveMessage>
{
	@SuppressWarnings("unused")
	private MoveMessage()
	{
		this(new Finger[0]);
	}

	public MoveMessage(Finger[] fingers)
	{
		super(fingers);
	}

	@Override
	public MessageId getId()
	{
		return MessageId.MOVE;
	}

	@Override
	public MoveMessage fromBytes(ByteBuffer buffer)
	{
		return new MoveMessage(fingersFromBytes(buffer));
	}
}
