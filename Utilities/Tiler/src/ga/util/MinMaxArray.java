package util;

public class MinMaxArray
{
	private Long[] minLongs;
	private Double[] minDoubles;
	private Long[] maxLongs;
	private Double[] maxDoubles;
	private boolean allNullCache;
	private boolean allNullDirty = true;

	public MinMaxArray(int length)
	{
		minLongs = new Long[length];
		minDoubles = new Double[length];
		maxLongs = new Long[length];
		maxDoubles = new Double[length];
	}

	public int length()
	{
		return minLongs.length;
	}

	public void setMinMaxLong(int i, Long min, Long max)
	{
		minLongs[i] = min;
		maxLongs[i] = max;
		minDoubles[i] = min != null ? min.doubleValue() : null;
		maxDoubles[i] = max != null ? max.doubleValue() : null;
		allNullDirty = true;
	}

	public void setMinMaxLongs(Long[] min, Long[] max)
	{
		for (int i = 0; i < min.length; i++)
			setMinMaxLong(i, min[i], max[i]);
	}

	public void setMinMaxDouble(int i, Double min, Double max)
	{
		minDoubles[i] = min;
		maxDoubles[i] = max;
		minLongs[i] = min != null ? min.longValue() : null;
		maxLongs[i] = max != null ? max.longValue() : null;
		allNullDirty = true;
	}

	public void setMinMaxDoubles(Double[] min, Double[] max)
	{
		for (int i = 0; i < min.length; i++)
			setMinMaxDouble(i, min[i], max[i]);
	}

	private boolean allNull()
	{
		if (allNullDirty)
		{
			allNullCache = true;
			for (int i = 0; i < length(); i++)
			{
				if (minLongs[i] != null || maxLongs[i] != null)
				{
					allNullCache = false;
					break;
				}
			}
			allNullDirty = false;
		}
		return allNullCache;
	}

	public boolean isBetweenLong(long[] values)
	{
		if (values.length != length())
			return false;

		if (allNull())
			return false;

		for (int i = 0; i < length(); i++)
		{
			if ((minLongs[i] != null && values[i] < minLongs[i])
					|| (maxLongs[i] != null && values[i] > maxLongs[i]))
				return false;
		}

		return true;
	}

	public boolean isBetweenDouble(double[] values)
	{
		if (values.length != length())
			return false;

		if (allNull())
			return false;

		for (int i = 0; i < length(); i++)
		{
			if ((minDoubles[i] != null && values[i] < minDoubles[i])
					|| (maxDoubles[i] != null && values[i] > maxDoubles[i]))
				return false;
		}

		return true;
	}

	public String toString(boolean isFloat, boolean mins)
	{
		Double[] ds = mins ? minDoubles : maxDoubles;
		Long[] ls = mins ? minLongs : maxLongs;
		String s = "(";
		for (int i = 0; i < length(); i++)
		{
			if (i > 0)
				s += ", ";
			s += isFloat ? (Object) ds[i] : (Object) ls[i];
		}
		s += ")";
		return s;
	}
}
