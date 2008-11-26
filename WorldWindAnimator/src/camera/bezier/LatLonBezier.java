package camera.bezier;

import java.io.Serializable;

import camera.params.LatLon;

public class LatLonBezier implements Serializable
{
	public final LatLon latlon;
	public final LatLon in;
	public final LatLon out;

	public LatLonBezier(LatLon latlon, LatLon in, LatLon out)
	{
		this.latlon = latlon;
		this.in = in;
		this.out = out;
	}
}
