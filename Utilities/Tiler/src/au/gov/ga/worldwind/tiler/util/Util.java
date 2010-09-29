package au.gov.ga.worldwind.tiler.util;

import java.awt.Color;

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

	public static int tileCount(Sector sector, LatLon origin, int level, double lztsd)
	{
		int minX = getTileX(sector.getMinLongitude() + 1e-10, origin, level, lztsd);
		int maxX = getTileX(sector.getMaxLongitude() - 1e-10, origin, level, lztsd);
		int minY = getTileY(sector.getMinLatitude() + 1e-10, origin, level, lztsd);
		int maxY = getTileY(sector.getMaxLatitude() - 1e-10, origin, level, lztsd);
		return (maxX - minX + 1) * (maxY - minY + 1);
	}

	public static int getTileX(double longitude, LatLon origin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude - origin.getLongitude()) / (lztsd * layerpow);
		return (int) X;
	}

	public static int getTileY(double latitude, LatLon origin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude - origin.getLatitude()) / (lztsd * layerpow);
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

	public static int colorToAlpha(int argb, Color color)
	{
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = (argb) & 0xff;

		float pr = distancePercent(r, color.getRed(), 0, 255);
		float pg = distancePercent(g, color.getGreen(), 0, 255);
		float pb = distancePercent(b, color.getBlue(), 0, 255);
		float percent = Math.max(pr, Math.max(pg, pb));

		//(image - color) / alpha + color
		if (percent > 0)
		{
			r = (int) ((r - color.getRed()) / percent) + color.getRed();
			g = (int) ((g - color.getGreen()) / percent) + color.getGreen();
			b = (int) ((b - color.getBlue()) / percent) + color.getBlue();
		}
		a = (int) (a * percent);

		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}

	private static float distancePercent(int value, int distanceTo, int min, int max)
	{
		float diff = 0f;
		if (value < distanceTo)
		{
			diff = (distanceTo - value) / (float) (distanceTo - min);
		}
		else if (value > distanceTo)
		{
			diff = (value - distanceTo) / (float) (max - distanceTo);
		}
		return Math.max(0f, Math.min(1f, diff));
	}
}
