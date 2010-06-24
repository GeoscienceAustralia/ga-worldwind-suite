package au.gov.ga.worldwind.tiler.util;

import org.gdal.gdal.Dataset;

public class Util
{
	public static String fixNewlines(String s)
	{
		String newLine = System.getProperty("line.separator");
		return s.replaceAll("(\\r\\n)|(\\r)|(\\n)", newLine);
	}

	public static int levelCount(Dataset dataset, double lztd, Sector sector, int tilesize)
	{
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		double lonPixels = sector.getDeltaLongitude() / width;
		double latPixels = sector.getDeltaLatitude() / height;
		double texelSize = Math.min(latPixels, lonPixels);
		int level = (int) Math.ceil(Math.log10(texelSize * tilesize / lztd) / Math.log10(0.5)) + 1;
		return Math.max(level, 1);
	}

	public static int tileCount(Sector sector, int level, double lztsd)
	{
		int minX = getTileX(sector.getMinLongitude() + 1e-10, level, lztsd);
		int maxX = getTileX(sector.getMaxLongitude() - 1e-10, level, lztsd);
		int minY = getTileY(sector.getMinLatitude() + 1e-10, level, lztsd);
		int maxY = getTileY(sector.getMaxLatitude() - 1e-10, level, lztsd);
		return (maxX - minX + 1) * (maxY - minY + 1);
	}

	public static int getTileX(double longitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude + 180) / (lztsd * layerpow);
		return (int) X;
	}

	public static int getTileY(double latitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude + 90) / (lztsd * layerpow);
		return (int) Y;
	}

	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}

	public static int limitRange(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	public static double limitRange(double value, double min, double max)
	{
		return Math.max(min, Math.min(max, value));
	}
}
