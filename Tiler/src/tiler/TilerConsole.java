package tiler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class TilerConsole
{
	public static void main(String[] args)
	{
		new TilerConsole();
	}

	public TilerConsole()
	{
		File file = new File("D:/SW Margins/sonne/SONNE_100B.ers");
		File outputDir = new File("D:/SW Margins/sonne/tiledB");
		String outputExt = "bil";
		double lztd = 20d;
		int tilesize = 150;
		long nodata = -9999;
		boolean overviews = true;
		int threadCount = 1;
		boolean saveAlpha = false;
		int outputType = gdalconstConstants.GDT_Int16;

		Dataset dataset = GDALTile.open(file);
		Sector sector = getSector(dataset);
		int levels = levelCount(dataset, lztd, sector, tilesize);
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		int bufferTypeSize = 2;

		int level = levels - 1;
		//level = 1;
		double tilesizedegrees = Math.pow(0.5, level) * lztd;
		System.out.println("Calculating level " + level + ", tile size "
				+ tilesizedegrees + " ("
				+ (tilesizedegrees * height / sector.getDeltaLongitude())
				+ " x " + (tilesizedegrees * width / sector.getDeltaLatitude())
				+ ")");

		int minX = getTileX(sector.getMinLongitude(), level, lztd);
		int maxX = getTileX(sector.getMaxLongitude(), level, lztd);
		int minY = getTileY(sector.getMinLatitude(), level, lztd);
		int maxY = getTileY(sector.getMaxLatitude(), level, lztd);

		int size = (maxX - minX + 1) * (maxY - minY + 1);
		int count = 0;
		for (int Y = minY; Y <= maxY; Y++)
		{
			File rowDir = new File(outputDir, String.valueOf(level));
			rowDir = new File(rowDir, paddedInt(Y, 4));
			if (!rowDir.exists())
			{
				rowDir.mkdirs();
			}

			for (int X = minX; X <= maxX; X++)
			{
				count++;
				System.out.println("Tile (" + X + "," + Y + "), " + count + "/"
						+ size + " (" + (count * 100 / size) + "%) (column "
						+ (X - minX + 1) + "/" + (maxX - minX + 1) + ", row "
						+ (Y - minY + 1) + "/" + (maxY - minY + 1) + ")");

				final double lat1 = (Y * tilesizedegrees) - 90;
				final double lon1 = (X * tilesizedegrees) - 180;
				final double lat2 = lat1 + tilesizedegrees;
				final double lon2 = lon1 + tilesizedegrees;

				final File dst = new File(rowDir, paddedInt(Y, 4) + "_"
						+ paddedInt(X, 4) + "." + outputExt);
				if (dst.exists())
				{
					System.out.println(dst.getAbsolutePath()
							+ " already exists.");
				}
				else
				{
					GDALTile tile = new GDALTile(dataset, tilesize, tilesize,
							lat1, lon1, lat2, lon2, saveAlpha);
					tile = tile.convertToType(outputType); //TODO only for bil
					tile.fillNodata(nodata);
					bufferTypeSize = tile.getBufferTypeSize();
					if (outputExt.toLowerCase().equals("bil"))
					{
						try
						{
							ByteBuffer bb = tile.getBuffer();
							bb.rewind();
							FileChannel fc = new RandomAccessFile(dst, "rw")
									.getChannel();
							MappedByteBuffer mbb = fc.map(MapMode.READ_WRITE,
									0, bb.limit());
							mbb.order(bb.order());
							mbb.put(bb);
							fc.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						BufferedImage image = tile.getAsImage();
						try
						{
							ImageIO.write(image, outputExt, dst);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		if (overviews)
		{
			System.out.println();
			System.out.println("Creating overviews...");

			if (outputExt.toLowerCase().equals("bil"))
			{
				Overviewer.createBilOverviews(outputDir, tilesize, tilesize,
						threadCount, bufferTypeSize, ByteOrder.LITTLE_ENDIAN,
						nodata);
			}
			else
			{
				Overviewer.createImageOverviews(outputDir, outputExt, tilesize,
						tilesize, saveAlpha, threadCount);
			}
		}

		System.out.println("Done");
	}

	private static int levelCount(Dataset dataset, double lztd, Sector sector,
			int tilesize)
	{
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		double latPixels = sector.getDeltaLatitude() / height;
		double lonPixels = sector.getDeltaLongitude() / width;
		double texelSize = Math.min(latPixels, lonPixels);
		return (int) Math.ceil(Math.log10(texelSize * tilesize / lztd)
				/ Math.log10(0.5)) + 1;
	}

	private static Sector getSector(Dataset dataset)
	{
		double[] geoTransformArray = new double[6];
		dataset.GetGeoTransform(geoTransformArray);

		if (geoTransformArray[0] == 0 && geoTransformArray[1] == 0
				&& geoTransformArray[2] == 0 && geoTransformArray[3] == 0
				&& geoTransformArray[4] == 0 && geoTransformArray[5] == 0)
		{
			System.err.println("Dataset contains zeroed geotransform");
			return null;
		}

		int width = dataset.getRasterXSize();
		int height = dataset.getRasterYSize();
		//gX = gt[0] + gt[1] * x + gt[2] * y;
		//gY = gt[3] + gt[4] * x + gt[5] * y;
		double minlon = geoTransformArray[0];
		double maxlat = geoTransformArray[3];
		double maxlon = geoTransformArray[0] + geoTransformArray[1] * width
				+ geoTransformArray[2] * height;
		double minlat = geoTransformArray[3] + geoTransformArray[4] * width
				+ geoTransformArray[5] * height;

		String projection = dataset.GetProjectionRef();
		if (projection != null && projection.length() > 0)
		{
			SpatialReference proj = new SpatialReference(projection);
			if (proj != null)
			{
				SpatialReference geog = proj.CloneGeogCS();
				if (geog != null)
				{
					CoordinateTransformation transform = new CoordinateTransformation(
							proj, geog);
					if (transform != null)
					{
						double[] transPoint = new double[3];
						transform.TransformPoint(transPoint, minlon, minlat, 0);
						minlon = transPoint[0];
						minlat = transPoint[1];
						transform.TransformPoint(transPoint, maxlon, maxlat, 0);
						maxlon = transPoint[0];
						maxlat = transPoint[1];
						transform.delete();
					}
					geog.delete();
				}
				proj.delete();
			}
		}

		if (minlat > maxlat)
		{
			double temp = minlat;
			minlat = maxlat;
			maxlat = temp;
		}
		if (minlon > maxlon)
		{
			double temp = minlon;
			minlon = maxlon;
			maxlon = temp;
		}

		return new Sector(minlat, minlon, maxlat, maxlon);
	}

	public static int getTileX(double lon, int layer, double lztsd)
	{
		double layerpow = Math.pow(0.5, layer);
		double X = (lon + 180) / (lztsd * layerpow);
		return (int) X;
	}

	public static int getTileY(double lat, int layer, double lztsd)
	{
		double layerpow = Math.pow(0.5, layer);
		double Y = (lat + 90) / (lztsd * layerpow);
		return (int) Y;
	}

	private String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
}
