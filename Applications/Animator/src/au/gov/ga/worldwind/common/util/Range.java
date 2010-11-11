package au.gov.ga.worldwind.common.util;

/**
 * Represents an immutable range of comparable values (eg. <code>[minValue, maxValue]</code>)
 */
public class Range<C extends Comparable<C>>
{
	private C minValue;
	private boolean includeMin = true;
	
	private C maxValue;
	private boolean includeMax = true;
	
	
	/**
	 * Create a inclusive range <code>[minValue, maxValue]</code>
	 */
	public Range(C minValue, C maxValue)
	{
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	/**
	 * Create new range, specifying the inclusivity.
	 */
	public Range(C minValue, boolean includeMin,  C maxValue, boolean includeMax)
	{
		this.minValue = minValue;
		this.includeMin = includeMin;
		
		this.maxValue = maxValue;
		this.includeMax = includeMax;
	}

	/**
	 * @return Whether this range is open on the left (lower) side
	 */
	public boolean isOpenLeft()
	{
		return minValue == null;
	}
	
	/**
	 * @return Whether this range is inclusive of the left (minimum) value
	 */
	public boolean isInclusiveLeft()
	{
		return !isOpenLeft() && includeMin;
	}
	
	/**
	 * @return Whether this range is open on the right (higher) side
	 */
	public boolean isOpenRight()
	{
		return maxValue == null;
	}
	
	/**
	 * @return Whether this range is inclusive of the right (maximum) value
	 */
	public boolean isInclusiveRight()
	{
		return !isOpenRight() && includeMax;
	}
	
	public C getMinValue()
	{
		return minValue;
	}
	
	public C getMaxValue()
	{
		return maxValue;
	}
	
	/**
	 * @return Whether the provided value is contained in this range
	 */
	public boolean contains(C value)
	{
		if (value == null)
		{
			return false;
		}
		
		return greaterThanMin(value) && lessThanMax(value);
	}

	private boolean lessThanMax(C value)
	{
		if (isOpenRight())
		{
			return true;
		}
		
		int compareValue = maxValue.compareTo(value);
		
		if (isInclusiveRight())
		{
			return compareValue >= 0;
		}
		return compareValue > 0;
	}

	private boolean greaterThanMin(C value)
	{
		if (isOpenLeft())
		{
			return true;
		}
		
		int compareValue = minValue.compareTo(value);
		
		if (isInclusiveLeft())
		{
			return compareValue <= 0;
		}
		return compareValue < 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		Range<C> other = null;
		try
		{
			other = (Range<C>)obj;
		}
		catch (Exception e)
		{
			return false;
		}
		
		return this.includeMax == other.includeMax && this.includeMin == other.includeMin && nullSafeEquals(minValue, other.minValue) && nullSafeEquals(maxValue, other.maxValue);
	}
	
	private boolean nullSafeEquals(Object o1, Object o2)
	{
		if (o1 == null && o2 == null)
		{
			return true;
		}
		
		if (o1 != null && o2 != null)
		{
			return o1.equals(o2);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return (minValue == null ? minValue.hashCode() : 31) + (maxValue == null ? maxValue.hashCode() : 131);
	}
}