package path;

import path.vector.Vector;

public class Bezier<V extends Vector<V>>
{
	private final static int NUM_SUBDIVISIONS = 1000;

	public final V begin;
	public final V out;
	public final V in;
	public final V end;

	private double[] lengths = new double[NUM_SUBDIVISIONS];
	private double length;

	public Bezier(V begin, V out, V in, V end)
	{
		this.begin = begin;
		this.end = end;
		this.out = out;
		this.in = in;
		subdivide();
	}

	private void subdivide()
	{
		length = 0d;

		V begin = this.begin, end = null;
		for (int i = 0; i < NUM_SUBDIVISIONS; i++)
		{
			double t = (i + 1) / (double) NUM_SUBDIVISIONS;
			end = pointAt(t);
			length += begin.subtract(end).distance();
			lengths[i] = length;
			begin = end;
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

		double t = (percent - length0) / (length1 - length0);
		t = (t + (double) i) / (double) NUM_SUBDIVISIONS;

		return pointAt(t);
	}

	private V pointAt(double t)
	{
		double t2 = t * t;
		V c = out.subtract(begin).multLocal(3d);
		V b = in.subtract(out).multLocal(3d).subtractLocal(c);
		V a = end.subtract(begin).subtractLocal(c).subtractLocal(b);
		a.multLocal(t2 * t);
		b.multLocal(t2);
		c.multLocal(t);
		a.addLocal(b).addLocal(c).addLocal(begin);
		return a;
	}

	@SuppressWarnings("unused")
	private V deCasteljau(double t)
	{
		V v1 = this.out.subtract(this.begin).multLocal(t).addLocal(this.begin);
		V v2 = this.in.subtract(this.out).multLocal(t).addLocal(this.out);
		V v3 = this.end.subtract(this.in).multLocal(t).addLocal(this.in);

		v3 = v3.subtractLocal(v2).multLocal(t).addLocal(v2);
		v2 = v2.subtractLocal(v1).multLocal(t).addLocal(v1);

		return v3.subtractLocal(v2).multLocal(t).addLocal(v2);
	}
}
