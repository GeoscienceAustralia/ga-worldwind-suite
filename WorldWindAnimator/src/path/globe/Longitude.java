package path.globe;

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

	public Longitude(double degrees)
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


	/**
	 * @param l1
	 * @param l2
	 * @return value to add to l1 to get to l2 (ie l2 - l1)
	 */
	public static double difference(Longitude l1, Longitude l2)
	{
		return new Longitude(l2.degrees - l1.degrees).degrees;
	}
}
