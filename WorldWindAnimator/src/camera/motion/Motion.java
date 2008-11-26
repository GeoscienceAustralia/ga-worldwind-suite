package camera.motion;

import java.io.Serializable;

public class Motion implements Serializable, Cloneable
{
	public static final double DBL_EPSILON = 2.220446049250313E-16d;
	public static final double SMALL_DOUBLE = 1E-8;

	private MotionParams params;

	private double time;
	private double distance;

	private double v1;
	private double v2;
	private double v3;
	private double a1;
	private double a2;
	private double a3;
	private double t1;
	private double t2;
	private double t3;
	private double d1;
	private double d2;
	private double d3;
	private double d3Calc;

	public Motion(MotionParams params)
	{
		this.params = params;
	}

	public void setTimeAndDistance(double time, double distance)
	{
		this.time = time;
		this.distance = distance;
		refresh();
	}

	public double getTime()
	{
		return time;
	}

	public double getDistance()
	{
		return distance;
	}

	public double getPercent(double time)
	{
		if (time <= 0)
			return 0;
		if (time >= getTime())
			return 1;
		if (getDistance() <= 0)
			return 1;

		double a;
		double d;
		double u;
		double t;

		if (time < t1)
		{
			t = time;
			a = a1;
			d = 0;
			u = v1;
		}
		else if (time < t1 + t2)
		{
			t = time - t1;
			a = a2;
			d = d1;
			u = v2;
		}
		else
		{
			t = time - t1 - t2;
			a = a3;
			d = d1 + d2;
			u = v2;
		}

		d += u * t + 0.5 * a * t * t;

		/*double v = u + a * t;
		System.out.println("Distance = " + d + ", Velocity = " + v);*/

		return Math.max(0, Math.min(1, d / getDistance()));
	}

	private void refresh()
	{
		a1 = params.accelerationIn;
		a3 = params.accelerationOut;
		v1 = params.velocityIn;
		v3 = params.velocityOut;

		calculate();
		if (!isValid())
		{
			a1 = -a1;
			calculate();
			if (!isValid())
			{
				a3 = -a3;
				calculate();
				if (!isValid())
				{
					a1 = -a1;
					calculate();
					if (!isValid())
					{
						throw new IllegalArgumentException();
					}
				}
			}
		}

		/*System.out.println();
		System.out.println("Calculations");
		System.out.println("Times: " + t1 + ", " + t2 + ", " + t3);
		System.out.println("Velocities: " + v1 + ", " + v2 + ", " + v3);
		System.out.println("Distances: " + d1 + ", " + d2 + ", " + d3 + " ("
				+ d3Calc + ")");
		System.out.println("Accelerations: " + a1 + ", " + a3);*/
	}

	private void calculate()
	{
		double a = a1 - a3;
		double b = 2 * (a1 * a3 * time + a3 * v1 - a1 * v3);
		double c = a1 * v3 * v3 - a3 * v1 * v1 - 2 * a1 * a3 * distance;

		calculate(a, b, c, false);
		if (!isValid())
		{
			calculate(a, b, c, true);
		}
	}

	private void calculate(double a, double b, double c, boolean useNegative)
	{
		double discriminant = b * b - 4 * a * c;
		v2 = (-b + Math.sqrt(discriminant) * (useNegative ? -1 : 1)) / (2 * a);

		calculateTimesAndDistances();

		//here, if the values are invalid but t1 and t2 and t3 are all greater than 0,
		//then do we know it is possible to get to the location and velocity in a shorter time?

		/*if (!isValid() && allowTimeModification && t1 >= 0 && t2 >= 0
				&& t3 >= 0)
		{
			time = t1 + t3;
			double d = d1 + d3;
			v2 = Math.sqrt((4 * a1 * a3 * d - 2 * a1 * v3 * v3 + 2 * a3 * v1
					* v1)
					/ (2 * (a3 - a1)));

			calculateTimesAndDistances();
		}*/
	}

	private void calculateTimesAndDistances()
	{
		a2 = 0;
		t1 = (v2 - v1) / a1;
		t3 = (v3 - v1 - a1 * t1) / a3;
		t2 = time - t3 - t1;
		d1 = v1 * t1 + 0.5 * a1 * t1 * t1;
		d2 = v2 * t2;
		d3 = distance - d1 - d2;
		d3Calc = v2 * t3 + 0.5 * a3 * t3 * t3;
	}

	private boolean isValid()
	{
		return t1 >= 0 && t2 >= 0 && t3 >= 0 && d1 >= 0 && d2 >= 0 && d3 >= 0
				&& v1 >= 0 && v2 >= 0 && v3 >= 0
				&& Math.abs(d3 - d3Calc) < SMALL_DOUBLE;
	}
}
