package camera.params;

import gov.nasa.worldwind.geom.Angle;

import java.io.Serializable;

public class Roll implements Serializable
{
	public final double degrees;

	public Roll(Angle angle)
	{
		this(angle.degrees);
	}

	public Roll(Roll heading)
	{
		this(heading.degrees);
	}

	private Roll(double degrees)
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

	public static double difference(Roll h1, Roll h2)
	{
		return new Roll(h2.degrees - h1.degrees).degrees;
	}

	public static Roll fromDegrees(double degrees)
	{
		return new Roll(degrees);
	}

	public static Roll interpolate(Roll h1, Roll h2, double percent)
	{
		double difference = difference(h1, h2);
		return new Roll((1 - percent) * h1.degrees + percent
				* (h1.degrees + difference));
	}
}
