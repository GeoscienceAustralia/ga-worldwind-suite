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
package au.gov.ga.worldwind.tiler.application;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.mapnik.MapnikUtil;
import au.gov.ga.worldwind.tiler.util.LatLon;
import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;

/**
 * Helper class used to generate the tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Tiler
{
	/**
	 * Enum of the various tiling formats/types supported by this application.
	 */
	public enum TilingType
	{
		Images,
		Elevations,
		Mapnik
	}

	/**
	 * Tile the given image dataset at the given level.
	 * 
	 * @param dataset
	 *            Dataset to tile
	 * @param reprojectIfRequired
	 *            If the dataset isn't in WGS84, should it be reprojected?
	 * @param linearInterpolationIfRequired
	 *            Should linear interpolation be used when reprojecting and
	 *            resizing?
	 * @param sector
	 *            Sector to tile
	 * @param origin
	 *            Origin to use when tiling (used to calculate tile row/column
	 *            numbers)
	 * @param level
	 *            Level at which to tile
	 * @param tilesize
	 *            Size of the tiles (width/height)
	 * @param lzts
	 *            Level zero tile size (in degrees)
	 * @param imageFormat
	 *            Format to save images in (must be supported by {@link ImageIO}
	 *            )
	 * @param addAlpha
	 *            Should an alpha channel be added to the image?
	 * @param jpegQuality
	 *            JPEG compression to use (if using the JPEG image format)
	 * @param outsideValues
	 *            Values to set data outside the dataset extents to (for each
	 *            band)
	 * @param ignoreBlank
	 *            Should blank tiles be ignored/not saved? (when each pixel is
	 *            equal to the outsideValues)
	 * @param replaceMinMaxs
	 *            Ranges to search for when replacing values
	 * @param replace
	 *            Values to use when replacing values inside the given replace
	 *            ranges (if a replacement value is null, the original value is
	 *            used)
	 * @param otherwise
	 *            Values to use when not replacing values inside the given
	 *            replace ranges (if an otherwise value is null, the original
	 *            value is used)
	 * @param outputDirectory
	 *            Tile output directory
	 * @param resume
	 *            Should the tiling progress be resumed at the last point (last
	 *            tile file is searched for, and then tiling begins from the
	 *            next tile)
	 * @param progress
	 *            Object to report progress to
	 */
	public static void tileImages(Dataset dataset, boolean reprojectIfRequired, boolean linearInterpolationIfRequired,
			Sector sector, LatLon origin, int level, int tilesize, double lzts, String imageFormat, boolean addAlpha,
			float jpegQuality, NullableNumberArray outsideValues, boolean ignoreBlank, MinMaxArray[] replaceMinMaxs,
			NullableNumberArray replace, NullableNumberArray otherwise, File outputDirectory, boolean resume,
			ProgressReporter progress)
	{
		tile(TilingType.Images, dataset, reprojectIfRequired, linearInterpolationIfRequired, null, sector, origin,
				level, tilesize, lzts, imageFormat, addAlpha, jpegQuality, -1, -1, outsideValues, ignoreBlank,
				replaceMinMaxs, replace, otherwise, null, outputDirectory, resume, progress);
	}

	/**
	 * Tile the given elevation dataset at the given level.
	 * 
	 * @param dataset
	 *            Dataset to tile
	 * @param reprojectIfRequired
	 *            If the dataset isn't in WGS84, should it be reprojected?
	 * @param linearInterpolationIfRequired
	 *            Should linear interpolation be used when reprojecting and
	 *            resizing?
	 * @param sector
	 *            Sector to tile
	 * @param origin
	 *            Origin to use when tiling (used to calculate tile row/colum
	 *            numbers)
	 * @param level
	 *            Level at which to tile
	 * @param tilesize
	 *            Size of the tiles (width/height)
	 * @param lzts
	 *            Level zero tile size (in degrees)
	 * @param bufferType
	 *            GDAL data type
	 * @param band
	 *            Band to tile (usually 1, not zero indexed)
	 * @param outsideValues
	 *            Value to set data outside the dataset extents to
	 * @param replaceMinMaxs
	 *            Ranges to search for when replacing values
	 * @param replace
	 *            Values to use when replacing values inside the given replace
	 *            ranges (if a replacement value is null, the original value is
	 *            used)
	 * @param otherwise
	 *            Values to use when not replacing values inside the given
	 *            replace ranges (if an otherwise value is null, the original
	 *            value is used)
	 * @param minMax
	 *            Array to store minimum/maximum elevations in
	 * @param outputDirectory
	 *            Tile output directory
	 * @param resume
	 *            Should the tiling progress be resumed at the last point (last
	 *            tile file is searched for, and then tiling begins from the
	 *            next tile)
	 * @param progress
	 *            Object to report progress to
	 */
	public static void tileElevations(Dataset dataset, boolean reprojectIfRequired,
			boolean linearInterpolationIfRequired, Sector sector, LatLon origin, int level, int tilesize, double lzts,
			int bufferType, int band, NullableNumberArray outsideValues, MinMaxArray[] replaceMinMaxs,
			NullableNumberArray replace, NullableNumberArray otherwise, NumberArray minMax, File outputDirectory,
			boolean resume, ProgressReporter progress)
	{
		tile(TilingType.Elevations, dataset, reprojectIfRequired, linearInterpolationIfRequired, null, sector, origin,
				level, tilesize, lzts, null, false, -1, bufferType, band, outsideValues, false, replaceMinMaxs,
				replace, otherwise, minMax, outputDirectory, resume, progress);
	}

	/**
	 * Tile the given Mapnik XML dataset.
	 * 
	 * @param mapFile
	 *            Mapnik XML mapfile to tile
	 * @param sector
	 *            Sector to tile
	 * @param origin
	 *            Origin to use when tiling (used to calculate tile row/colum
	 *            numbers)
	 * @param level
	 *            Level at which to tile
	 * @param tilesize
	 *            Size of the tiles (width/height)
	 * @param lzts
	 *            Level zero tile size (in degrees)
	 * @param imageFormat
	 *            Image output format (must be supported by the Mapnik util)
	 * @param ignoreBlank
	 *            Should blank (transparent) tiles be ignored/not saved?
	 * @param reprojectIfRequired
	 *            If the dataset isn't in WGS84, should it be reprojected?
	 * @param outputDirectory
	 *            Tile output directory
	 * @param resume
	 *            Should the tiling progress be resumed at the last point (last
	 *            tile file is searched for, and then tiling begins from the
	 *            next tile)
	 * @param progress
	 *            Object to report progress to
	 */
	public static void tileMapnik(File mapFile, Sector sector, LatLon origin, int level, int tilesize, double lzts,
			String imageFormat, boolean ignoreBlank, boolean reprojectIfRequired, File outputDirectory, boolean resume,
			ProgressReporter progress)
	{
		tile(TilingType.Mapnik, null, reprojectIfRequired, false, mapFile, sector, origin, level, tilesize, lzts,
				imageFormat, false, -1, -1, -1, null, ignoreBlank, null, null, null, null, outputDirectory, resume,
				progress);
	}

	private static void tile(TilingType type, Dataset dataset, boolean reprojectIfRequired,
			boolean linearInterpolationIfRequired, File mapFile, Sector sector, LatLon origin, int level, int tilesize,
			double lzts, String imageFormat, boolean addAlpha, float jpegQuality, int bufferType, int band,
			NullableNumberArray outsideValues, boolean ignoreBlank, MinMaxArray[] replaceMinMaxs,
			NullableNumberArray replace, NullableNumberArray otherwise, NumberArray minMax, File outputDirectory,
			boolean resume, ProgressReporter progress)
	{
		progress.getLogger().info("Generating tiles...");

		String outputExt = type == TilingType.Elevations ? "bil" : imageFormat;

		double tilesizedegrees = Math.pow(0.5, level) * lzts;
		int minX = Util.getTileX(sector.getMinLongitude() + 1e-10, origin, level, lzts);
		int maxX = Util.getTileX(sector.getMaxLongitude() - 1e-10, origin, level, lzts);
		int minY = Util.getTileY(sector.getMinLatitude() + 1e-10, origin, level, lzts);
		int maxY = Util.getTileY(sector.getMaxLatitude() - 1e-10, origin, level, lzts);

		File levelDir = new File(outputDirectory, String.valueOf(level));

		int startX = minX;
		int startY = minY;
		if (resume)
		{
			//check if this data has been tiled before; if so, start from previous position
			for (int Y = minY; Y <= maxY; Y++)
			{
				File rowDir = new File(levelDir, Util.paddedInt(Y, 4));
				if (rowDir.exists())
				{
					startY = Y;
				}
			}

			File rowDir = new File(levelDir, Util.paddedInt(startY, 4));
			if (rowDir.exists())
			{
				for (int X = minX; X <= maxX; X++)
				{
					final File dst =
							new File(rowDir, Util.paddedInt(startY, 4) + "_" + Util.paddedInt(X, 4) + "." + outputExt);
					if (dst.exists())
					{
						startX = X + 1;
					}
				}
			}
		}

		int xsize = maxX - minX + 1;
		int ysize = maxY - minY + 1;
		int size = xsize * ysize;
		int count = (startY - minY) * xsize + (startX - minX);
		for (int Y = startY; Y <= maxY; Y++)
		{
			if (progress.isCancelled())
				break;

			File rowDir = new File(levelDir, Util.paddedInt(Y, 4));
			if (!rowDir.exists())
			{
				rowDir.mkdirs();
			}

			for (int X = (Y == startY ? startX : minX); X <= maxX; X++)
			{
				if (progress.isCancelled())
					break;

				count++;
				progress.getLogger().fine(
						"Tile (" + X + "," + Y + "), " + count + "/" + size + " (" + (count * 100 / size)
								+ "%) (column " + (X - minX + 1) + "/" + xsize + ", row " + (Y - minY + 1) + "/"
								+ ysize + ")");
				progress.progress(count / (double) size);

				final double lat1 = (Y * tilesizedegrees) + origin.getLatitude();
				final double lon1 = (X * tilesizedegrees) + origin.getLongitude();
				final double lat2 = lat1 + tilesizedegrees;
				final double lon2 = lon1 + tilesizedegrees;
				Sector s = new Sector(lat1, lon1, lat2, lon2);

				final File dst = new File(rowDir, Util.paddedInt(Y, 4) + "_" + Util.paddedInt(X, 4) + "." + outputExt);
				if (dst.exists())
				{
					progress.getLogger().warning(dst.getAbsolutePath() + " already exists");
				}
				else
				{
					try
					{
						if (type == TilingType.Mapnik)
						{
							MapnikUtil.tile(s, tilesize, tilesize, ignoreBlank, reprojectIfRequired, mapFile, dst,
									progress.getLogger());
						}
						else
						{
							GDALTileParameters parameters =
									new GDALTileParameters(dataset, new Dimension(tilesize, tilesize), s);
							parameters.addAlpha = addAlpha;
							parameters.selectedBand = band;
							parameters.reprojectIfRequired = reprojectIfRequired;
							parameters.bilinearInterpolationIfRequired = linearInterpolationIfRequired;
							parameters.noData = outsideValues;
							parameters.minMaxs = replaceMinMaxs;
							parameters.replacement = replace;
							parameters.otherwise = otherwise;

							GDALTile tile = new GDALTile(parameters);
							if (type == TilingType.Elevations)
							{
								tile = tile.convertToType(bufferType);

								tile.updateMinMax(minMax, outsideValues);

								ByteBuffer bb = tile.getBuffer();
								bb.rewind();
								RandomAccessFile raf = null;
								try
								{
									raf = new RandomAccessFile(dst, "rw");
									MappedByteBuffer mbb = raf.getChannel().map(MapMode.READ_WRITE, 0, bb.limit());
									mbb.order(bb.order());
									mbb.put(bb);
								}
								finally
								{
									if (raf != null)
										raf.close();
								}
							}
							else
							{
								if (!(ignoreBlank && tile.isBlank()))
								{
									BufferedImage image = tile.getAsImage();
									writeImage(image, imageFormat, dst, jpegQuality);
								}
							}
						}
					}
					catch (Exception e)
					{
						progress.getLogger().severe(e.getMessage());
						try
						{
							Thread.sleep(1);
						}
						catch (InterruptedException e1)
						{
							e1.printStackTrace();
						}
					}
				}
			}
		}

		progress.getLogger().info("Tile generation " + (progress.isCancelled() ? "cancelled" : "complete"));
	}

	public static void writeImage(BufferedImage image, String format, File file, float jpegQuality) throws IOException
	{
		if ("jpg".equalsIgnoreCase(format))
		{
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = writers.next();
			JPEGImageWriteParam iwp = (JPEGImageWriteParam) writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(jpegQuality);
			iwp.setOptimizeHuffmanTables(true);
			FileImageOutputStream ios = null;
			try
			{
				ios = new FileImageOutputStream(file);
				writer.setOutput(ios);
				IIOImage iioimage = new IIOImage(image, null, null);
				writer.write(null, iioimage, iwp);
			}
			finally
			{
				if (ios != null)
					ios.close();
			}
		}
		else
		{
			ImageIO.write(image, format, file);
		}
	}
}
