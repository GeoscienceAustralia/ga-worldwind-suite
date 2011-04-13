package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoordConverterAccessible;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class Util
{
	/** The settings folder name to use for GA world wind settings */
	public static final String SETTINGS_FOLDER_NAME = ".gaww";
	
	public final static double METER_TO_FEET = 3.280839895;
	public final static double METER_TO_MILE = 0.000621371192;

	public final static String UTM_COORDINATE_REGEX =
			"(?:[a-zA-Z]*\\s*)(\\d+)(?:\\s*)([a-zA-Z])(?:\\s+)((?:\\d*\\.?\\d+)|(?:\\d+))(?:[E|e]?)(?:\\s+)((?:\\d*\\.?\\d+)|(?:\\d+))(?:[N|n]?)";

	/**
	 * @return A string representation of the provided integer value, padded with 0's to a total
	 * length of <code>charcount</code>. Note: If length(value) > charcount, the input value
	 * will be returned as a string.
	 */
	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
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
	public static URL getLocalTileURL(String service, String dataset, int level, int row, int col, URL context,
			String format, String defaultExt)
	{
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
				level + File.separator + Util.paddedInt(row, 4) + File.separator + Util.paddedInt(row, 4) + "_"
						+ Util.paddedInt(col, 4) + "." + ext;

		try
		{
			if (parent.isFile() && isZip)
			{
				//zip file; return URL using 'jar' protocol
				String entry1 = filename;
				//if file is not found, attempt to find a file with the defaultExt in the zip as well
				String entry2 =
						ext.equals(defaultExt) ? null : filename.substring(0, filename.length() - ext.length())
								+ defaultExt;

				URL url = entry2 != null ? Util.zipEntryUrl(parent, entry1, entry2) : Util.zipEntryUrl(parent, entry1);
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
	 */
	private static File getPathWithinContext(String path, URL context)
	{
		//first attempt finding of the directory using a URL
		try
		{
			URL url = context == null ? new URL(path) : new URL(context, path);
			File file = URLUtil.urlToFile(url);
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
			File file = URLUtil.urlToFile(context);
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

	/**
	 * @return A string containing random characters <code>[a-zA-z]</code> of length <code>length</code>
	 */
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
		return getScaledLengthMillis(beginLatLon, endLatLon, (long) (4000 / scale), (long) (20000 / scale));
	}

	public static long getScaledLengthMillis(LatLon beginLatLon, LatLon endLatLon, long minLengthMillis,
			long maxLengthMillis)
	{
		Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
		double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	public static long getScaledLengthMillis(double beginZoom, double endZoom, long minLengthMillis, long maxLengthMillis)
	{
		double scaleFactor = Math.abs(endZoom - beginZoom) / Math.max(endZoom, beginZoom);
		scaleFactor = clamp(scaleFactor, 0.0, 1.0);
		return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
	}

	/**
	 * Returns a 'mixing' of the values <code>value1</code> and <code>value2</code> using <code>1-amount<code> of <code>value1</code> linearly combined
	 * with <code>amount</code> of <code>value2</code>. <code>amount</code> should be expressed as a percentage in the range <code>[0,1]</code>
	 */
	public static double mixDouble(double amount, double value1, double value2)
	{
		if (amount < 0)
			return value1;
		else if (amount > 1)
			return value2;
		return value1 * (1.0 - amount) + value2 * amount;
	}

	/**
	 * @return The provided value as a percentage of the interval <code>[min, max]</code>, clamped to the interval <code>[0,1]</code>
	 */
	public static double percentDouble(double value, double min, double max)
	{
		if (value < min)
			return 0;
		if (value > max)
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
		return clamp(unclampedRatio, 0, 1);
	}

	public static Position computePositionFromString(String positionString)
	{
		return computePositionFromString(positionString, null);
	}

	public static Position computePositionFromString(String positionString, Globe globe)
	{
		int lastIndexOfSpace = positionString.trim().lastIndexOf(' ');
		if (lastIndexOfSpace < 0)
			return null;
		String latlonString = positionString.substring(0, lastIndexOfSpace);
		String elevationString = positionString.substring(lastIndexOfSpace + 1);

		double elevation;
		try
		{
			elevation = Double.parseDouble(elevationString);
		}
		catch (Exception e)
		{
			return null;
		}

		LatLon ll = computeLatLonFromString(latlonString, globe);
		if (ll == null)
			return null;

		return new Position(ll, elevation);
	}

	/**
	 * @see #computeLatLonFromString(String, Globe)
	 */
	public static LatLon computeLatLonFromString(String coordString)
	{
		return computeLatLonFromString(coordString, null);
	}

	/**
	 * Tries to extract a latitude and a longitude from the given text string.
	 * <p/>
	 * Supported formats are:
	 * <ol>
	 * 	<li>Comma- or space-separated signed decimal degrees, with optional E/W/N/S suffixes (eg. <code>-97.345, 123.45</code> or <code>97.345S 123.345W</code>)</li>
	 * 	<li>Comma- or space-separated degree-minute-second blocks, with optional E/W/N/S suffixes (eg. <code>-123° 34' 42", +45° 12' 30"</code> or <code>123° 34' 42"S 45° 12' 30"W</code>)</li>
	 * </ol>
	 * <p/>
	 * If the parsed Lat-Lons are outside of the valid range of <code>Lat=[-90,90]</code> and <code>Lon=[-180,180]</code>, or parsing fails, will return <code>null</code>.
	 * @param coordString the input string 
	 * @param globe the current <code>Globe</code> (Optional).
	 * 
	 * @return the corresponding <code>LatLon</code> or <code>null</code>.
	 */
	public static LatLon computeLatLonFromString(String coordString, Globe globe)
	{
		if (isBlank(coordString))
		{
			String msg = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(msg);
			return null;
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
					// NOTE: the MGRSCoord does not always report errors with invalid strings,
					// but will have lat and lon set to zero
					if (MGRS.getLatitude().degrees != 0 || MGRS.getLatitude().degrees != 0)
					{
						lat = MGRS.getLatitude();
						lon = MGRS.getLongitude();
					}
					else
					{
						return null;
					}
				}
				catch (IllegalArgumentException e)
				{
					return null;
				}
			}
		}

		// Try to extract a pair of signed decimal values separated by a space, ',' or ', '
		// Allow E, W, S, N suffixes
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
			regex = "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}[m|M|'|\u2019|\\s])?(\\s*\\d{1,2}[s|S|\"|\u201d])?\\s*[N|n|S|s]?)";
			regex += separators;
			regex += "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}[m|M|'|\u2019|\\s])?(\\s*\\d{1,2}[s|S|\"|\u201d])?\\s*[E|e|W|w]?)";
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(coordString);
			if (matcher.matches())
			{
				lat = parseDMSString(matcher.group(1));
				lon = parseDMSString(matcher.group(5));
			}
		}

		if (lat == null || lon == null)
		{
			return null;
		}

		if (lat.degrees >= -90 && lat.degrees <= 90 && lon.degrees >= -180 && lon.degrees <= 180)
		{
			return new LatLon(lat, lon);
		}

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
	public static LatLon computeLatLonFromUTMString(String coordString, Globe globe, boolean charRepresentsHemisphere)
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
					charRepresentsHemisphere ? (latitudeBand <= 'N' ? AVKey.NORTH : AVKey.SOUTH) : (latitudeBand >= 'N'
							? AVKey.NORTH : AVKey.SOUTH);

			try
			{
				final UTMCoordConverterAccessible converter = new UTMCoordConverterAccessible(globe);
				long err = converter.convertUTMToGeodetic(zone, hemisphere, easting, northing);

				if (err == UTMCoordConverterAccessible.UTM_NO_ERROR)
				{
					LatLon latlon =
							new LatLon(Angle.fromRadians(converter.getLatitude()), Angle.fromRadians(converter
									.getLongitude()));
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

	/**
	 * @return The capitalised version of the provided string
	 */
	public static String capitalizeFirstLetter(String s)
	{
		if (isBlank(s))
		{
			return s;
		}
		
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	/**
	 * @return Whether the provided string is blank (<code>null</code>, empty
	 *         string, or contains only whitespace)
	 */
	public static boolean isBlank(String string)
	{
		return string == null || string.trim().isEmpty();
	}

	/**
	 * @return Whether the provided collection is <code>null</code> or empty
	 */
	public static boolean isEmpty(Collection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}
	
	/**
	 * @return Whether the provided map is <code>null</code> or empty
	 */
	public static boolean isEmpty(Map<?,?> map)
	{
		return map == null || map.isEmpty();
	}
	
	/**
	 * @return Whether the provided array is <code>null</code> or empty
	 */
	public static boolean isEmpty(Object[] array)
	{
		return array == null || array.length == 0;
	}
	
	/**
	 * Clamp the provided value to the range specified by <code>[min, max]</code>
	 */
	public static int clamp(int value, int min, int max)
	{
		Validate.isTrue(min <= max, "Min must be less than or equal to max");
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Clamp the provided value to the range specified by <code>[min, max]</code>
	 */
	public static double clamp(double value, double min, double max)
	{
		Validate.isTrue(min <= max, "Min must be less than or equal to max");
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Clamp the provided {@link LatLon} pair to be within the provided {@link Sector} extents.
	 */
	public static LatLon clampLatLon(LatLon latlon, Sector sector)
	{
		if (latlon ==  null || sector == null)
		{
			return latlon;
		}
		
		double lat = clamp(latlon.latitude.degrees, sector.getMinLatitude().degrees, sector.getMaxLatitude().degrees);
		double lon = clamp(latlon.longitude.degrees, sector.getMinLongitude().degrees, sector.getMaxLongitude().degrees);

		return LatLon.fromDegrees(lat, lon);
	}
	
	/**
	 * Clamp the provided {@link Sector} to be within the provided {@link Sector} extents.
	 */
	public static Sector clampSector(Sector source, Sector extents)
	{
		if (source == null || extents == null)
		{
			return source;
		}
		
		double minLat = clamp(source.getMinLatitude().degrees, extents.getMinLatitude().degrees, extents.getMaxLatitude().degrees);
		double maxLat = clamp(source.getMaxLatitude().degrees, extents.getMinLatitude().degrees, extents.getMaxLatitude().degrees);
		double minLon = clamp(source.getMinLongitude().degrees, extents.getMinLongitude().degrees, extents.getMaxLongitude().degrees);
		double maxLon = clamp(source.getMaxLongitude().degrees, extents.getMinLongitude().degrees, extents.getMaxLongitude().degrees);
		
		return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
	}

	/**
	 * Calculate the position on the globe in the center of the view. If the
	 * view is not looking at a point on the globe, the closest position is
	 * found by calculating the horizon distance.
	 * 
	 * @param view
	 *            View to find center position for
	 * @param eyePoint
	 *            Eye point in model coordinates (if null, requested from view)
	 * @return View's closest center position on the globe
	 */
	public static Position computeViewClosestCenterPosition(View view, Vec4 eyePoint)
	{
		Globe globe = view.getGlobe();

		Vec4 centerPoint = view.getCenterPoint();
		if (centerPoint != null)
		{
			return globe.computePositionFromPoint(centerPoint);
		}

		//center point is not on the globe, so compute the closest point on the horizon

		if (eyePoint == null)
		{
			eyePoint = view.getEyePoint();
		}

		Vec4 forward = view.getForwardVector().normalize3();
		Vec4 normal = globe.computeSurfaceNormalAtPoint(eyePoint);
		Vec4 left = normal.cross3(forward);
		forward = left.cross3(normal);

		double horizonDistance = view.computeHorizonDistance();
		centerPoint = eyePoint.add3(forward.multiply3(horizonDistance));
		Position pos = globe.computePositionFromPoint(centerPoint);
		double elevation = globe.getElevation(pos.latitude, pos.longitude);

		return new Position(pos, elevation);
	}

	/**
	 * Parse a Vec4 from a String representation of the form <code>[(]x,[ ]y,[ ]z[,w][)]</code>
	 */
	public static Vec4 computeVec4FromString(String text)
	{
		if (isBlank(text))
		{
			return null;
		}
		
		String separators = "(\\s+|,|,\\s+)"; // Separate on commas (with or without whitespace)
		String[] split = text.replaceAll("\\s", "").replaceAll("\\(|\\)", "").split(separators); // Clean up whitespace and braces before splitting
		if (split.length == 3 || split.length == 4)
		{
			try
			{
				double x = Double.valueOf(split[0]);
				double y = Double.valueOf(split[1]);
				double z = Double.valueOf(split[2]);
				double w = 1d;
				if (split.length == 4)
				{
					w = Double.valueOf(split[3]);
				}
				return new Vec4(x, y, z, w);
			}
			catch (NumberFormatException e)
			{
			}
		}
		return null;
	}
	
	/**
	 * @return The user's home directory
	 */
	public static File getUserGAWorldWindDirectory()
	{
		String home = System.getProperty("user.home");
		File homeDir = new File(home);
		File dir = new File(homeDir, SETTINGS_FOLDER_NAME);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	/**
	 * Read the provided stream into a String
	 */
	public static String readStreamToString(InputStream stream) throws Exception
	{
		if (stream == null)
		{
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer result = new StringBuffer();
		String readLine = null;
		while((readLine = reader.readLine()) != null)
		{
			if (result.length() > 0)
			{
				result.append('\n');
			}
			result.append(readLine);
		}
		reader.close();
		return result.toString();
	}
}
