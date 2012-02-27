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

/**
 * Represents a array value range with a minimum/maximum for each array entry.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MinMaxArray
{
	private Long[] minLongs;
	private Double[] minDoubles;
	private Long[] maxLongs;
	private Double[] maxDoubles;
	private boolean allNullCache;
	private boolean allNullDirty = true;

	/**
	 * Create a new {@link MinMaxArray} instance.
	 * 
	 * @param length
	 *            Length of the arrays
	 */
	public MinMaxArray(int length)
	{
		minLongs = new Long[length];
		minDoubles = new Double[length];
		maxLongs = new Long[length];
		maxDoubles = new Double[length];
	}

	/**
	 * @return Length of the range arrays
	 */
	public int length()
	{
		return minLongs.length;
	}

	/**
	 * Set the min/max as longs at the given index
	 * 
	 * @param i
	 * @param min
	 * @param max
	 */
	public void setMinMaxLong(int i, Long min, Long max)
	{
		minLongs[i] = min;
		maxLongs[i] = max;
		minDoubles[i] = min != null ? min.doubleValue() : null;
		maxDoubles[i] = max != null ? max.doubleValue() : null;
		allNullDirty = true;
	}

	/**
	 * Set all min/max values as longs
	 * 
	 * @param min
	 * @param max
	 */
	public void setMinMaxLongs(Long[] min, Long[] max)
	{
		for (int i = 0; i < min.length; i++)
			setMinMaxLong(i, min[i], max[i]);
	}

	/**
	 * Set the min/max as doubles at the given index
	 * 
	 * @param i
	 * @param min
	 * @param max
	 */
	public void setMinMaxDouble(int i, Double min, Double max)
	{
		minDoubles[i] = min;
		maxDoubles[i] = max;
		minLongs[i] = min != null ? min.longValue() : null;
		maxLongs[i] = max != null ? max.longValue() : null;
		allNullDirty = true;
	}

	/**
	 * Set all min/max values as doubles
	 * 
	 * @param min
	 * @param max
	 */
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

	/**
	 * Do the given long values lie within the ranges in this object?
	 * 
	 * @param values
	 * @return
	 */
	public boolean isBetweenLong(long[] values)
	{
		if (values.length != length())
			return false;

		if (allNull())
			return false;

		for (int i = 0; i < length(); i++)
		{
			if ((minLongs[i] != null && values[i] < minLongs[i]) || (maxLongs[i] != null && values[i] > maxLongs[i]))
				return false;
		}

		return true;
	}

	/**
	 * Do the given double values lie within the ranges in this object?
	 * 
	 * @param values
	 * @return
	 */
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

	/**
	 * Generate a string containing the values from this object.
	 * 
	 * @param isFloat
	 *            Should the double values or the long values be used?
	 * @param mins
	 *            Should the mins or the maxs be used?
	 * @return String containing a comma-separated list of the mins or maxs from
	 *         this object.
	 */
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
