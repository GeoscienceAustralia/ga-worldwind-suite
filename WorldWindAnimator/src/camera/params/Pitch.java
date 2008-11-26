package camera.params;

import gov.nasa.worldwind.geom.Angle;

import java.io.Serializable;

public class Pitch implements Serializable
{
	public final double degrees;

	public Pitch(Angle angle)
	{
		this(angle.degrees);
	}

	public Pitch(Pitch pitch)
	{
		this(pitch.degrees);
	}

	private Pitch(double degrees)
	{
		this.degrees = Math.max(0, Math.min(90, degrees));
	}

	public Angle getAngle()
	{
		return Angle.fromDegrees(degrees);
	}

	public static double difference(Pitch p1, Pitch p2)
	{
		return p2.degrees - p1.degrees;
	}

	public static Pitch fromDegrees(double degrees)
	{
		return new Pitch(degrees);
	}

	public static Pitch interpolate(Pitch p1, Pitch p2, double percent)
	{
		return new Pitch((1 - percent) * p1.degrees + percent * p2.degrees);
	}
}
