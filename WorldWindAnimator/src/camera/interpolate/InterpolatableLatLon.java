package camera.interpolate;

import camera.bezier.Bezier;
import camera.bezier.LatLonBezier;
import camera.params.LatLon;
import camera.vector.Vector2;

public class InterpolatableLatLon implements
		Interpolatable<LatLonBezier, LatLon>
{
	private LatLonBezier previous;
	private LatLonBezier current;
	private Bezier<Vector2> bezier;

	public InterpolatableLatLon(LatLonBezier current)
	{
		this.current = current;
	}

	public LatLonBezier getCurrent()
	{
		return current;
	}

	public LatLon getEnd()
	{
		return current.latlon;
	}

	public LatLon interpolate(double percent)
	{
		if (bezier == null)
			return current.latlon;
		Vector2 v = bezier.pointAt(percent);
		return LatLon.fromVector2(v);
	}

	public double length()
	{
		if (bezier == null)
			return 0;
		return bezier.getLength();
	}

	public void setPrevious(Interpolatable<LatLonBezier, LatLon> previous)
	{
		this.previous = previous.getCurrent();
		refresh();
	}

	private void refresh()
	{
		Vector2 v1 = previous.latlon.toVector2();
		Vector2 vout = previous.out.toVector2(previous.latlon);

		Vector2 v2 = current.latlon.toVector2(previous.latlon);
		Vector2 vin = current.in.toVector2(v2);

		bezier = new Bezier<Vector2>(v1, vout, vin, v2);
	}
}
