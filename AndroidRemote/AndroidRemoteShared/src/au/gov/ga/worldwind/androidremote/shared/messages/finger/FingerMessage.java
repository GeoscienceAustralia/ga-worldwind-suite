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
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.androidremote.shared.Message;

/**
 * Abstract message containing 1 or more {@link Finger} object data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            This Message type
 */
public abstract class FingerMessage<E extends FingerMessage<E>> implements Message<E>
{
	/**
	 * Message's fingers.
	 */
	public final Finger[] fingers;

	public FingerMessage(Finger[] fingers)
	{
		this.fingers = fingers;
	}

	@Override
	public int getLength()
	{
		return fingers.length * Finger.requiredMessageLength();
	}

	@Override
	public void toBytes(ByteBuffer buffer)
	{
		for (Finger finger : fingers)
		{
			finger.toBytes(buffer);
		}
	}

	protected Finger[] fingersFromBytes(ByteBuffer buffer)
	{
		List<Finger> fingers = new ArrayList<Finger>();
		while (buffer.hasRemaining())
		{
			Finger finger = Finger.fromBytes(buffer);
			if (finger == null)
			{
				return null;
			}
			fingers.add(finger);
		}
		return fingers.toArray(new Finger[fingers.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" (");
		for (int i = 0; i < fingers.length; i++)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			sb.append(fingers[i].toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
