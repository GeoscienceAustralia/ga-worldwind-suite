package path;

public class Bezier
{
	private final static int NUM_SUBDIVISIONS = 1000;

	public final Vector v0;
	public final Vector v1;
	public final Vector v2;
	public final Vector v3;

	private double[] lengths = new double[NUM_SUBDIVISIONS];

	public Bezier(Vector v0, Vector v1, Vector v2, Vector v3)
	{
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		subdivide();
	}

	private void subdivide()
	{
		double length = 0d;

		Vector v0 = this.v0, v1 = null;
		for (int i = 0; i < NUM_SUBDIVISIONS; i++)
		{
			double t = (i + 1) / (double) NUM_SUBDIVISIONS;
			v1 = pointAt(t);
			length += v0.distance(v1);
			lengths[i] = length;
			v0 = v1;
		}

		if (length > 0d)
		{
			for (int i = 0; i < NUM_SUBDIVISIONS; i++)
			{
				lengths[i] /= length;
			}
		}
		lengths[NUM_SUBDIVISIONS - 1] = 1d;
	}

	public Vector linearPointAt(double percent)
	{
		if (percent < 0 || percent > 1)
		{
			throw new IllegalArgumentException();
		}
		int i = 0;
		while (lengths[i] <= percent)
		{
			i++;
		}

		double length0 = 0d;
		double length1 = lengths[i];
		if (i > 0)
		{
			length0 = lengths[i - 1];
		}

		double t = (percent - length0) / (length1 - length0);
		t = (t + (double) i) / (double) NUM_SUBDIVISIONS;

		return pointAt(t);
	}

	private Vector pointAt(double t)
	{
		double t2 = t * t;
		double t3 = t2 * t;

		double cx = 3 * (v1.x - v0.x);
		double cy = 3 * (v1.y - v0.y);
		double cz = 3 * (v1.z - v0.z);
		double bx = 3 * (v2.x - v1.x) - cx;
		double by = 3 * (v2.y - v1.y) - cy;
		double bz = 3 * (v2.z - v1.z) - cz;
		double ax = v3.x - v0.x - cx - bx;
		double ay = v3.y - v0.y - cy - by;
		double az = v3.z - v0.z - cz - bz;

		double x = ax * t3 + bx * t2 + cx * t + v0.x;
		double y = ay * t3 + by * t2 + cy * t + v0.y;
		double z = az * t3 + bz * t2 + cz * t + v0.z;

		return new Vector(x, y, z);
	}

	private Vector deCasteljau(double t)
	{
		Vector v1 = this.v1.subtract(this.v0).multLocal(t).addLocal(this.v0);
		Vector v2 = this.v2.subtract(this.v1).multLocal(t).addLocal(this.v1);
		Vector v3 = this.v3.subtract(this.v2).multLocal(t).addLocal(this.v2);

		v3 = v3.subtractLocal(v2).multLocal(t).addLocal(v2);
		v2 = v2.subtractLocal(v1).multLocal(t).addLocal(v1);

		return v3.subtractLocal(v2).multLocal(t).addLocal(v2);
	}
}
