package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

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

	public boolean isBottom()
	{
		return bottom;
	}

	public void setBottom(boolean bottom)
	{
		this.bottom = bottom;
	}
}
