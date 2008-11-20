package path;

public class Motion
{
	//Useful motion equations:
	//v = u + at
	//v^2 - u^2 = 2ad
	//d = (v^2 - u^2) / 2a
	//d = ut + 0.5at^2
	//t = (v - u) / a
	//if a = 0, t = d / u

	public static final double DBL_EPSILON = 2.220446049250313E-16d; //TODO move somewhere

	public double v1;
	public double v2;
	public double v3;
	public double a1;
	public double a2;
	public double a3;
	public double t1;
	public double t2;
	public double t3;
	public double d1;
	public double d2;
	public double d3;

	public Motion(double a, double v1, double v2, double v3, double d)
	{
		if (a <= 0)
			throw new RuntimeException("Acceleration must be greater than 0");

		if (v1 < 0 || v2 < 0 || v3 < 0)
			throw new RuntimeException("Velocities must not be less than 0");

		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;

		calculate(d, a);

		if (d2 < 0)
		{
			//will only work if a1 is positive and a3 is negative or vise versa
			if (Math.signum(a1) != Math.signum(a3))
			{
				fixV2(d, a);
				calculate(d, a);
			}
			else
			{
				throw new RuntimeException(
						"Cannot reach target velocities with current acceleration");
			}
		}
	}


	private void calculate(double d, double a)
	{
		//acceleration/deceleration
		a1 = (v2 > v1) ? a : -a;
		a2 = 0;
		a3 = (v3 > v2) ? a : -a;

		//in,out time
		t1 = (v2 - v1) / a1;
		t3 = (v3 - v2) / a3;

		//in,out distance
		d1 = v1 * t1 + 0.5 * a1 * t1 * t1;
		d3 = v2 * t3 + 0.5 * a3 * t3 * t3;

		//between
		d2 = d - d1 - d3;

		if (v2 == 0)
		{
			if (Math.abs(d2) < DBL_EPSILON)
			{
				t2 = 0;
			}
			else
			{
				throw new RuntimeException(
						"Between velocity must be greater than 0");
			}
		}
		else
		{
			t2 = d2 / v2;
		}
	}

	private void fixV2(double d, double a)
	{
		//find a new v2 such that d2 = 0
		v2 = Math.sqrt((4 * a1 * a3 * d - 2 * a1 * v3 * v3 + 2 * a3 * v1 * v1)
				/ (2 * (a3 - a1)));
	}

	public double getTime()
	{
		return t1 + t2 + t3;
	}

	public double getPercent(double time)
	{
		if (time < t1)
		{
			double t = time;
			double d = v1 * t + 0.5 * a1 * t * t;
			return d / (d1 + d2 + d3);
		}
		else if (time < t1 + t2)
		{
			double t = time - t1;
			double d = v2 * t/* + 0.5 * a2 * t * t*/; //a2 is 0
			d += d1;
			return d / (d1 + d2 + d3);
		}
		else if (time < t1 + t2 + t3)
		{
			double t = time - t1 - t2;
			double d = v2 * t + 0.5 * a3 * t * t;
			d += d1 + d2;
			return d / (d1 + d2 + d3);
		}
		return 1;
	}
}
