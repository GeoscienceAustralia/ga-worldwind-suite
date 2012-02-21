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


public class Tiler
{
	public enum TilingType
	{
		Images,
		Elevations,
		Mapnik
	}

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

		int size = (maxX - minX + 1) * (maxY - minY + 1);
		int count = 0;
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
								+ "%) (column " + (X - minX + 1) + "/" + (maxX - minX + 1) + ", row " + (Y - minY + 1)
								+ "/" + (maxY - minY + 1) + ")");
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
							parameters.ignoreBlank = ignoreBlank;

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
								if (!(parameters.ignoreBlank && tile.isBlank()))
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
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(jpegQuality);
			writer.setOutput(new FileImageOutputStream(file));
			IIOImage iioimage = new IIOImage(image, null, null);
			writer.write(null, iioimage, iwp);
		}
		else
		{
			ImageIO.write(image, format, file);
		}
	}
}
