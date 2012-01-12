package au.gov.ga.worldwind.tiler.mapnik;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.tiler.mask.MaskDeleter;
import au.gov.ga.worldwind.tiler.util.InputStreamHandler;
import au.gov.ga.worldwind.tiler.util.Prefs;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.TilerException;


public class MapnikUtil
{
	public static Sector getSector(File input) throws TilerException
	{
		String command = "mapnik/Nik2Img.exe -m \"" + input.getAbsolutePath() + "\" -e";
		final StringBuilder sb = new StringBuilder();
		final StringBuilder eb = new StringBuilder();
		try
		{
			Process process = Runtime.getRuntime().exec(command, null, new File("mapnik"));
			new InputStreamHandler(process.getInputStream())
			{
				@Override
				public void handle(String string)
				{
					sb.append(string);
				}
			};
			new InputStreamHandler(process.getErrorStream())
			{
				@Override
				public void handle(String string)
				{
					eb.append(string);
				}
			};
			process.waitFor();
		}
		catch (Exception e)
		{
			throw new TilerException(e.getLocalizedMessage());
		}

		String error = eb.toString();
		if (error.length() > 0)
			throw new TilerException(error);

		String output = sb.toString();
		Pattern pattern = Pattern.compile("(-?[\\d\\.]+),(-?[\\d\\.]+),(-?[\\d\\.]+),(-?[\\d\\.]+)");
		Matcher matcher = pattern.matcher(output);
		if (matcher.find())
		{
			try
			{
				double minlon = Double.parseDouble(matcher.group(1));
				double minlat = Double.parseDouble(matcher.group(2));
				double maxlon = Double.parseDouble(matcher.group(3));
				double maxlat = Double.parseDouble(matcher.group(4));
				return new Sector(minlat, minlon, maxlat, maxlon);
			}
			catch (NumberFormatException e)
			{
				//ignore
			}
		}

		return null;
	}

	public static void tile(Sector sector, int width, int height, boolean ignoreBlank, boolean reproject, File input,
			File dst, final Logger logger) throws TilerException
	{
		String format = dst.getName().toLowerCase().endsWith("jpg") ? "jpg" : "png";
		String command =
				"mapnik/Nik2Img.exe -m \"" + input.getAbsolutePath() + "\" -o \"" + dst.getAbsolutePath() + "\" -f "
						+ format + " -b " + sector.getMinLongitude() + "," + sector.getMinLatitude() + ","
						+ sector.getMaxLongitude() + "," + sector.getMaxLatitude() + " -d " + width + "," + height;
		if (reproject)
		{
			command += " -s epsg:4326";
		}

		try
		{
			Process process = Runtime.getRuntime().exec(command, null, new File("mapnik"));

			new InputStreamHandler(process.getInputStream())
			{
				@Override
				public void handle(String string)
				{
					logger.finer(string);
				}
			};
			new InputStreamHandler(process.getErrorStream())
			{
				@Override
				public void handle(String string)
				{
					logger.severe(string);
				}
			};
			process.waitFor();
		}
		catch (Exception e)
		{
			throw new TilerException(e.getLocalizedMessage());
		}

		if (ignoreBlank)
		{
			try
			{
				BufferedImage image = ImageIO.read(dst);
				if (MaskDeleter.isEmpty(image))
				{
					dst.delete();
				}
			}
			catch (IOException e)
			{
				logger.severe(e.getLocalizedMessage());
			}
		}
	}
}
