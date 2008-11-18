package path;

import java.io.Serializable;

public class AnimationPoint extends Point implements Serializable,
		Comparable<AnimationPoint>
{
	public double time;
	public Vector3 positionIn;
	public Vector3 positionOut;
	public Vector3 lookAtIn;
	public Vector3 lookAtOut;

	public AnimationPoint(double time, Point point, Vector3 positionIn,
			Vector3 positionOut, Vector3 lookAtIn, Vector3 lookAtOut)
	{
		this(time, new Vector3(point.position), new Vector3(point.lookAt),
				positionIn, positionOut, lookAtIn, lookAtOut);
	}

	public AnimationPoint(AnimationPoint a)
	{
		this(a.time, new Vector3(a.position), new Vector3(a.lookAt),
				new Vector3(a.positionIn), new Vector3(a.positionOut),
				new Vector3(a.lookAtIn), new Vector3(a.lookAtOut));
	}

	public AnimationPoint(double time, Vector3 position, Vector3 lookAt,
			Vector3 positionIn, Vector3 positionOut, Vector3 lookAtIn,
			Vector3 lookAtOut)
	{
		super(position, lookAt);
		this.time = time;
		this.positionIn = positionIn;
		this.positionOut = positionOut;
		this.lookAtIn = lookAtIn;
		this.lookAtOut = lookAtOut;
	}

	public int compareTo(AnimationPoint o)
	{
		return Double.compare(time, o.time);
	}

	public static Point bezierInterpolate(AnimationPoint p1, AnimationPoint p2,
			double percent)
	{
		Vector3 v0 = p1.position;
		Vector3 v3 = p2.position;
		Vector3 v1 = v0.add(p1.positionOut);
		Vector3 v2 = v3.add(p2.positionIn);

		Bezier<Vector3> bezier = new Bezier<Vector3>(v0, v1, v2, v3);
		Vector3 position = bezier.linearPointAt(percent);

		v0 = p1.lookAt;
		v3 = p2.lookAt;
		v1 = v0.add(p1.lookAtOut);
		v2 = v3.add(p2.lookAtIn);

		bezier = new Bezier<Vector3>(v0, v1, v2, v3);
		Vector3 lookAt = bezier.linearPointAt(percent);

		return new Point(position, lookAt);
	}

	public static Point linearInterpolate(AnimationPoint p1, AnimationPoint p2,
			double percent)
	{
		Vector3 position = p1.position.interpolate(p2.position, percent);
		Vector3 lookAt = p1.lookAt.interpolate(p2.lookAt, percent);
		return new Point(position, lookAt);
	}
}
