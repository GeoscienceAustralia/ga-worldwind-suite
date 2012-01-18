package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.cache.Cacheable;

/**
 * Defines a (sub) segment of a {@link Path}. Segments are defined in
 * percentages; a vertical 'top' and 'bottom' percentage of the path, and a
 * horizontal 'start' and 'end' of the path.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Segment implements Cacheable
{
	//percentages, between 0.0 and 1.0
	private final double start, end;
	private final double top, bottom;

	public final static Segment FULL = new Segment(0d, 1d, 0d, 1d);

	public Segment(double start, double end, double top, double bottom)
	{
		this.start = start;
		this.end = end;
		this.top = top;
		this.bottom = bottom;
	}

	public double getStart()
	{
		return start;
	}

	public double getEnd()
	{
		return end;
	}

	public double getTop()
	{
		return top;
	}

	public double getBottom()
	{
		return bottom;
	}

	public double getHorizontalCenter()
	{
		return 0.5 * (start + end);
	}

	public double getHorizontalDelta()
	{
		return end - start;
	}

	public double getVerticalCenter()
	{
		return 0.5 * (top + bottom);
	}

	public double getVerticalDelta()
	{
		return top - bottom;
	}

	@Override
	public long getSizeInBytes()
	{
		return 4 * Double.SIZE;
	}

	@Override
	public String toString()
	{
		return "(" + start + "," + top + " to " + end + "," + bottom + ")";
	}
}
