package path;

import path.quaternion.Quaternion;
import path.vector.Vector2;
import path.vector.Vector3;

public class AnimationSection
{
	private final static int NUM_SUBDIVISIONS = 1000;
	
	public AnimationPoint point0;
	public AnimationPoint point1;

	private Bezier<Vector3> bezier;
	private Motion motion;

	private double[] percents = new double[NUM_SUBDIVISIONS];
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

		Vector2 oStart = new Vector2(), oEnd = new Vector2();
		Quaternion qStart = new Quaternion(), qEnd = new Quaternion();
		Vector3 vStart = new Vector3(), vEnd = new Vector3();
		Vector3 pStart = new Vector3(), pEnd = new Vector3();

		for (int i = -1; i < NUM_SUBDIVISIONS; i++)
		{
			double p1 = (i + 1) / (double) NUM_SUBDIVISIONS;

			vEnd = bezier.pointAt(p1);
			oEnd = orientation0.interpolate(orientation1, p1, oEnd);
			qEnd.fromAnglesDegrees(0, oEnd.x, oEnd.y);
			pEnd = qEnd.mult(vEnd, pEnd);

			if (i >= 0)
			{
				length += pStart.subtractLocal(pEnd).distance();
				percents[i] = length;
			}

			vStart.set(vEnd);
			oStart.set(oEnd);
			qStart.set(qEnd);
			pStart.set(pEnd);
		}

		if (length > 0d)
		{
			for (int i = 0; i < NUM_SUBDIVISIONS; i++)
			{
				percents[i] /= length;
			}
		}
		percents[NUM_SUBDIVISIONS - 1] = 1d;

		double ain = point0.accelerationIn;
		double aout = point0.accelerationOut;
		double d = length;
		double v1 = point0.velocityAt;
		double v2 = point0.velocityAfter;
		double v3 = point1.velocityAt;

		motion = new Motion(v1, v2, v3, d, ain, aout);
		length = motion.getTime();
	}

	public Point linearPointAt(double percent)
	{
		if (percent < 0 || percent > 1)
		{
			throw new IllegalArgumentException();
		}

		int i = 0;
		while (i < percents.length - 1 && percents[i] <= percent)
		{
			i++;
		}

		double percentStart = i > 0 ? percents[i - 1] : 0d;
		double percentWindow = percents[i] - percentStart;
		double p = (percent - percentStart) / percentWindow;
		percent = (p + (double) i) / (double) NUM_SUBDIVISIONS;

		//now apply motion to percent
		double time = motion.getTime() * percent;
		percent = motion.getPercent(time);

		Vector3 position = bezier.pointAt(percent);
		Vector2 orientation = point0.orientation.interpolate(
				point1.orientation, percent);
		return new Point(position, orientation);
	}

	public double getLength()
	{
		return length;
	}
}
