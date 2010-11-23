package au.gov.ga.worldwind.animator.math.interpolation;

import au.gov.ga.worldwind.animator.math.vector.Vector;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A simple linear interpolator that interpolates linearly between
 * a start and end vector
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class LinearInterpolator<V extends Vector<V>> implements Interpolator<V>
{

	private V start;
	private V end;
	
	/**
	 * Constructor. Initialises the start and end vectors.
	 * 
	 * @param start
	 * @param end
	 */
	public LinearInterpolator(V start, V end)
	{
		Validate.notNull(start, "A start vector is required");
		Validate.notNull(end, "An end vector is required");
		
		this.start = start;
		this.end = end;
	}

	@Override
	public V computeValue(double percent)
	{
		return start.interpolate(end, percent);
	}

	/**
	 * @return the start
	 */
	public V getStart()
	{
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(V start)
	{
		Validate.notNull(start, "A start vector is required");
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public V getEnd()
	{
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(V end)
	{
		Validate.notNull(end, "An end vector is required");
		this.end = end;
	}

}
