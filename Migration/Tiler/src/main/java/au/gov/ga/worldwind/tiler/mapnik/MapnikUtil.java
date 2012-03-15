/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.tiler.mapnik;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.tiler.util.InputStreamHandler;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.TilerException;
import au.gov.ga.worldwind.tiler.util.Util;

/**
 * Helper class containing some utility functions for handling Mapnik XML
 * mapfiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MapnikUtil
{
	/**
	 * Calculate the sector of the given Mapnik XML mapfile.
	 * 
	 * @param input
	 * @return Sector of XML mapfile
	 * @throws TilerException
	 */
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

	/**
	 * Generate a tile from the given Mapnik XML mapfile within the given sector
	 * extents.
	 * 
	 * @param sector
	 *            Dataset extents contained within the tile
	 * @param width
	 *            Width of the tile
	 * @param height
	 *            Height of the tile
	 * @param ignoreBlank
	 *            Should blank (transparent) tiles be ignored? (This function
	 *            generates the tile anyway, and if it's blank, deletes it. Only
	 *            works for PNG output.)
	 * @param reproject
	 *            Should the dataset be reprojected to WGS84 if not already?
	 * @param input
	 *            Input Mapnik XML mapfile
	 * @param dst
	 *            Output image file
	 * @param logger
	 *            Logger which logs the progress
	 * @throws TilerException
	 *             If generating the tile fails for some reason
	 */
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
				if (Util.isEmpty(image))
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
