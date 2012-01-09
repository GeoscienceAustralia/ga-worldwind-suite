package au.gov.ga.worldwind.tiler.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ByteBufferCache
{
	private static Map<String, CachedByteBuffer> buffers = new HashMap<String, CachedByteBuffer>();
	private static NavigableMap<Long, CachedByteBuffer> creationTimes = new TreeMap<Long, CachedByteBuffer>();

	static
	{
		Thread thread = new Thread(new CacheCleaner());
		thread.setDaemon(true);
		thread.setName("ByteBuffer cache cleaner");
		thread.start();
	}

	public static ByteBuffer getByteBuffer(int size)
	{
		synchronized (creationTimes)
		{
			String id = calculateId(size);
			if (buffers.containsKey(id))
			{
				CachedByteBuffer buffer = buffers.get(id);
				buffer.use();
				return buffer.buffer;
			}

			CachedByteBuffer buffer = new CachedByteBuffer(size);
			return buffer.buffer;
		}
	}

	private static String calculateId(int size)
	{
		return Thread.currentThread().hashCode() + "_" + size;
	}

	private static class CachedByteBuffer
	{
		public long lastUseTime;
		public final ByteBuffer buffer;
		public final String id;

		public CachedByteBuffer(int size)
		{
			buffer = ByteBuffer.allocateDirect(size);
			id = calculateId(size);
			buffers.put(id, this);
			use();
		}

		public void use()
		{
			creationTimes.remove(lastUseTime);
			lastUseTime = System.nanoTime();
			creationTimes.put(lastUseTime, this);
		}
	}

	private static class CacheCleaner implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				long currentNanoTime = System.nanoTime();
				long tenSecondsAgo = currentNanoTime - 1000000000L * 10L;

				synchronized (creationTimes)
				{
					NavigableMap<Long, CachedByteBuffer> headMap = creationTimes.headMap(tenSecondsAgo, false);
					if (!headMap.isEmpty())
					{
						List<Long> keysToRemove = new ArrayList<Long>();
						for (Entry<Long, CachedByteBuffer> entry : headMap.entrySet())
						{
							buffers.remove(entry.getValue().id);
							keysToRemove.add(entry.getKey());
						}
						for (Long keyToRemove : keysToRemove)
						{
							creationTimes.remove(keyToRemove);
						}
					}
				}
			}
		}
	}
}
