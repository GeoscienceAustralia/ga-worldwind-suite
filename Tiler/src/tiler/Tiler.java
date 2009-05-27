package tiler;

import gdal.GDALTile;
import gdal.GDALUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import javax.imageio.ImageIO;

import mapnik.MapnikUtil;

import org.gdal.gdal.Dataset;

import util.ProgressReporter;
import util.Sector;

public class Tiler
{
	private enum Type
	{
		Images,
		Elevations,
		Mapnik
	}

	public static void tileImages(Dataset dataset, Sector sector, int level,
			int tilesize, double lzts, String imageFormat, boolean addAlpha,
			int[] outsideValues, int[] minReplace, int[] maxReplace,
			Integer[] replace, Integer[] otherwise, File outputDirectory,
			ProgressReporter progress)
	{
		tile(Type.Images, dataset, null, sector, level, tilesize, lzts,
				imageFormat, addAlpha, -1, -1, outsideValues, minReplace,
				maxReplace, replace, otherwise, outputDirectory, progress);
	}

	public static void tileElevations(Dataset dataset, Sector sector,
			int level, int tilesize, double lzts, int bufferType, int band,
			int[] outsideValues, int[] minReplace, int[] maxReplace,
			Integer[] replace, Integer[] otherwise, File outputDirectory,
			ProgressReporter progress)
	{
		tile(Type.Elevations, dataset, null, sector, level, tilesize, lzts,
				null, false, bufferType, band, outsideValues, minReplace,
				maxReplace, replace, otherwise, outputDirectory, progress);
	}

	public static void tileMapnik(File mapFile, Sector sector, int level,
			int tilesize, double lzts, String imageFormat,
			File outputDirectory, ProgressReporter progress)
	{
		tile(Type.Mapnik, null, mapFile, sector, level, tilesize, lzts,
				imageFormat, false, -1, -1, null, null, null, null, null,
				outputDirectory, progress);
	}

	private static void tile(Type type, Dataset dataset, File mapFile,
			Sector sector, int level, int tilesize, double lzts,
			String imageFormat, boolean addAlpha, int bufferType, int band,
			int[] outsideValues, int[] minReplace, int[] maxReplace,
			Integer[] replace, Integer[] otherwise, File outputDirectory,
			ProgressReporter progress)
	{
		progress.getLogger().info("Generating tiles...");

		String outputExt = type == Type.Elevations ? "bil" : imageFormat;

		double tilesizedegrees = Math.pow(0.5, level) * lzts;
		int minX = GDALUtil.getTileX(sector.getMinLongitude() + 1e-10, level,
				lzts);
		int maxX = GDALUtil.getTileX(sector.getMaxLongitude() - 1e-10, level,
				lzts);
		int minY = GDALUtil.getTileY(sector.getMinLatitude() + 1e-10, level,
				lzts);
		int maxY = GDALUtil.getTileY(sector.getMaxLatitude() - 1e-10, level,
				lzts);

		int size = (maxX - minX + 1) * (maxY - minY + 1);
		int count = 0;
		for (int Y = minY; Y <= maxY; Y++)
		{
			if (progress.isCancelled())
				break;

			File rowDir = new File(outputDirectory, String.valueOf(level));
			rowDir = new File(rowDir, GDALUtil.paddedInt(Y, 4));
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
						"Tile (" + X + "," + Y + "), " + count + "/" + size
								+ " (" + (count * 100 / size) + "%) (column "
								+ (X - minX + 1) + "/" + (maxX - minX + 1)
								+ ", row " + (Y - minY + 1) + "/"
								+ (maxY - minY + 1) + ")");
				progress.progress(count / (double) size);

				final double lat1 = (Y * tilesizedegrees) - 90;
				final double lon1 = (X * tilesizedegrees) - 180;
				final double lat2 = lat1 + tilesizedegrees;
				final double lon2 = lon1 + tilesizedegrees;

				final File dst = new File(rowDir, GDALUtil.paddedInt(Y, 4)
						+ "_" + GDALUtil.paddedInt(X, 4) + "." + outputExt);
				if (dst.exists())
				{
					progress.getLogger().warning(
							dst.getAbsolutePath() + " already exists");
				}
				else
				{
					try
					{
						if (type == Type.Mapnik)
						{
							Sector s = new Sector(lat1, lon1, lat2, lon2);
							MapnikUtil.tile(s, tilesize, tilesize, mapFile,
									dst, progress.getLogger());
						}
						else
						{
							GDALTile tile = new GDALTile(dataset, tilesize,
									tilesize, lat1, lon1, lat2, lon2, addAlpha,
									band);
							if (type == Type.Elevations)
							{
								tile = tile.convertToType(bufferType);
							}
							if (outsideValues != null)
							{
								tile.fillOutside(outsideValues);
							}
							if (minReplace != null && maxReplace != null
									&& replace != null)
							{
								tile.replaceValues(minReplace, maxReplace,
										replace, otherwise);
							}
							if (type == Type.Elevations)
							{
								ByteBuffer bb = tile.getBuffer();
								bb.rewind();
								FileChannel fc = new RandomAccessFile(dst, "rw")
										.getChannel();
								MappedByteBuffer mbb = fc.map(
										MapMode.READ_WRITE, 0, bb.limit());
								mbb.order(bb.order());
								mbb.put(bb);
								fc.close();
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
				"Tile generation "
						+ (progress.isCancelled() ? "cancelled" : "complete"));
	}
}
