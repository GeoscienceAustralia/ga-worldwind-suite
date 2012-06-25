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
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link BufferManager} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BufferManagerTest
{

	@Before
	public void setup()
	{
		BufferManager.reset();
	}
	
	@Test
	public void testTakeBufferOnce()
	{
		ByteBuffer buffer = BufferManager.takeByteBuffer(16);
		
		assertEquals(16, buffer.limit());
		assertEquals(0, buffer.position());
		assertBufferIsCleared(buffer);
		
		assertEquals(0, BufferManager.getBuffers().size());
	}
	
	@Test
	public void testTakeBufferTwiceNoReturn()
	{
		ByteBuffer buffer1 = BufferManager.takeByteBuffer(16);
		assertEquals(16, buffer1.limit());
		assertEquals(0, buffer1.position());
		assertBufferIsCleared(buffer1);
		
		ByteBuffer buffer2 = BufferManager.takeByteBuffer(32);
		assertEquals(32, buffer2.limit());
		assertEquals(0, buffer2.position());
		assertBufferIsCleared(buffer2);
		
		assertEquals(0, BufferManager.getBuffers().size());
	}
	
	@Test
	public void testTakeBufferTwiceWithReturnSmallerBuffer()
	{
		ByteBuffer buffer1 = BufferManager.takeByteBuffer(16);
		assertEquals(16, buffer1.limit());
		assertEquals(0, buffer1.position());
		assertBufferIsCleared(buffer1);
		BufferManager.returnByteBuffer(buffer1);
		
		ByteBuffer buffer2 = BufferManager.takeByteBuffer(8);
		assertEquals(8, buffer2.limit());
		assertEquals(0, buffer2.position());
		assertBufferIsCleared(buffer2);
		BufferManager.returnByteBuffer(buffer2);
		
		// Expect buffer re-use
		assertEquals(1, BufferManager.getBuffers().size());
	}
	
	@Test
	public void testTakeBufferTwiceWithReturnLargerBuffer()
	{
		ByteBuffer buffer1 = BufferManager.takeByteBuffer(16);
		assertEquals(16, buffer1.limit());
		assertEquals(0, buffer1.position());
		assertBufferIsCleared(buffer1);
		BufferManager.returnByteBuffer(buffer1);
		
		ByteBuffer buffer2 = BufferManager.takeByteBuffer(32);
		assertEquals(32, buffer2.limit());
		assertEquals(0, buffer2.position());
		assertBufferIsCleared(buffer2);
		BufferManager.returnByteBuffer(buffer2);
		
		// Expect no buffer re-use
		assertEquals(2, BufferManager.getBuffers().size());
	}
	
	@Test
	public void testBufferIsClearedWhenReused()
	{
		ByteBuffer buffer1 = BufferManager.takeByteBuffer(16);
		assertBufferIsCleared(buffer1);
		BufferUtil.putLongValue(buffer1, gdalconstConstants.GDT_UInt16, 115);
		BufferManager.returnByteBuffer(buffer1);
		
		// Buffer should have been reused, and should be cleared
		ByteBuffer buffer2 = BufferManager.takeByteBuffer(8);
		assertEquals(8, buffer2.limit());
		assertEquals(0, buffer2.position());
		assertBufferIsCleared(buffer2);
		BufferManager.returnByteBuffer(buffer2);
		
		assertEquals(1, BufferManager.getBuffers().size());
	}
	
	private void assertBufferIsCleared(ByteBuffer buffer)
	{
		buffer.rewind();
		
		for (int i = 0; i < buffer.limit(); i++)
		{
			assertEquals(0, buffer.get());
		}
		
		buffer.rewind();
	}
	
}
