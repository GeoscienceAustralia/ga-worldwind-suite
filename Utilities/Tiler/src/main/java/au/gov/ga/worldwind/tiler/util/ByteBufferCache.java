package au.gov.ga.worldwind.tiler.util;

import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ByteBufferCache
{
	private static NavigableMap<Integer, ByteBuffer> availableBuffers = new TreeMap<Integer, ByteBuffer>();

	public synchronized static ByteBuffer takeByteBuffer(int size)
	{
		Entry<Integer, ByteBuffer> entry = availableBuffers.higherEntry(size);
		if (entry != null)
		{
			ByteBuffer buffer = entry.getValue();
			availableBuffers.remove(entry.getKey());
			buffer.rewind();
			buffer.limit(size);
			return buffer;
		}
		return ByteBuffer.allocateDirect(size);
	}

	public synchronized static void returnByteBuffer(ByteBuffer buffer)
	{
		if (buffer != null)
		{
			availableBuffers.put(buffer.capacity(), buffer);
		}
	}
}
