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

import au.gov.ga.worldwind.androidremote.shared.MessageIO;

/**
 * Contains position and velocity information about a single finger; used by
 * {@link FingerMessage}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Finger
{
	/**
	 * Unique finger id.
	 */
	public final int id;
	/**
	 * x-position of the finger.
	 */
	public final float x;
	/**
	 * y-position of the finger.
	 */
	public final float y;
	/**
	 * x-velocity of the finger.
	 */
	public final float xVelocity;
	/**
	 * y-velocity of the finger.
	 */
	public final float yVelocity;
	/**
	 * Is this finger down (touching the screen)?
	 */
	public final boolean down;

	public Finger(int id, float x, float y, float xVelocity, float yVelocity, boolean down)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.down = down;
	}

	/**
	 * @return Number of bytes required to store this Finger in an array/buffer.
	 */
	public static int requiredMessageLength()
	{
		return 4 * 5 + 1;
	}

	/**
	 * Create a Finger object from the given byte buffer. If buffer doesn't
	 * contain at least the required number of bytes remaining (see
	 * {@link #requiredMessageLength()}), null is returned.
	 * 
	 * @param buffer
	 *            Buffer to read finger bytes from.
	 * @return New Finger object from data in buffer.
	 */
	public static Finger fromBytes(ByteBuffer buffer)
	{
		if (buffer.remaining() < requiredMessageLength())
		{
			return null;
		}
		int id = MessageIO.bytesToInt(buffer);
		float x = MessageIO.bytesToFloat(buffer);
		float y = MessageIO.bytesToFloat(buffer);
		float xVelocity = MessageIO.bytesToFloat(buffer);
		float yVelocity = MessageIO.bytesToFloat(buffer);
		boolean down = buffer.get() != 0;
		return new Finger(id, x, y, xVelocity, yVelocity, down);
	}

	/**
	 * Save this Finger object to a ByteBuffer. Buffer must have at least the
	 * required number of bytes remaining (see {@link #requiredMessageLength()}
	 * ).
	 * 
	 * @param buffer
	 *            Buffer to save to
	 */
	public void toBytes(ByteBuffer buffer)
	{
		if (buffer.remaining() < requiredMessageLength())
		{
			throw new IllegalArgumentException("Incorrect array length");
		}
		MessageIO.intToBytes(id, buffer);
		MessageIO.floatToBytes(x, buffer);
		MessageIO.floatToBytes(y, buffer);
		MessageIO.floatToBytes(xVelocity, buffer);
		MessageIO.floatToBytes(yVelocity, buffer);
		buffer.put((byte) (down ? 1 : 0));
	}

	@Override
	public String toString()
	{
		return "Finger " + id + (down ? " down " : " up ") + "(" + x + "," + y + "," + xVelocity + "," + yVelocity
				+ ")";
	}
}
