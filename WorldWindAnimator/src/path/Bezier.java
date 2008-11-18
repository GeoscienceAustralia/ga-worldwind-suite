package path;

public class Bezier<V extends Vector<V>>
{
	private final static int NUM_SUBDIVISIONS = 1000;

	public final V v0;
	public final V v1;
	public final V v2;
	public final V v3;

	private double[] lengths = new double[NUM_SUBDIVISIONS];
	private double length;

	public Bezier(V v0, V v1, V v2, V v3)
	{
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		subdivide();
	}

	private void subdivide()
	{
		length = 0d;

		V v0 = this.v0, v1 = null;
		for (int i = 0; i < NUM_SUBDIVISIONS; i++)
		{
			double t = (i + 1) / (double) NUM_SUBDIVISIONS;
			v1 = pointAt(t);
			length += v0.subtract(v1).distance();
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

	public double getLength()
	{
		return length;
	}

	public V linearPointAt(double percent)
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

	private V pointAt(double t)
	{
		double t2 = t * t;
		V c = v1.subtract(v0).multLocal(3d);
		V b = v2.subtract(v1).multLocal(3d).subtractLocal(c);
		V a = v3.subtract(v0).subtractLocal(c).subtractLocal(b);
		a.multLocal(t2 * t);
		b.multLocal(t2);
		c.multLocal(t);
		a.addLocal(b).addLocal(c).addLocal(v0);
		return a;
	}

	@SuppressWarnings("unused")
	private V deCasteljau(double t)
	{
		V v1 = this.v1.subtract(this.v0).multLocal(t).addLocal(this.v0);
		V v2 = this.v2.subtract(this.v1).multLocal(t).addLocal(this.v1);
		V v3 = this.v3.subtract(this.v2).multLocal(t).addLocal(this.v2);

		v3 = v3.subtractLocal(v2).multLocal(t).addLocal(v2);
		v2 = v2.subtractLocal(v1).multLocal(t).addLocal(v1);

		return v3.subtractLocal(v2).multLocal(t).addLocal(v2);
	}
}
