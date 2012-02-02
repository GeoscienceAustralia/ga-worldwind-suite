package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/**
 * {@link Position} subclass which adds an {@link TopBottomPosition#isBottom()}
 * field, which defines whether this position is at the top or the bottom of a
 * shape. See {@link TopBottomFastShape}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TopBottomPosition extends Position
{
	private boolean bottom = false;

	public TopBottomPosition(LatLon latLon, double elevation, boolean bottom)
	{
		super(latLon, elevation);
		this.bottom = bottom;
	}

	public TopBottomPosition(Angle latitude, Angle longitude, double elevation, boolean bottom)
	{
		super(latitude, longitude, elevation);
		this.bottom = bottom;
	}

	/**
	 * @return Is this position at the bottom of the shape?
	 */
	public boolean isBottom()
	{
		return bottom;
	}

	/**
	 * Set this position to be the bottom of the shape.
	 * 
	 * @param bottom
	 */
	public void setBottom(boolean bottom)
	{
		this.bottom = bottom;
	}
}
