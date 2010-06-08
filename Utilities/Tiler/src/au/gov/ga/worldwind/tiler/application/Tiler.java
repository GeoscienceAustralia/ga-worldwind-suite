package au.gov.ga.worldwind.tiler.application;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import javax.imageio.ImageIO;


import org.gdal.gdal.Dataset;

import au.gov.ga.worldwind.tiler.gdal.GDALTile;
import au.gov.ga.worldwind.tiler.gdal.GDALTileParameters;
import au.gov.ga.worldwind.tiler.mapnik.MapnikUtil;
import au.gov.ga.worldwind.tiler.util.MinMaxArray;
import au.gov.ga.worldwind.tiler.util.NullableNumberArray;
import au.gov.ga.worldwind.tiler.util.NumberArray;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;


public class Tiler
{
	public enum TilingType
	{
		Images, Elevations, Mapnik
	}

	public static void tileImages(Dataset dataset, boolean reprojectIfRequired,
			boolean linearInterpolationIfRequired, Sector sector, int level, int tilesize,
			double lzts, String imageFormat, boolean addAlpha, NullableNumberArray outsideValues,
			MinMaxArray[] replaceMinMaxs, NullableNumberArray replace,
			NullableNumberArray otherwise, File outputDirectory, ProgressReporter progress)
	{
		tile(TilingType.Images, dataset, reprojectIfRequired, linearInterpolationIfRequired, null,
				sector, level, tilesize, lzts, imageFormat, addAlpha, -1, -1, outsideValues,
				replaceMinMaxs, replace, otherwise, null, outputDirectory, progress);
	}

	public static void tileElevations(Dataset dataset, boolean reprojectIfRequired,
			boolean linearInterpolationIfRequired, Sector sector, int level, int tilesize,
			double lzts, int bufferType, int band, NullableNumberArray outsideValues,
			MinMaxArray[] replaceMinMaxs, NullableNumberArray replace,
			NullableNumberArray otherwise, NumberArray minMax, File outputDirectory,
			ProgressReporter progress)
	{
		tile(TilingType.Elevations, dataset, reprojectIfRequired, linearInterpolationIfRequired,
				null, sector, level, tilesize, lzts, null, false, bufferType, band, outsideValues,
				replaceMinMaxs, replace, otherwise, minMax, outputDirectory, progress);
	}

	public static void tileMapnik(File mapFile, Sector sector, int level, int tilesize,
			double lzts, String imageFormat, File outputDirectory, ProgressReporter progress)
	{
		tile(TilingType.Mapnik, null, false, false, mapFile, sector, level, tilesize, lzts,
				imageFormat, false, -1, -1, null, null, null, null, null, outputDirectory, progress);
	}

	private static void tile(TilingType type, Dataset dataset, boolean reprojectIfRequired,
			boolean linearInterpolationIfRequired, File mapFile, Sector sector, int level,
			int tilesize, double lzts, String imageFormat, boolean addAlpha, int bufferType,
			int band, NullableNumberArray outsideValues, MinMaxArray[] replaceMinMaxs,
			NullableNumberArray replace, NullableNumberArray otherwise, NumberArray minMax,
			File outputDirectory, ProgressReporter progress)
	{
		progress.getLogger().info("Generating tiles...");

		String outputExt = type == TilingType.Elevations ? "bil" : imageFormat;

		double tilesizedegrees = Math.pow(0.5, level) * lzts;
		int minX = Util.getTileX(sector.getMinLongitude() + 1e-10, level, lzts);
		int maxX = Util.getTileX(sector.getMaxLongitude() - 1e-10, level, lzts);
		int minY = Util.getTileY(sector.getMinLatitude() + 1e-10, level, lzts);
		int maxY = Util.getTileY(sector.getMaxLatitude() - 1e-10, level, lzts);

		int size = (maxX - minX + 1) * (maxY - minY + 1);
		int count = 0;
		for (int Y = minY; Y <= maxY; Y++)
		{
			if (progress.isCancelled())
				break;

			File rowDir = new File(outputDirectory, String.valueOf(level));
			rowDir = new File(rowDir, Util.paddedInt(Y, 4));
			if (!rowDir.exists())
			{
				rowDir.mkdirs();
			}

			for (int X = minX; X <= maxX; X++)
			{
				if (progress.isCancelled())
					break;

				count++;
				progress.getLogger().fine(
						"Tile (" + X + "," + Y + "), " + count + "/" + size + " ("
								+ (count * 100 / size) + "%) (column " + (X - minX + 1) + "/"
								+ (maxX - minX + 1) + ", row " + (Y - minY + 1) + "/"
								+ (maxY - minY + 1) + ")");
				progress.progress(count / (double) size);

				final double lat1 = (Y * tilesizedegrees) - 90;
				final double lon1 = (X * tilesizedegrees) - 180;
				final double lat2 = lat1 + tilesizedegrees;
				final double lon2 = lon1 + tilesizedegrees;
				Sector s = new Sector(lat1, lon1, lat2, lon2);

				final File dst =
						new File(rowDir, Util.paddedInt(Y, 4) + "_" + Util.paddedInt(X, 4)
								+ "." + outputExt);
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
							MapnikUtil.tile(s, tilesize, tilesize, mapFile, dst, progress
									.getLogger());
						}
						else
						{
							GDALTileParameters parameters =
									new GDALTileParameters(dataset, new Dimension(tilesize,
											tilesize), s);
							parameters.addAlpha = addAlpha;
							parameters.selectedBand = band;
							parameters.reprojectIfRequired = reprojectIfRequired;
							parameters.bilinearInterpolationIfRequired =
									linearInterpolationIfRequired;
							parameters.noData = outsideValues;
							parameters.minMaxs = replaceMinMaxs;
							parameters.replacement = replace;
							parameters.otherwise = otherwise;
							
							GDALTile tile = new GDALTile(parameters);
							if (type == TilingType.Elevations)
							{
								tile = tile.convertToType(bufferType);
							}
							if (type == TilingType.Elevations)
							{
								tile.updateMinMax(minMax, outsideValues);

								ByteBuffer bb = tile.getBuffer();
								bb.rewind();
								RandomAccessFile raf = null;
								try
								{
									raf = new RandomAccessFile(dst, "rw");
									MappedByteBuffer mbb =
											raf.getChannel().map(MapMode.READ_WRITE, 0, bb.limit());
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
								BufferedImage image = tile.getAsImage();
								ImageIO.write(image, outputExt, dst);
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

		progress.getLogger().info(
				"Tile generation " + (progress.isCancelled() ? "cancelled" : "complete"));
	}
}
