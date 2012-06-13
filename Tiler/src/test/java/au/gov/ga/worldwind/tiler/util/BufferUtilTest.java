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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Test;

/**
 * Unit tests for the {@link BufferUtil} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BufferUtilTest
{

	@Test
	public void testPutGetLongWithByteType()
	{
		long valueIn = 115;
		int type = gdalconstConstants.GDT_Byte;
		
		ByteBuffer buffer = ByteBuffer.allocate(1);
		
		BufferUtil.putLongValue(buffer, type, valueIn);
		buffer.rewind();
		long valueOut = BufferUtil.getLongValue(buffer, type);

		assertEquals(valueIn, valueOut);
	}
	
	@Test
	public void testPutGetLongWithUInt16Type()
	{
		long valueIn = 115;
		int type = gdalconstConstants.GDT_UInt16;
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		
		BufferUtil.putLongValue(buffer, type, valueIn);
		buffer.rewind();
		long valueOut = BufferUtil.getLongValue(buffer, type);

		assertEquals(valueIn, valueOut);
	}
	
	@Test
	public void testPutGetLongWithInt16Type()
	{
		long valueIn = 115;
		int type = gdalconstConstants.GDT_Int16;
		
		ByteBuffer buffer = ByteBuffer.allocate(2);
		
		BufferUtil.putLongValue(buffer, type, valueIn);
		buffer.rewind();
		long valueOut = BufferUtil.getLongValue(buffer, type);

		assertEquals(valueIn, valueOut);
	}
	
	@Test
	public void testPutGetLongWithUInt32Type()
	{
		long valueIn = 115;
		int type = gdalconstConstants.GDT_UInt32;
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
		
		BufferUtil.putLongValue(buffer, type, valueIn);
		buffer.rewind();
		long valueOut = BufferUtil.getLongValue(buffer, type);

		assertEquals(valueIn, valueOut);
	}
	
	@Test
	public void testPutGetLongWithInt32Type()
	{
		long valueIn = 115;
		int type = gdalconstConstants.GDT_Int32;
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
		
		BufferUtil.putLongValue(buffer, type, valueIn);
		buffer.rewind();
		long valueOut = BufferUtil.getLongValue(buffer, type);

		assertEquals(valueIn, valueOut);
	}
	
	@Test
	public void testPutGetDoubleWithFloat32Type()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float32;
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
		
		BufferUtil.putDoubleValue(buffer, type, valueIn);
		buffer.rewind();
		double valueOut = BufferUtil.getDoubleValue(buffer, type);

		assertEquals(valueIn, valueOut, 0.0001);
	}
	
	@Test
	public void testPutGetDoubleWithFloat64Type()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		
		BufferUtil.putDoubleValue(buffer, type, valueIn);
		buffer.rewind();
		double valueOut = BufferUtil.getDoubleValue(buffer, type);

		assertEquals(valueIn, valueOut, 0.0001);
	}
	
	@Test
	public void testPutGetDoubleWithValidIndex()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		BufferUtil.putDoubleValue(3, buffer, type, valueIn);
		buffer.rewind();
		double valueOut = BufferUtil.getDoubleValue(3, buffer, type);

		assertEquals(valueIn, valueOut, 0.0001);
	}
	
	@Test( expected = IndexOutOfBoundsException.class )
	public void testPutDoubleWithNegativeIndex()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		BufferUtil.putDoubleValue(-1, buffer, type, valueIn);
	}
	
	@Test( expected = IndexOutOfBoundsException.class )
	public void testPutDoubleWithTooLargeIndex()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		BufferUtil.putDoubleValue(16, buffer, type, valueIn);
	}
	
	@Test( expected = IndexOutOfBoundsException.class )
	public void testGetDoubleWithNegativeIndex()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		
		BufferUtil.putDoubleValue(buffer, type, valueIn);
		buffer.rewind();
		
		BufferUtil.getDoubleValue(-1, buffer, type);
	}
	
	@Test( expected = IndexOutOfBoundsException.class )
	public void testGetDoubleWithTooLargeIndex()
	{
		double valueIn = 115.15;
		int type = gdalconstConstants.GDT_Float64;
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		
		BufferUtil.putDoubleValue(buffer, type, valueIn);
		buffer.rewind();
		
		BufferUtil.getDoubleValue(16, buffer, type);
	}
}
