package util;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.Logging;

public class Util
{
	public static long getScaledLengthMillis(LatLon beginLatLon,
			LatLon endLatLon, long minLengthMillis, long maxLengthMillis)
	{
		Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon,
				endLatLon);
		double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	public static long getScaledLengthMillis(double beginZoom, double endZoom,
			long minLengthMillis, long maxLengthMillis)
	{
		double scaleFactor = Math.abs(endZoom - beginZoom)
				/ Math.max(endZoom, beginZoom);
		scaleFactor = clampDouble(scaleFactor, 0.0, 1.0);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	private static double mixDouble(double amount, double value1, double value2)
	{
		if (amount < 0)
			return value1;
		else if (amount > 1)
			return value2;
		return value1 * (1.0 - amount) + value2 * amount;
	}

	private static double angularRatio(Angle x, Angle y)
	{
		if (x == null || y == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		double unclampedRatio = x.divide(y);
		return clampDouble(unclampedRatio, 0, 1);
	}

	private static double clampDouble(double value, double min, double max)
	{
		return value < min ? min : (value > max ? max : value);
	}
}
