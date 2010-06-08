package au.gov.ga.worldwind.tiler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiMap<K, V> extends HashMap<K, List<V>>
{
	public void put(K key, V value)
	{
		List<V> values = null;
		if (containsKey(key))
		{
			values = get(key);
		}
		else
		{
			values = new ArrayList<V>();
			put(key, values);
		}
		values.add(value);
	}
}
