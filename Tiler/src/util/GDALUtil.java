package util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;


public class GDALUtil
{
	static
	{
		gdal.AllRegister();
	}

	private static Logger logger;

	public static void init(Logger logger)
	{
		GDALUtil.logger = logger;
	};

	public static Dataset open(File file)
	{
		try
		{
			Dataset dataset = (Dataset) gdal.Open(file.getAbsolutePath(),
					gdalconst.GA_ReadOnly);
			if (dataset == null)
			{
				printLastError();
			}
			return dataset;
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	public static void printLastError()
	{
		String msg = gdal.GetLastErrorNo() + " - " + gdal.GetLastErrorMsg();
		logger.severe(msg);
	}

	public static Sector getSector(Dataset dataset)
	{
		double[] geoTransformArray = new double[6];
		dataset.GetGeoTransform(geoTransformArray);

		if (geoTransformArray[0] == 0 && geoTransformArray[1] == 0
				&& geoTransformArray[2] == 0 && geoTransformArray[3] == 0
				&& geoTransformArray[4] == 0 && geoTransformArray[5] == 0)
		{
			logger.severe("Dataset contains zeroed geotransform");
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
	
	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
}
