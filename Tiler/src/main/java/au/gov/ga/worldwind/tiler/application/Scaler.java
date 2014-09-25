/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.worldwind.tiler.application;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.tiler.application.Console.ConsoleProgressReporter;
import au.gov.ga.worldwind.tiler.util.FileFilters.DirectoryFileFilter;
import au.gov.ga.worldwind.tiler.util.FileFilters.ExtensionFileFilter;
import au.gov.ga.worldwind.tiler.util.LatLon;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;

/**
 * Utility that takes a tileset with tiles of a certain size and splits them to
 * a tileset of tiles of a smaller size (has to be a power of two smaller, ie
 * half, quarter, eighth, etc).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Scaler
{
	public static void main(String[] args) throws IOException
	{
		//scaling/tileset parameters
		int srcTileSize = 4096;
		int dstTileSize = 256;
		File srcDirectory = new File("E:/World/water/worldwaterbodies_tiles_4096");
		File dstDirectory = new File("E:/World/water/worldwaterbodies_tiles_256");
		String extension = "png";
		
		//overview parameters
		NullableNumberArray outsideValues = null;
		Sector sector = Sector.FULL_SPHERE;
		LatLon origin = LatLon.DEFAULT_ORIGIN;
		double lzts = 180;
		boolean bilinear = true;
		boolean ignoreBlankTiles = false;
		float jpegQuality = 0.8f;

		//validate tile sizes
		int perTile = srcTileSize / dstTileSize;
		if (perTile * dstTileSize != srcTileSize)
		{
			throw new IllegalArgumentException("Not a multiple!");
		}
		int log2 = (int) Math.round(Math.log10(perTile) / Math.log10(2));
		if (Math.pow(log2, 2.0) != perTile)
		{
			throw new IllegalArgumentException("Multiple not a power of 2");
		}

		File[] dirs = srcDirectory.listFiles(new DirectoryFileFilter());
		int maxlevel = Integer.MIN_VALUE;
		for (File dir : dirs)
		{
			try
			{
				int num = Integer.parseInt(dir.getName());
				if (num > maxlevel)
				{
					maxlevel = num;
				}
			}
			catch (NumberFormatException e)
			{
			}
		}
		
		ProgressReporter progress = new ConsoleProgressReporter();

		File inputDirectory = new File(srcDirectory, "" + maxlevel);
		File outputDirectory = new File(dstDirectory, "" + (maxlevel + log2));

		List<File> tiles = new ArrayList<File>();
		File[] rows = inputDirectory.listFiles(new DirectoryFileFilter());
		for (File rowDir : rows)
		{
			File[] tileFiles = rowDir.listFiles(new ExtensionFileFilter(extension));
			tiles.addAll(Arrays.asList(tileFiles));
		}

		//column 0 from rows 

		Pattern pattern = Pattern.compile("(\\d+)_(\\d+)\\..*");
		for (int i = 0; i < tiles.size(); i++)
		{
			File tile = tiles.get(i);
			Matcher matcher = pattern.matcher(tile.getName());
			if (matcher.find())
			{
				int row = Integer.parseInt(matcher.group(1));
				int col = Integer.parseInt(matcher.group(2));
				BufferedImage input = null;
				for (int y = 0; y < perTile; y++)
				{
					int sy = dstTileSize * (perTile - 1 - y);
					int dy = row * perTile + y;
					File outputRowDir = new File(outputDirectory, Util.paddedInt(dy, 4));
					outputRowDir.mkdirs();
					for (int x = 0; x < perTile; x++)
					{
						int sx = dstTileSize * x;
						int dx = col * perTile + x;
						File outputFile =
								new File(outputRowDir, Util.paddedInt(dy, 4) + "_" + Util.paddedInt(dx, 4) +
										"." + extension);
						if (outputFile.exists())
						{
							continue;
						}
						if (input == null)
						{
							input = ImageIO.read(tile);
						}
						BufferedImage output =
								new BufferedImage(dstTileSize, dstTileSize, BufferedImage.TYPE_INT_ARGB);
						Graphics2D graphics = output.createGraphics();
						graphics.drawImage(input, 0, 0, dstTileSize, dstTileSize,
								sx, sy, sx + dstTileSize, sy + dstTileSize, null);
						graphics.dispose();
						ImageIO.write(output, extension, outputFile);
					}
				}
			}

			double percent = i / (double) tiles.size();
			progress.progress(percent);
		}
		progress.progress(1);

		Overviewer.createImageOverviews(dstDirectory, extension, dstTileSize, dstTileSize, outsideValues, sector,
				origin, lzts, bilinear, ignoreBlankTiles, jpegQuality, progress);
	}
}
