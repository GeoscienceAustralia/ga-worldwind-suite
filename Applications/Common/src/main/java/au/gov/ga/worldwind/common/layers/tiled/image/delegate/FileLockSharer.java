package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class which allows sharing of a fileLock (object on which blocks are
 * synchronized before reading from and writing to the cache). This is useful as
 * some layers may share the same imagery but do different post processing on
 * the textures.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileLockSharer
{
	private static Map<String, Object> locks = new HashMap<String, Object>();

	/**
	 * Get an object on which to synchronize for reading/writing to the cache
	 * location identified by dataCacheName.
	 * 
	 * @param dataCacheName
	 *            Cache location
	 * @return Object on which to synchronize
	 */
	public static Object getLock(String dataCacheName)
	{
		if (!locks.containsKey(dataCacheName))
		{
			locks.put(dataCacheName, new Object());
		}
		return locks.get(dataCacheName);
	}
}
