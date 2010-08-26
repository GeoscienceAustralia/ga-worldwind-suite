package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import java.util.HashMap;
import java.util.Map;

public class FileLockSharer
{
	private static Map<String, Object> locks = new HashMap<String, Object>();

	public static Object getLock(String dataCacheName)
	{
		if (!locks.containsKey(dataCacheName))
		{
			locks.put(dataCacheName, new Object());
		}
		return locks.get(dataCacheName);
	}
}
