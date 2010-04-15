package util;

public class NullableNumberArray
{
	private Long[] longs;
	private Double[] doubles;

	public NullableNumberArray(int length)
	{
		longs = new Long[length];
		doubles = new Double[length];
	}

	public int length()
	{
		return longs.length;
	}

	public Byte getByte(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].byteValue();
	}

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

	public Short getShort(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].shortValue();
	}

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

	public Integer getInt(int i)
	{
		if (longs[i] == null)
			return null;
		return longs[i].intValue();
	}

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

	public Long getLong(int i)
	{
		return longs[i];
	}

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

	public Float getFloat(int i)
	{
		if (doubles[i] == null)
			return null;
		return doubles[i].floatValue();
	}

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

	public Double getDouble(int i)
	{
		if (doubles[i] == null)
			return null;
		return doubles[i];
	}

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
	
	public String toString(boolean isFloat)
	{
		String s = "(";
		for (int i = 0; i < length(); i++)
		{
			if (i > 0)
				s += ", ";
			s += isFloat ? (Object)doubles[i] : (Object)longs[i];
		}
		s += ")";
		return s;
	}
}
