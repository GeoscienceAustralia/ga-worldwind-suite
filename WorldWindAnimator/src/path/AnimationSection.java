package path;

import path.quaternion.Quaternion;
import path.vector.Vector2;
import path.vector.Vector3;

public class AnimationSection
{
	public AnimationPoint point0;
	public AnimationPoint point1;

	public Bezier<Vector3> bezier;
	public Motion motion;

	private final static int NUM_SUBDIVISIONS = 1000;
	private double[] lengths = new double[NUM_SUBDIVISIONS];
	private double length;

	public AnimationSection(AnimationPoint point0, AnimationPoint point1)
	{
		this.point0 = point0;
		this.point1 = point1;
		refresh();
	}

	public void refresh()
	{
		length = 0;

		bezier = new Bezier<Vector3>(point0.position, point0.position
				.add(point0.out), point1.position.add(point1.in),
				point1.position);

		Vector2 orientation0 = point0.orientation;
		Vector2 orientation1 = point1.orientation;
		Quaternion q0 = new Quaternion();
		Quaternion q1 = new Quaternion();
		Vector2 o0 = new Vector2();
		Vector2 o1 = new Vector2();

		for (int i = 0; i < NUM_SUBDIVISIONS; i++)
		{
			double p0 = i / (double) NUM_SUBDIVISIONS;
			double p1 = (i + 1) / (double) NUM_SUBDIVISIONS;

			Vector3 v0 = bezier.linearPointAt(p0);
			Vector3 v1 = bezier.linearPointAt(p1);

			o0 = orientation0.interpolate(orientation1, p0, o0);
			o1 = orientation0.interpolate(orientation1, p1, o1);

			q0.fromAngles(0, o0.x, o0.y);
			q1.fromAngles(0, o1.x, o1.y);

			v0 = q0.multLocal(v0);
			v1 = q1.multLocal(v1);

			length += v0.subtractLocal(v1).distance();
			lengths[i] = length;
		}

		if (length > 0d)
		{
			for (int i = 0; i < NUM_SUBDIVISIONS; i++)
			{
				lengths[i] /= length;
			}
		}
		lengths[NUM_SUBDIVISIONS - 1] = 1d;

		double a = point0.accelerationAfter;
		double d = length;
		double v1 = point0.velocityAt;
		double v2 = point0.velocityAfter;
		double v3 = point1.velocityAt;

		motion = new Motion(a, v1, v2, v3, d);
	}

	public Point linearPointAt(double percent)
	{
		if (percent < 0 || percent > 1)
		{
			throw new IllegalArgumentException();
		}
		/*int i = 0;
		while (i < lengths.length - 1 && lengths[i] <= percent)
		{
			i++;
		}

		double length0 = 0d;
		double length1 = lengths[i];
		if (i > 0)
		{
			length0 = lengths[i - 1];
		}

		percent = (percent - length0) / (length1 - length0);
		percent = (percent + (double) i) / (double) NUM_SUBDIVISIONS;*/

		//now apply motion to percent
		double time = motion.getTime() * percent;
		percent = motion.getPercent(time);

		return pointAt(percent);
	}

	private Point pointAt(double percent)
	{
		Vector3 position = bezier.linearPointAt(percent);
		Vector2 orientation = point0.orientation.interpolate(
				point1.orientation, percent);
		return new Point(position, orientation);
	}

	public double getLength()
	{
		return length;
	}
}
