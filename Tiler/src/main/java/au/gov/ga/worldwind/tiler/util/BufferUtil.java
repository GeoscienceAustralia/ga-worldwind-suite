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
package au.gov.ga.worldwind.tiler.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.gdal.gdalconst.gdalconstConstants;

/**
 * Utility methods for working with {@link ByteBuffer}s
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BufferUtil
{
	private BufferUtil(){}
	
	/**
	 * Retrieves the next {@link Long} value from the provided buffer, handling byte conversions etc.
	 * 
	 * @param buffer The buffer to take the value from
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * 
	 * @return The next {@link Long} value from the buffer of the given type
	 */
	public static long getLongValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			return buffer.get() & 0xff;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			return buffer.getShort();
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			return buffer.getInt();
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			return getUInt16(buffer);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			return getUInt32(buffer);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Retrieves a {@link Long} value from the provided buffer, handling byte conversions etc.
	 * 
	 * @param index The index to retrieve the value from
	 * @param buffer The buffer to take the value from
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * 
	 * @return A {@link Long} value at the given index from the buffer of the given type
	 */
	public static long getLongValue(int index, ByteBuffer buffer, int bufferType)
	{
		if (index < 0 || index > buffer.limit())
		{
			throw new IndexOutOfBoundsException("Index " + index + " is outside buffer limit [0," + buffer.limit() + "]");
		}
		
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			return buffer.get(index) & 0xff;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			return buffer.getShort(index);
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			return buffer.getInt(index);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			return getUInt16(index, buffer);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			return getUInt32(index, buffer);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Retrieves the next floating point value from the provided buffer, handling byte conversions etc.
	 * 
	 * @param buffer The buffer to take the value from
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * 
	 * @return The next floating point value from the buffer of the given type
	 */
	public static double getDoubleValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			return buffer.getFloat();
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			return buffer.getDouble();
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Retrieves a floating point value from the provided buffer, handling byte conversions etc.
	 * 
	 * @param index The index to retrieve the value from
	 * @param buffer The buffer to take the value from
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * 
	 * @return A floating point value at the given index from the buffer of the given type
	 */
	public static double getDoubleValue(int index, ByteBuffer buffer, int bufferType)
	{
		if (index < 0 || index > buffer.limit())
		{
			throw new IndexOutOfBoundsException("Index " + index + " is outside buffer limit [0," + buffer.limit() + "]");
		}
		
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			return buffer.getFloat(index);
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			return buffer.getDouble(index);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Puts the given value into the provided buffer in the format appropriate to the given type.
	 * 
	 * @param buffer The buffer to put the value into
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * @param value The value to put into the buffer
	 */
	public static void putLongValue(ByteBuffer buffer, int bufferType, long value)
	{
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			buffer.put((byte) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			buffer.putShort((short) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			buffer.putInt((int) value);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			putUInt16(buffer, value);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			putUInt32(buffer, value);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Puts the given value into the provided buffer in the format appropriate to the given type.
	 * Values are put into the given index 
	 * 
	 * @param index The index to put the value in
	 * @param buffer The buffer to put the value into
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * @param value The value to put into the buffer
	 */
	public static void putLongValue(int index, ByteBuffer buffer, int bufferType, long value)
	{
		if (index < 0 || index > buffer.limit())
		{
			throw new IndexOutOfBoundsException("Index " + index + " is outside buffer limit [0," + buffer.limit() + "]");
		}
		
		if (bufferType == gdalconstConstants.GDT_Byte)
		{
			buffer.put(index, (byte) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			buffer.putShort(index, (short) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			buffer.putInt(index, (int) value);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			putUInt16(index, buffer, value);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			putUInt32(index, buffer, value);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Puts the given floating point value into the provided buffer in the format appropriate to the given type.
	 * 
	 * @param buffer The buffer to put the value into
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * @param value The value to put into the buffer
	 */
	public static void putDoubleValue(ByteBuffer buffer, int bufferType, double value)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			buffer.putFloat((float) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			buffer.putDouble(value);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	/**
	 * Puts the given floating point value into the provided buffer in the format appropriate to the given type.
	 * Values are put into the given index 
	 * 
	 * @param index The index to put the value in
	 * @param buffer The buffer to put the value into
	 * @param bufferType The type of buffer (see {@link gdalconstConstants} for values)
	 * @param value The value to put into the buffer
	 */
	public static void putDoubleValue(int index, ByteBuffer buffer, int bufferType, double value)
	{
		if (index < 0 || index > buffer.limit())
		{
			throw new IndexOutOfBoundsException("Index " + index + " is outside buffer limit [0," + buffer.limit() + "]");
		}
		
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			buffer.putFloat(index, (float) value);
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			buffer.putDouble(index, value);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	private static int getUInt16(ByteBuffer buffer)
	{
		int first = 0xff & buffer.get();
		int second = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 8 | second);
		}
		else
		{
			return (first | second << 8);
		}
	}

	private static int getUInt16(int index, ByteBuffer buffer)
	{
		int first = 0xff & buffer.get(index);
		int second = 0xff & buffer.get(index + 1);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 8 | second);
		}
		else
		{
			return (first | second << 8);
		}
	}

	private static long getUInt32(ByteBuffer buffer)
	{
		long first = 0xff & buffer.get();
		long second = 0xff & buffer.get();
		long third = 0xff & buffer.get();
		long fourth = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 24l | second << 16l | third << 8l | fourth);
		}
		else
		{
			return (first | second << 8l | third << 16l | fourth << 24l);
		}
	}

	private static long getUInt32(int index, ByteBuffer buffer)
	{
		long first = 0xff & buffer.get(index);
		long second = 0xff & buffer.get(index + 1);
		long third = 0xff & buffer.get(index + 2);
		long fourth = 0xff & buffer.get(index + 3);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 24l | second << 16l | third << 8l | fourth);
		}
		else
		{
			return (first | second << 8l | third << 16l | fourth << 24l);
		}
	}

	private static void putUInt16(ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 8) & 0xff);
		byte second = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(first);
			buffer.put(second);
		}
		else
		{
			buffer.put(second);
			buffer.put(first);
		}
	}

	private static void putUInt16(int index, ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 8) & 0xff);
		byte second = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(index, first);
			buffer.put(index + 1, second);
		}
		else
		{
			buffer.put(index, second);
			buffer.put(index + 1, first);
		}
	}

	private static void putUInt32(ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 24) & 0xff);
		byte second = (byte) ((value >> 16) & 0xff);
		byte third = (byte) ((value >> 8) & 0xff);
		byte fourth = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(first);
			buffer.put(second);
			buffer.put(third);
			buffer.put(fourth);
		}
		else
		{
			buffer.put(fourth);
			buffer.put(third);
			buffer.put(second);
			buffer.put(first);
		}
	}

	private static void putUInt32(int index, ByteBuffer buffer, long value)
	{
		byte first = (byte) ((value >> 24) & 0xff);
		byte second = (byte) ((value >> 16) & 0xff);
		byte third = (byte) ((value >> 8) & 0xff);
		byte fourth = (byte) (value & 0xff);
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			buffer.put(index, first);
			buffer.put(index + 1, second);
			buffer.put(index + 2, third);
			buffer.put(index + 3, fourth);
		}
		else
		{
			buffer.put(index, fourth);
			buffer.put(index + 1, third);
			buffer.put(index + 2, second);
			buffer.put(index + 3, first);
		}
	}
}
