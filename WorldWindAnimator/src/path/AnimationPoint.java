package path;

import java.io.Serializable;

import camera.vector.Vector2;
import camera.vector.Vector3;


public class AnimationPoint extends Point implements Serializable
{
	public Vector3 in;
	public Vector3 out;
	public double velocityAt;
	public double velocityAfter;
	public double accelerationIn;
	public double accelerationOut;

	public AnimationPoint(Point point, Vector3 in, Vector3 out)
	{
		this(new Vector3(point.position), new Vector2(point.orientation), in,
				out);
	}

	public AnimationPoint(AnimationPoint a)
	{
		this(new Vector3(a.position), new Vector2(a.orientation), new Vector3(
				a.in), new Vector3(a.out));
	}

	public AnimationPoint(Vector3 position, Vector2 orientation, Vector3 in,
			Vector3 out)
	{
		super(position, orientation);
		this.in = in;
		this.out = out;
	}
}
