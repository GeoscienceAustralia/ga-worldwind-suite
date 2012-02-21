package au.gov.ga.worldwind.tiler.util;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.gdal.gdal.Dataset;

public class Util
{
	private static final long FIFTY_MB = 1024 * 1024 * 50; 
	
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
	
	public static double optimalLztsd(Dataset dataset, Sector sector, int tilesize, double closeLztsd)
	{
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		double lonPixels = sector.getDeltaLongitude() / width;
		double latPixels = sector.getDeltaLatitude() / height;
		double texelSize = Math.min(latPixels, lonPixels);
		double tileDegrees = texelSize * tilesize;
		double level = Math.log10(closeLztsd / tileDegrees) / Math.log10(2);
		return Math.pow(2, Math.round(level)) * tileDegrees;
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
	
	public static String stripExtension(String filename)
	{
		if (filename == null || filename.trim().isEmpty())
		{
			return null;
		}
		
		if (filename.lastIndexOf('.') == -1)
		{
			return filename;
		}
		
		return filename.substring(0, filename.lastIndexOf('.'));
	}
	
	/**
	 * From Apache Commons FileUtils
	 * @see http://svn.apache.org/viewvc/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java?view=markup
	 */
	public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException
	{
		if (srcFile == null || destDir == null || !destDir.isDirectory())
		{
			throw new IllegalArgumentException();
		}
		File destFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, destFile, preserveFileDate);
	}
	
	/**
	 * From Apache Commons FileUtils
	 * 
	 * @see http://svn.apache.org/viewvc/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java?view=markup
	 */
	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' exists but is a directory");
		}
		if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		}
		if (destFile.getParentFile() != null && destFile.getParentFile().exists() == false) {
			if (destFile.getParentFile().mkdirs() == false) {
				throw new IOException("Destination '" + destFile + "' directory cannot be created");
			}
		}
		if (destFile.exists() && destFile.canWrite() == false) {
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate);
	}

	/**
	 * From Apache Commons FileUtils
	 * 
	 * @see http://svn.apache.org/viewvc/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java?view=markup
	 */
	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = (size - pos) > FIFTY_MB ? FIFTY_MB : (size - pos);
				pos += output.transferFrom(input, pos, count);
			}
		} finally {
			output.close();
			fos.close();
			input.close();
			fis.close();
		}

		if (srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}
	
	public static boolean isBlank(String str)
	{
		return str == null || str.trim().isEmpty();
	}
	
}
