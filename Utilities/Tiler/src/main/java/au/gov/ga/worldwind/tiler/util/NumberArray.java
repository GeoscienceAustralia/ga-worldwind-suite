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
 * Represents a number array.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NumberArray
{
	private long[] longs;
	private double[] doubles;

	public NumberArray(int length)
	{
		longs = new long[length];
		doubles = new double[length];
	}

	public int length()
	{
		return longs.length;
	}

	public byte getByte(int i)
	{
		return (byte) longs[i];
	}

	public void setByte(int i, byte v)
	{
		longs[i] = v;
		doubles[i] = v;
	}

	public short getShort(int i)
	{
		return (short) longs[i];
	}

	public void setShort(int i, short v)
	{
		longs[i] = v;
		doubles[i] = v;
	}

	public int getInt(int i)
	{
		return (int) longs[i];
	}

	public void setInt(int i, int v)
	{
		longs[i] = v;
		doubles[i] = v;
	}

	public long getLong(int i)
	{
		return longs[i];
	}

	public void setLong(int i, long v)
	{
		longs[i] = v;
		doubles[i] = v;
	}

	public float getFloat(int i)
	{
		return (float) doubles[i];
	}

	public void setFloat(int i, float v)
	{
		longs[i] = (long) v;
		doubles[i] = v;
	}

	public double getDouble(int i)
	{
		return doubles[i];
	}

	public void setDouble(int i, double v)
	{
		longs[i] = (long) v;
		doubles[i] = v;
	}

	public String toString(boolean isFloat)
	{
		String s = "(";
		for (int i = 0; i < length(); i++)
		{
			if (i > 0)
				s += ", ";
			s += isFloat ? doubles[i] : longs[i];
		}
		s += ")";
		return s;
	}
}
