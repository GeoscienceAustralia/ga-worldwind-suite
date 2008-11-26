package camera.params;

import gov.nasa.worldwind.geom.Angle;

import java.io.Serializable;

public class Heading implements Serializable
{
	public final double degrees;

	public Heading(Angle angle)
	{
		this(angle.degrees);
	}

	public Heading(Heading heading)
	{
		this(heading.degrees);
	}

	private Heading(double degrees)
	{
		//degrees = fixDegrees(degrees);
		this.degrees = degrees;
	}

	public Angle getAngle()
	{
		return Angle.fromDegrees(fixDegrees(degrees));
	}

	private static double fixDegrees(double degrees)
	{
		while (degrees > 180d)
		{
			degrees -= 360d;
		}
		while (degrees <= -180d)
		{
			degrees += 360d;
		}
		return degrees;
	}

	public static double difference(Heading h1, Heading h2)
	{
		return new Heading(h2.degrees - h1.degrees).degrees;
	}

	public static Heading fromDegrees(double degrees)
	{
		return new Heading(degrees);
	}

	public static Heading interpolate(Heading h1, Heading h2, double percent)
	{
		double difference = difference(h1, h2);
		return new Heading((1 - percent) * h1.degrees + percent
				* (h1.degrees + difference));
	}
}
