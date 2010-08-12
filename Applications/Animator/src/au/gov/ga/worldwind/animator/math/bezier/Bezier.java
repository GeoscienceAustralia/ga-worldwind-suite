package au.gov.ga.worldwind.animator.math.bezier;

import java.io.Serializable;

import au.gov.ga.worldwind.animator.math.vector.Vector;


public class Bezier<V extends Vector<V>> implements Serializable
{
	private final static int NUM_SUBDIVISIONS = 1000;

	public final V begin;
	public final V out;
	public final V in;
	public final V end;

	private double[] percents = new double[NUM_SUBDIVISIONS];
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

		V begin = this.begin.clone(), end;
		for (int i = 0; i < NUM_SUBDIVISIONS; i++)
		{
			double t = (i + 1) / (double) NUM_SUBDIVISIONS;
			end = bezierPointAt(t);
			length += begin.subtractLocal(end).distance();
			percents[i] = length;
			begin = end;
		}

		if (length > 0d)
		{
			for (int i = 0; i < NUM_SUBDIVISIONS; i++)
			{
				percents[i] /= length;
			}
		}
		percents[NUM_SUBDIVISIONS - 1] = 1d;
	}

	public double getLength()
	{
		return length;
	}

	public V pointAt(double percent)
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

		return bezierPointAt(percent);
	}

	private V bezierPointAt(double t)
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
