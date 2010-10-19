package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoordConverterAccessible;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class Util
{
	public final static double METER_TO_FEET = 3.280839895;
	public final static double METER_TO_MILE = 0.000621371192;

	public final static String UTM_COORDINATE_REGEX =
			"(?:[a-zA-Z]*\\s*)(\\d+)(?:\\s*)([a-zA-Z])(?:\\s+)((?:\\d*\\.?\\d+)|(?:\\d+))(?:[E|e]?)(?:\\s+)((?:\\d*\\.?\\d+)|(?:\\d+))(?:[N|n]?)";

	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}

	public static File urlToFile(URL url)
	{
		if ("file".equalsIgnoreCase(url.getProtocol()))
		{
			try
			{
				return new File(url.toURI());
			}
			catch (Exception e1)
			{
				try
				{
					return new File(url.getPath());
				}
				catch (Exception e2)
				{
				}
			}
		}
		return null;
	}

	/**
	 * Create a URL pointing to a tile file on the local file system (or inside
	 * a zip file). Returns null if no file for the tile was found.
	 * 
	 * @param tile
	 *            Tile to search for a file for
	 * @param context
	 *            Tile's layer's context URL
	 * @param format
	 *            Tile's layer's default format
	 * @param defaultExt
	 *            If the format is not given, search using this file extension
	 * @return URL pointing to tile's file, or null if not found
	 */
	public static URL getLocalTileURL(Tile tile, URL context, String format, String defaultExt)
	{
		String service = tile.getLevel().getService();
		String dataset = tile.getLevel().getDataset();

		if (dataset == null || dataset.length() <= 0)
			dataset = service;
		else if (service != null && service.length() > 0)
			dataset = service + "/" + dataset;

		if (dataset == null)
			dataset = "";

		boolean isZip = false;
		File parent = Util.getPathWithinContext(dataset, context);
		if (parent == null)
		{
			//if the directory didn't exist, try a zip file
			isZip = true;
			parent = Util.getPathWithinContext(dataset + ".zip", context);
		}

		if (parent == null)
			return null;

		//default to JPG
		String ext = defaultExt;
		if (format != null)
		{
			format = format.toLowerCase();
			if (format.contains("jpg") || format.contains("jpeg"))
				ext = "jpg";
			else if (format.contains("png"))
				ext = "png";
			else if (format.contains("zip"))
				ext = "zip";
			else if (format.contains("dds"))
				ext = "dds";
			else if (format.contains("bmp"))
				ext = "bmp";
			else if (format.contains("gif"))
				ext = "gif";
			//for elevation models:
			else if (format.contains("bil"))
				ext = "bil";
			else if (format.contains("zip"))
				ext = "zip";
		}

		String filename =
				tile.getLevelNumber() + File.separator + Util.paddedInt(tile.getRow(), 4)
						+ File.separator + Util.paddedInt(tile.getRow(), 4) + "_"
						+ Util.paddedInt(tile.getColumn(), 4) + "." + ext;

		try
		{
			if (parent.isFile() && isZip)
			{
				//zip file; return URL using 'jar' protocol
				String entry1 = filename;
				//if file is not found, attempt to find a file with the defaultExt in the zip as well
				String entry2 =
						ext.equals(defaultExt) ? null : filename.substring(0, filename.length()
								- ext.length())
								+ defaultExt;

				URL url =
						entry2 != null ? Util.zipEntryUrl(parent, entry1, entry2) : Util
								.zipEntryUrl(parent, entry1);
				return url;
			}
			else if (parent.isDirectory())
			{
				//return standard 'file' protocol URL
				File file = new File(parent, filename);
				if (file.exists())
				{
					return file.toURI().toURL();
				}
			}
		}
		catch (MalformedURLException e)
		{
			String msg = "Converting tile file to URL failed";
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
		}
		return null;
	}

	/**
	 * Attempt to find a directory or file, relative to a given context URL
	 * 
	 * @param path
	 * @param context
	 * @return
	 */
	private static File getPathWithinContext(String path, URL context)
	{
		//first attempt finding of the directory using a URL
		try
		{
			URL url = context == null ? new URL(path) : new URL(context, path);
			File file = Util.urlToFile(url);
			if (file != null && file.exists())
				return file;
		}
		catch (Exception e)
		{
		}

		//next try parsing the context to pull out a parent file
		File parent = null;
		if (context != null)
		{
			File file = Util.urlToFile(context);
			if (file != null && file.isFile())
			{
				parent = file.getParentFile();
				if (parent != null && !parent.isDirectory())
					parent = null;
			}
		}

		//if the parent isn't null, try using it as a parent file
		if (parent != null)
		{
			try
			{
				File dir = new File(parent, path);
				if (dir.exists())
					return dir;
			}
			catch (Exception e)
			{
			}
		}

		//otherwise ignore the parent and just attempt the path
		File dir = new File(path);
		if (dir.exists())
			return dir;
		return null;
	}

	/**
	 * Return a URL which points to an entry within a zip file (or
	 * <code>null</code> if none of the entries exist).
	 * 
	 * @param zipFile
	 * @param entries
	 *            Filenames within the zip file, returning the first entry found
	 *            (must be relative with no leading slash)
	 * @return URL pointing to entry within zipFile
	 * @throws MalformedURLException
	 */
	private static URL zipEntryUrl(File zipFile, String... entries) throws MalformedURLException
	{
		ZipFile zip = null;
		try
		{
			zip = new ZipFile(zipFile);
			for (String entry : entries)
			{
				entry = entry.replaceAll("\\\\", "/");
				if (zip.getEntry(entry) != null)
				{
					URL zipFileUrl = zipFile.toURI().toURL();
					return new URL("jar:" + zipFileUrl.toExternalForm() + "!/" + entry);
				}
			}
		}
		catch (MalformedURLException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			//ignore
		}
		finally
		{
			if (zip != null)
			{
				try
				{
					zip.close();
				}
				catch (IOException e)
				{
					//ignore
				}
			}
		}
		return null;
	}

	public static String randomString(int length)
	{
		String chars = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	public static long getScaledLengthMillis(double scale, LatLon beginLatLon, LatLon endLatLon)
	{
		return getScaledLengthMillis(beginLatLon, endLatLon, (long) (4000 / scale),
				(long) (20000 / scale));
	}

	public static long getScaledLengthMillis(LatLon beginLatLon, LatLon endLatLon,
			long minLengthMillis, long maxLengthMillis)
	{
		Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
		double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	public static long getScaledLengthMillis(double beginZoom, double endZoom,
			long minLengthMillis, long maxLengthMillis)
	{
		double scaleFactor = Math.abs(endZoom - beginZoom) / Math.max(endZoom, beginZoom);
		scaleFactor = clampDouble(scaleFactor, 0.0, 1.0);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	public static double mixDouble(double amount, double value1, double value2)
	{
		if (amount < 0)
			return value1;
		else if (amount > 1)
			return value2;
		return value1 * (1.0 - amount) + value2 * amount;
	}
	
	public static double percentDouble(double value, double min, double max)
	{
		if(value < min)
			return 0;
		if(value > max)
			return 1;
		return (value - min) / (max - min);
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
					if (MGRS.getLatitude().degrees != 0 || MGRS.getLatitude().degrees != 0)
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
			regex =
					"([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}[m|M|'|\u2019|\\s])?(\\s*\\d{1,2}[s|S|\"|\u201d])?\\s*[N|n|S|s]?)";
			regex += separators;
			regex +=
					"([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}[m|M|'|\u2019|\\s])?(\\s*\\d{1,2}[s|S|\"|\u201d])?\\s*[E|e|W|w]?)";
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

		if (lat.degrees >= -90 && lat.degrees <= 90 && lon.degrees >= -180 && lon.degrees <= 180)
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
		dmsString = dmsString.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ");
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
			return Angle.fromDegrees(d * sign + m / 60 * sign + s / 3600 * sign);

		return null;
	}

	/**
	 * Parse and convert a UTM string to a LatLon point.
	 * 
	 * @param coordString
	 * @param globe
	 * @param charRepresentsHemisphere
	 *            Does the character after the UTM zone represent the hemisphere
	 *            or the latitude band?
	 * @return Point represented by UTM string
	 */
	public static LatLon computeLatLonFromUTMString(String coordString, Globe globe,
			boolean charRepresentsHemisphere)
	{
		coordString = coordString.trim();
		Pattern pattern = Pattern.compile(UTM_COORDINATE_REGEX);
		Matcher matcher = pattern.matcher(coordString);
		if (matcher.matches())
		{
			long zone = Long.parseLong(matcher.group(1));
			char latitudeBand = matcher.group(2).toUpperCase().charAt(0);
			double easting = Double.parseDouble(matcher.group(3));
			double northing = Double.parseDouble(matcher.group(4));

			//if charRepresentsHemisphere, then latitudeBand will be 'N' or 'S'
			//otherwise, latitudeBand will be the actual latitudeBand
			//convert back to hemisphere strings:
			String hemisphere =
					charRepresentsHemisphere ? (latitudeBand <= 'N' ? AVKey.NORTH : AVKey.SOUTH)
							: (latitudeBand >= 'N' ? AVKey.NORTH : AVKey.SOUTH);

			try
			{
				final UTMCoordConverterAccessible converter = new UTMCoordConverterAccessible(globe);
				long err = converter.convertUTMToGeodetic(zone, hemisphere, easting, northing);

				if (err == UTMCoordConverterAccessible.UTM_NO_ERROR)
				{
					LatLon latlon =
							new LatLon(Angle.fromRadians(converter.getLatitude()),
									Angle.fromRadians(converter.getLongitude()));
					return latlon;
				}
			}
			catch (Exception e)
			{
				//ignore
			}
		}
		return null;
	}

	public static String capitalizeFirstLetter(String s)
	{
		if (s == null)
			return null;
		if (s.isEmpty())
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	public static double clamp(double value, double min, double max)
	{
		return Math.max(min, Math.min(max, value));
	}

	public static LatLon clampLatLon(LatLon latlon, Sector sector)
	{
		double lat =
				clamp(latlon.latitude.degrees, sector.getMinLatitude().degrees,
						sector.getMaxLatitude().degrees);
		double lon =
				clamp(latlon.longitude.degrees, sector.getMinLongitude().degrees,
						sector.getMaxLongitude().degrees);

		return LatLon.fromDegrees(lat, lon);
	}
}
