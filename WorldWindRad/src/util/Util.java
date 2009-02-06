package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

public class Util
{
	public final static double METER_TO_FEET = 3.280839895;
	public final static double METER_TO_MILE = 0.000621371192;
	
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

	public static LatLon computeLatLonFromString(String coordString)
	{
		return computeLatLonFromString(coordString, null);
	}

	/**
	 * Tries to extract a latitude and a longitude from the given text string.
	 * 
	 * @param coordString
	 *            the input string.
	 * @param globe
	 *            the current <code>Globe</code>.
	 * @return the corresponding <code>LatLon</code> or <code>null</code>.
	 */
	public static LatLon computeLatLonFromString(String coordString, Globe globe)
	{
		if (coordString == null)
		{
			String msg = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Angle lat = null;
		Angle lon = null;
		coordString = coordString.trim();
		String regex;
		String separators = "(\\s*|,|,\\s*)";
		Pattern pattern;
		Matcher matcher;

		// Try MGRS - allow spaces
		if (globe != null)
		{
			regex = "\\d{1,2}[A-Za-z]\\s*[A-Za-z]{2}\\s*\\d{1,5}\\s*\\d{1,5}";
			if (coordString.matches(regex))
			{
				try
				{
					MGRSCoord MGRS = MGRSCoord.fromString(coordString, globe);
					// NOTE: the MGRSCoord does not always report errors with invalide strings,
					// but will have lat and lon set to zero
					if (MGRS.getLatitude().degrees != 0
							|| MGRS.getLatitude().degrees != 0)
					{
						lat = MGRS.getLatitude();
						lon = MGRS.getLongitude();
					}
					else
						return null;
				}
				catch (IllegalArgumentException e)
				{
					return null;
				}
			}
		}

		// Try to extract a pair of signed decimal values separated by a space, ',' or ', '
		// Allow E, W, S, N sufixes
		if (lat == null || lon == null)
		{
			regex = "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[N|n|S|s]??)";
			regex += separators;
			regex += "([-|\\+]?\\d+?(\\.\\d+?)??\\s*[E|e|W|w]??)";
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(coordString);
			if (matcher.matches())
			{
				String sLat = matcher.group(1).trim(); // Latitude
				int signLat = 1;
				char suffix = sLat.toUpperCase().charAt(sLat.length() - 1);
				if (!Character.isDigit(suffix))
				{
					signLat = suffix == 'N' ? 1 : -1;
					sLat = sLat.substring(0, sLat.length() - 1);
					sLat = sLat.trim();
				}

				String sLon = matcher.group(4).trim(); // Longitude
				int signLon = 1;
				suffix = sLon.toUpperCase().charAt(sLon.length() - 1);
				if (!Character.isDigit(suffix))
				{
					signLon = suffix == 'E' ? 1 : -1;
					sLon = sLon.substring(0, sLon.length() - 1);
					sLon = sLon.trim();
				}

				lat = Angle.fromDegrees(Double.parseDouble(sLat) * signLat);
				lon = Angle.fromDegrees(Double.parseDouble(sLon) * signLon);
			}
		}

		// Try to extract two degrees minute seconds blocks separated by a space, ',' or ', '
		// Allow S, N, W, E suffixes and signs.
		// eg: -123° 34' 42" +45° 12' 30"
		// eg: 123° 34' 42"S 45° 12' 30"W
		if (lat == null || lon == null)
		{
			regex = "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[N|n|S|s]?)";
			regex += separators;
			regex += "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?(\\s*\\d{1,2}[\"|\u201d])?\\s*[E|e|W|w]?)";
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(coordString);
			if (matcher.matches())
			{
				lat = parseDMSString(matcher.group(1));
				lon = parseDMSString(matcher.group(5));
			}
		}

		if (lat == null || lon == null)
			return null;

		if (lat.degrees >= -90 && lat.degrees <= 90 && lon.degrees >= -180
				&& lon.degrees <= 180)
			return new LatLon(lat, lon);

		return null;
	}

	/**
	 * Parse a Degrees, Minute, Second coordinate string.
	 * 
	 * @param dmsString
	 *            the string to parse.
	 * @return the corresponding <code>Angle</code> or null.
	 */
	private static Angle parseDMSString(String dmsString)
	{
		// Replace degree, min and sec signs with space
		dmsString = dmsString
				.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ");
		// Replace multiple spaces with single ones
		dmsString = dmsString.replaceAll("\\s+", " ");
		dmsString = dmsString.trim();

		// Check for sign prefix and suffix
		int sign = 1;
		char suffix = dmsString.toUpperCase().charAt(dmsString.length() - 1);
		if (!Character.isDigit(suffix))
		{
			sign = (suffix == 'N' || suffix == 'E') ? 1 : -1;
			dmsString = dmsString.substring(0, dmsString.length() - 1);
			dmsString = dmsString.trim();
		}
		char prefix = dmsString.charAt(0);
		if (!Character.isDigit(prefix))
		{
			sign *= (prefix == '-') ? -1 : 1;
			dmsString = dmsString.substring(1, dmsString.length());
		}

		// Process degrees, minutes and seconds
		String[] DMS = dmsString.split(" ");
		double d = Integer.parseInt(DMS[0]);
		double m = DMS.length > 1 ? Integer.parseInt(DMS[1]) : 0;
		double s = DMS.length > 2 ? Integer.parseInt(DMS[2]) : 0;

		if (m >= 0 && m <= 60 && s >= 0 && s <= 60)
			return Angle
					.fromDegrees(d * sign + m / 60 * sign + s / 3600 * sign);

		return null;
	}
}
