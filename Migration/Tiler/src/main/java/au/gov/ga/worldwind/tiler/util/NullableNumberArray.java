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
 * Represents a number array that can contain null elements.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NullableNumberArray
{
	private Long[] longs;
	private Double[] doubles;

	public NullableNumberArray(int length)
	{
		longs = new Long[length];
		doubles = new Double[length];
	}

	/**
	 * @return Array length
	 */
	public int length()
	{
		return longs.length;
	}

	/**
	 * Get the value at the given index as a byte.
	 * 
	 * @param i
	 * @return
	 */
	public Byte getByte(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].byteValue();
	}

	/**
	 * Set the value at the given index to the given byte. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setByte(int i, Byte v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v.longValue();
			doubles[i] = v.doubleValue();
		}
	}

	/**
	 * Get the value at the given index as a short.
	 * 
	 * @param i
	 * @return
	 */
	public Short getShort(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].shortValue();
	}

	/**
	 * Set the value at the given index to the given short. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setShort(int i, Short v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v.longValue();
			doubles[i] = v.doubleValue();
		}
	}

	/**
	 * Get the value at the given index as an integer.
	 * 
	 * @param i
	 * @return
	 */
	public Integer getInt(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].intValue();
	}

	/**
	 * Set the value at the given index to the given integer. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setInt(int i, Integer v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v.longValue();
			doubles[i] = v.doubleValue();
		}
	}

	/**
	 * Get the value at the given index as a long.
	 * 
	 * @param i
	 * @return
	 */
	public Long getLong(int i)
	{
		return longs[i];
	}

	/**
	 * Set the value at the given index to the given long. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setLong(int i, Long v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v;
			doubles[i] = v.doubleValue();
		}
	}

	/**
	 * Get the value at the given index as a float.
	 * 
	 * @param i
	 * @return
	 */
	public Float getFloat(int i)
	{
		if (doubles[i] == null)
			return null;
		return doubles[i].floatValue();
	}

	/**
	 * Set the value at the given index to the given float. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setFloat(int i, Float v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v.longValue();
			doubles[i] = v.doubleValue();
		}
	}

	/**
	 * Get the value at the given index as a double.
	 * 
	 * @param i
	 * @return
	 */
	public Double getDouble(int i)
	{
		if (doubles[i] == null)
			return null;
		return doubles[i];
	}

	/**
	 * Set the value at the given index to the given double. Can be null.
	 * 
	 * @param i
	 * @param v
	 */
	public void setDouble(int i, Double v)
	{
		if (v == null)
		{
			longs[i] = null;
			doubles[i] = null;
		}
		else
		{
			longs[i] = v.longValue();
			doubles[i] = v;
		}
	}

	/**
	 * Set the array to the given double values.
	 * 
	 * @param d
	 */
	public void setDoubles(Double[] d)
	{
		for (int i = 0; i < d.length; i++)
		{
			setDouble(i, d[i]);
		}
	}

	/**
	 * Generate a comma-separated string representation of this array.
	 * 
	 * @param isFloat
	 *            Should the doubles or the longs be used?
	 * @return
	 */
	public String toString(boolean isFloat)
	{
		String s = "(";
		for (int i = 0; i < length(); i++)
		{
			if (i > 0)
				s += ", ";
			s += isFloat ? (Object) doubles[i] : (Object) longs[i];
		}
		s += ")";
		return s;
	}

	/**
	 * Does this array equal the given array of doubles?
	 * 
	 * @param d
	 * @param offset
	 *            Offset in d
	 * @return
	 */
	public boolean equalsDoubles(double[] d, int offset)
	{
		if (d == null || d.length < length() + offset)
			return false;
		for (int i = 0; i < length(); i++)
			if (getDouble(i) != d[i + offset])
				return false;
		return true;
	}

	/**
	 * Does this array equal the given array of longs?
	 * 
	 * @param l
	 * @param offset
	 *            Offset in l
	 * @return
	 */
	public boolean equalsLongs(long[] l, int offset)
	{
		if (l == null || l.length < length() + offset)
			return false;
		for (int i = 0; i < length(); i++)
			if (getLong(i) != l[i + offset])
				return false;
		return true;
	}
}
