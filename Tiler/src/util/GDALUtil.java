package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import util.FileFilters.ExtensionFileFilter;


public class GDALUtil
{
	static
	{
		gdal.AllRegister();
	}

	public static void init()
	{
	}

	public static Dataset open(File file) throws GDALException
	{
		Dataset dataset = (Dataset) gdal.Open(file.getAbsolutePath(),
				gdalconst.GA_ReadOnly);
		if (dataset == null)
		{
			throw new GDALException();
		}
		return dataset;
	}

	public static Sector getSector(Dataset dataset) throws TilerException
	{
		double[] geoTransformArray = new double[6];
		dataset.GetGeoTransform(geoTransformArray);

		if (geoTransformArray[0] == 0 && geoTransformArray[1] == 0
				&& geoTransformArray[2] == 0 && geoTransformArray[3] == 0
				&& geoTransformArray[4] == 0 && geoTransformArray[5] == 0)
		{
			throw new TilerException("Dataset contains zeroed geotransform");
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

	public static int levelCount(Dataset dataset, double lztd, Sector sector,
			int tilesize)
	{
		double width = dataset.getRasterXSize();
		double height = dataset.getRasterYSize();
		double lonPixels = sector.getDeltaLongitude() / width;
		double latPixels = sector.getDeltaLatitude() / height;
		double texelSize = Math.min(latPixels, lonPixels);
		return (int) Math.ceil(Math.log10(texelSize * tilesize / lztd)
				/ Math.log10(0.5)) + 1;
	}

	public static int tileCount(Sector sector, int level, double lztsd)
	{
		int minX = GDALUtil.getTileX(sector.getMinLongitude(), level, lztsd);
		int maxX = GDALUtil.getTileX(sector.getMaxLongitude(), level, lztsd);
		int minY = GDALUtil.getTileY(sector.getMinLatitude(), level, lztsd);
		int maxY = GDALUtil.getTileY(sector.getMaxLatitude(), level, lztsd);
		return (maxX - minX + 1) * (maxY - minY + 1);
	}

	public static int getTileX(double longitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude + 180) / (lztsd * layerpow);
		return (int) X;
	}

	public static int getTileY(double latitude, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude + 90) / (lztsd * layerpow);
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

	public static void main(String[] args)
	{
		short[] minmax = findShortMinMax(new File(
				"D:/SW Margins/sonne/tiledB/8"), (short) -9999,
				Short.MIN_VALUE, (short) -1);
		System.out.println("Min = " + minmax[0]);
		System.out.println("Max = " + minmax[1]);
	}

	public static short[] findShortMinMax(File dir, short nodataValue,
			short ignoreMin, short ignoreMax)
	{
		short min = Short.MAX_VALUE;
		short max = Short.MIN_VALUE;

		String extension = "bil";
		ExtensionFileFilter fileFilter = new ExtensionFileFilter(extension);
		List<File> sourceFiles = new ArrayList<File>();
		FileUtil.recursivelyAddFiles(sourceFiles, dir, fileFilter);

		int count = sourceFiles.size(), done = 0;
		for (File file : sourceFiles)
		{
			System.out.println("Processing " + file + " (" + (++done) + "/"
					+ count + " - " + (done * 100 / count) + "%) " + min + ","
					+ max);
			try
			{
				FileChannel fc = new FileInputStream(file).getChannel();
				ByteBuffer bb = ByteBuffer.allocate((int) file.length());
				bb.order(ByteOrder.LITTLE_ENDIAN);
				fc.read(bb);
				bb.rewind();
				ShortBuffer sb = bb.asShortBuffer();
				for (int i = 0; i < sb.limit(); i++)
				{
					short current = sb.get();
					if (current == nodataValue)
						continue;
					if (current < ignoreMin || current > ignoreMax)
						continue;
					min = current < min ? current : min;
					max = current > max ? current : max;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return new short[] { min, max };
	}
}
