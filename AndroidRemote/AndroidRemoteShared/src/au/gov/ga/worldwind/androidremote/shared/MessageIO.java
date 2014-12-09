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
package au.gov.ga.worldwind.androidremote.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Helper class used to serialize/deserialize messages from byte buffers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MessageIO
{
	/**
	 * Read the next message from the given input stream. First reads an integer
	 * from the stream which represents the ordinal of the {@link MessageId}.
	 * Then reads another integer which represents the length of the message.
	 * Finally reads <code>length</code> bytes into a ByteBuffer, and then
	 * instantiates the message from the byte data.
	 * 
	 * @param is
	 *            InputStream to read from.
	 * @return New Message read from the given InputStream.
	 * @throws IOException
	 */
	public static Message<?> readMessage(InputStream is) throws IOException
	{
		Integer id = readInt(is);
		Integer length = readInt(is);
		if (id == null || length == null)
		{
			return null;
		}
		if (id < 0 || id >= MessageId.values().length || length < 0)
		{
			return null;
		}
		byte[] array = new byte[length];
		readArray(is, array);
		MessageId m = MessageId.values()[id];
		ByteBuffer buffer = ByteBuffer.wrap(array);
		return m.fromBytes(buffer);
	}

	/**
	 * Read an integer from the given InputStream.
	 * 
	 * @param is
	 * @return Integer read from the InputStream, or null if failed.
	 * @throws IOException
	 */
	protected static Integer readInt(InputStream is) throws IOException
	{
		byte[] b = new byte[4];
		if (!readArray(is, b))
		{
			return null;
		}
		return bytesToInt(b);
	}

	/**
	 * Read bytes from the given InputStream into the given byte array.
	 * 
	 * @param is
	 *            InputStream to read from.
	 * @param b
	 *            Byte array to read into.
	 * @return True if the array was filled up, false otherwise.
	 * @throws IOException
	 */
	protected static boolean readArray(InputStream is, byte[] b) throws IOException
	{
		int len = b.length;
		int off = 0;
		while (off < len)
		{
			int read = is.read(b, off, len - off);
			if (read <= 0)
			{
				return false;
			}
			off += read;
		}
		return true;
	}

	/**
	 * Write a message to an OutputStream.
	 * 
	 * @param message
	 *            Message to write.
	 * @param os
	 *            OutputStream to write to.
	 * @throws IOException
	 */
	public static void writeMessage(Message<?> message, OutputStream os) throws IOException
	{
		byte[] array = new byte[message.getLength()];
		ByteBuffer buffer = ByteBuffer.wrap(array);
		message.toBytes(buffer);
		writeInt(message.getId().getId(), os);
		writeInt(message.getLength(), os);
		os.write(array);
		os.flush();
	}

	/**
	 * Write an int to a an OutputStream as 4 bytes.
	 * 
	 * @param i
	 *            Integer to write.
	 * @param os
	 *            OutputStream to write to.
	 * @throws IOException
	 */
	protected static void writeInt(int i, OutputStream os) throws IOException
	{
		byte[] b = new byte[4];
		intToBytes(i, b);
		os.write(b);
	}

	/**
	 * Convert the next 4 bytes in a ByteBuffer to an int.
	 * 
	 * @param buffer
	 * @return Int read from the buffer.
	 */
	public static int bytesToInt(ByteBuffer buffer)
	{
		return ((buffer.get() & 0xff) << 24) | ((buffer.get() & 0xff) << 16) | ((buffer.get() & 0xff) << 8)
				| ((buffer.get() & 0xff) << 0);
	}

	/**
	 * Convert a 4 byte array to an int.
	 * 
	 * @param b
	 *            Byte array of length 4.
	 * @return Int converted from the byte array.
	 */
	protected static int bytesToInt(byte[] b)
	{
		return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | ((b[3] & 0xff) << 0);
	}

	/**
	 * Store an int in a ByteBuffer.
	 * 
	 * @param i
	 *            Int to store.
	 * @param buffer
	 *            Buffer to write to.
	 */
	public static void intToBytes(int i, ByteBuffer buffer)
	{
		buffer.put((byte) ((i >> 24) & 0xff));
		buffer.put((byte) ((i >> 16) & 0xff));
		buffer.put((byte) ((i >> 8) & 0xff));
		buffer.put((byte) (i & 0xff));
	}

	/**
	 * Store an int in a byte array.
	 * 
	 * @param i
	 *            Int to store.
	 * @param b
	 *            Byte array to save to.
	 */
	protected static void intToBytes(int i, byte[] b)
	{
		b[0] = (byte) ((i >> 24) & 0xff);
		b[1] = (byte) ((i >> 16) & 0xff);
		b[2] = (byte) ((i >> 8) & 0xff);
		b[3] = (byte) (i & 0xff);
	}

	/**
	 * Convert the next 4 bytes in a ByteBuffer to a float.
	 * 
	 * @param buffer
	 * @return Float read from buffer.
	 */
	public static float bytesToFloat(ByteBuffer buffer)
	{
		return Float.intBitsToFloat(bytesToInt(buffer));
	}

	/**
	 * Store a float in a ByteBuffer.
	 * 
	 * @param f
	 *            Float to store.
	 * @param buffer
	 *            Buffer to write to.
	 */
	public static void floatToBytes(float f, ByteBuffer buffer)
	{
		intToBytes(Float.floatToIntBits(f), buffer);
	}

	/**
	 * Convert the next 8 bytes in a ByteBuffer to a double.
	 * 
	 * @param buffer
	 * @return Double read from buffer.
	 */
	public static double bytesToDouble(ByteBuffer buffer)
	{
		return buffer.getDouble();
	}

	/**
	 * Store a float in a ByteBuffer.
	 * 
	 * @param d
	 *            Double to store.
	 * @param buffer
	 *            Buffer to write to.
	 */
	public static void doubleToBytes(double d, ByteBuffer buffer)
	{
		buffer.putDouble(d);
	}

	/**
	 * Read <code>length</code> bytes from a ByteBuffer, and convert them to a
	 * string using the given charset.
	 * 
	 * @param buffer
	 *            Buffer to read from.
	 * @param length
	 *            Number of bytes to read.
	 * @param charset
	 *            Charset to use when converting bytes to a string.
	 * @return String read from the buffer.
	 */
	public static String bytesToString(ByteBuffer buffer, int length, Charset charset)
	{
		if (buffer.hasArray())
		{
			int offset = buffer.position();
			buffer.position(offset + length);
			return new String(buffer.array(), offset, length, charset);
		}
		else
		{
			byte[] array = new byte[length];
			buffer.get(array);
			return new String(array, charset);
		}
	}
}
