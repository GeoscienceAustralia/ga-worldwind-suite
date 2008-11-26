package camera.params;

import gov.nasa.worldwind.geom.Angle;

import java.io.Serializable;

public class Longitude implements Serializable
{
	public final double degrees;

	public Longitude(Angle angle)
	{
		this(angle.degrees);
	}

	public Longitude(Longitude longitude)
	{
		this(longitude.degrees);
	}

	private Longitude(double degrees)
	{
		while (degrees > 180d)
		{
			degrees -= 360d;
		}
		while (degrees <= -180d)
		{
			degrees += 360d;
		}
		this.degrees = degrees;
	}

	public Angle getAngle()
	{
		return Angle.fromDegreesLongitude(degrees);
	}

	public static double difference(Longitude l1, Longitude l2)
	{
		return new Longitude(l2.degrees - l1.degrees).degrees;
	}

	public static Longitude fromDegrees(double degrees)
	{
		return new Longitude(degrees);
	}
}
