package camera.params;

import java.io.Serializable;

import gov.nasa.worldwind.geom.Angle;

public class Latitude implements Serializable
{
	public final double degrees;

	public Latitude(Angle angle)
	{
		this(angle.degrees);
	}

	public Latitude(Latitude latitude)
	{
		this(latitude.degrees);
	}

	private Latitude(double degrees)
	{
		while (degrees > 90d)
		{
			degrees -= 180d;
		}
		while (degrees <= -90d)
		{
			degrees += 180d;
		}
		this.degrees = degrees;
	}

	public Angle getAngle()
	{
		return Angle.fromDegreesLatitude(degrees);
	}

	public static double difference(Latitude l1, Latitude l2)
	{
		return l2.degrees - l1.degrees;
	}

	public static Latitude fromDegrees(double degrees)
	{
		return new Latitude(degrees);
	}
}
