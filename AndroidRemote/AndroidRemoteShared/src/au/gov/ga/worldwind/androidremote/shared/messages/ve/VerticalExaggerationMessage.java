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
package au.gov.ga.worldwind.androidremote.shared.messages.ve;

import java.nio.ByteBuffer;

import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.MessageIO;
import au.gov.ga.worldwind.androidremote.shared.MessageId;

/**
 * Message used to synchronize the vertical exaggeration between the devices.
 * Used both to change the vertical exaggeration, and to notify when the
 * vertical exaggeration changes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class VerticalExaggerationMessage implements Message<VerticalExaggerationMessage>
{
	/**
	 * Vertical exaggeration value.
	 */
	public final float exaggeration;

	@SuppressWarnings("unused")
	private VerticalExaggerationMessage()
	{
		this(1f);
	}

	public VerticalExaggerationMessage(float exaggeration)
	{
		this.exaggeration = exaggeration;
	}

	@Override
	public MessageId getId()
	{
		return MessageId.VERTICAL_EXAGGERATION;
	}

	@Override
	public int getLength()
	{
		return 4;
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		MessageIO.floatToBytes(exaggeration, buffer);
	}

	@Override
	public VerticalExaggerationMessage fromBytes(ByteBuffer buffer)
	{
		float exaggeration = MessageIO.bytesToFloat(buffer);
		return new VerticalExaggerationMessage(exaggeration);
	}
}
