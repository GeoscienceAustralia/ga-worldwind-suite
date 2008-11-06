package path;

import java.io.Serializable;

public class AnimationPoint extends Point implements Serializable,
		Comparable<AnimationPoint>
{
	public double time;
	public Vector in;
	public Vector out;

	public AnimationPoint(double time, Point point, Vector in, Vector out)
	{
		this(time, new Position(point.position), point.heading, point.pitch,
				in, out);
	}

	public AnimationPoint(AnimationPoint animationPoint)
	{
		this(animationPoint.time, new Position(animationPoint.position),
				animationPoint.heading, animationPoint.pitch, new Vector(
						animationPoint.in), new Vector(animationPoint.out));
	}

	public AnimationPoint(double time, Position position, double heading,
			double pitch, Vector in, Vector out)
	{
		super(position, heading, pitch);
		this.time = time;
		this.in = in;
		this.out = out;
	}

	public int compareTo(AnimationPoint o)
	{
		return Double.compare(time, o.time);
	}

	public static Point bezierInterpolate(AnimationPoint p1, AnimationPoint p2,
			double percent)
	{
		double longdiff = Longitude.difference(p1.position.longitude,
				p2.position.longitude);
		double latdiff = Latitude.difference(p1.position.latitude,
				p2.position.latitude);

		double t1 = percent;
		double t2 = t1 * t1;
		double t3 = t2 * t1;
		Vector v0 = new Vector(p1.position.longitude.degrees,
				p1.position.latitude.degrees, p1.position.elevation);
		Vector v3 = new Vector(v0.x + longdiff, v0.y + latdiff,
				p2.position.elevation);
		Vector v1 = v0.add(p1.out);
		Vector v2 = v3.add(p2.in);

		double cx = 3 * (v1.x - v0.x);
		double cy = 3 * (v1.y - v0.y);
		double cz = 3 * (v1.z - v0.z);
		double bx = 3 * (v2.x - v1.x) - cx;
		double by = 3 * (v2.y - v1.y) - cy;
		double bz = 3 * (v2.z - v1.z) - cz;
		double ax = v3.x - v0.x - cx - bx;
		double ay = v3.y - v0.y - cy - by;
		double az = v3.z - v0.z - cz - bz;

		double x = ax * t3 + bx * t2 + cx * t1 + v0.x;
		double y = ay * t3 + by * t2 + cy * t1 + v0.y;
		double z = az * t3 + bz * t2 + cz * t1 + v0.z;

		double heading = interpolate(p1.heading, p2.heading, percent);
		double pitch = interpolate(p1.pitch, p2.pitch, percent);

		return new Point(new Position(x, y, z), heading, pitch);
	}

	public static Point linearInterpolate(AnimationPoint p1, AnimationPoint p2,
			double percent)
	{
		double longdiff = Longitude.difference(p1.position.longitude,
				p2.position.longitude);
		double latdiff = Latitude.difference(p1.position.latitude,
				p2.position.latitude);

		double x1 = p1.position.longitude.degrees;
		double y1 = p1.position.latitude.degrees;

		double x = interpolate(x1, x1 + longdiff, percent);
		double y = interpolate(y1, y1 + latdiff, percent);
		double z = interpolate(p1.position.elevation, p2.position.elevation,
				percent);

		double heading = interpolate(p1.heading, p2.heading, percent);
		double pitch = interpolate(p1.pitch, p2.pitch, percent);

		return new Point(new Position(x, y, z), heading, pitch);
	}

	public static double interpolate(double d1, double d2, double percent)
	{
		return d1 * (1 - percent) + d2 * percent;
	}
}
