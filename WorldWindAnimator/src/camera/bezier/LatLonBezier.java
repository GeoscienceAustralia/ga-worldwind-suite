package camera.bezier;

import camera.params.LatLon;
import camera.vector.Vector2;

public class LatLonBezier
{
	public final LatLon latlon;
	public final LatLon in;
	public final LatLon out;

	private Bezier<Vector2> bezier;

	public LatLonBezier(LatLon latlon, LatLon in, LatLon out)
	{
		this.latlon = latlon;
		this.in = in;
		this.out = out;
	}

	public void setPreviousPoint(LatLonBezier previous)
	{
		Vector2 v1 = previous.latlon.toVector2();
		Vector2 vout = previous.out.toVector2(previous.latlon);

		Vector2 v2 = latlon.toVector2(previous.latlon);
		Vector2 vin = in.toVector2(v2);

		bezier = new Bezier<Vector2>(v1, vout, vin, v2);
	}

	public double getDistance()
	{
		if (bezier == null)
			return 0;
		return bezier.getLength();
	}

	public LatLon getLatLon(double percent)
	{
		if (bezier == null)
			return latlon;
		Vector2 v = bezier.pointAt(percent);
		return LatLon.fromVector2(v);
	}
}
