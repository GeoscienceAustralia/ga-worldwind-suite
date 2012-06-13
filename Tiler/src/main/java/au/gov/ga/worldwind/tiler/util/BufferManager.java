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
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A simple (and fairly naive) buffer manager that endeavours to re-use buffer objects
 * where possible in an attempt to avoid OOM errors. 
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BufferManager
{
	/** The map of available buffers. Capacity->Buffer */
	private static NavigableMap<Integer, ByteBuffer> availableBuffers = new TreeMap<Integer, ByteBuffer>();
	
	/** Clears the buffer pool */
	public synchronized static void reset()
	{
		availableBuffers.clear();
	}
	
	/**
	 * Get a buffer of the given size, re-using an existing buffer if possible.
	 * <p/>
	 * When finished with the buffer it should be returned to the pool using {@link #returnByteBuffer(ByteBuffer)}.
	 */
	public synchronized static ByteBuffer takeByteBuffer(int size)
	{
		Entry<Integer, ByteBuffer> entry = availableBuffers.ceilingEntry(size);
		if (entry != null)
		{
			ByteBuffer buffer = entry.getValue();
			availableBuffers.remove(entry.getKey());
			
			buffer.rewind();
			buffer.limit(size);
			
			// Clear the buffer incase there's something in there...
			for (int i = 0; i < size; i++)
			{
				buffer.put((byte)0);
			}
			
			buffer.rewind();
			return buffer;
		}
		return ByteBuffer.allocateDirect(size);
	}

	/**
	 * Return a buffer for use by other objects.
	 * <p/>
	 * <b>Important:</b> After calling this method DO NOT use the buffer object. It may be
	 * allocated to another object for another use. If another
	 * buffer is required it should be obtained from {@link #takeByteBuffer(int)}.
	 */
	public synchronized static void returnByteBuffer(ByteBuffer buffer)
	{
		if (buffer != null)
		{
			availableBuffers.put(buffer.capacity(), buffer);
		}
	}
	
	/**
	 * @return The buffer pool. For testing purposes only.
	 */
	static NavigableMap<Integer, ByteBuffer> getBuffers()
	{
		return availableBuffers;
	}
	
	private BufferManager(){};
	
}
