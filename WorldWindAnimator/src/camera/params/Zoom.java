package camera.params;

import java.io.Serializable;

public class Zoom implements Serializable
{
	public final double zoom;

	public Zoom(Zoom zoom)
	{
		this(zoom.zoom);
	}

	private Zoom(double zoom)
	{
		this.zoom = zoom;
	}

	public double toCameraZoom()
	{
		return z2c(zoom);
	}

	public static Zoom fromCameraZoom(double camera)
	{
		return new Zoom(c2z(camera));
	}

	private static double c2z(double camera)
	{
		return Math.log(camera + 1);
	}

	private static double z2c(double zoom)
	{
		return Math.pow(Math.E, zoom) - 1;
	}

	public static double difference(Zoom z1, Zoom z2)
	{
		return z2.zoom - z1.zoom;
	}

	public static Zoom interpolate(Zoom z1, Zoom z2, double percent)
	{
		return new Zoom((1 - percent) * z1.zoom + percent * z2.zoom);
	}
}
