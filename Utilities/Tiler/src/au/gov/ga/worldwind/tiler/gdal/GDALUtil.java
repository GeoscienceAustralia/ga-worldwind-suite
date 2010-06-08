package au.gov.ga.worldwind.tiler.gdal;

import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.StringLineBuilder;
import au.gov.ga.worldwind.tiler.util.TilerException;
import au.gov.ga.worldwind.tiler.util.Util;


public class GDALUtil
{
	public static final String GCS_FILE = "gcs.csv";

	static
	{
		gdal.AllRegister();

		File gcs = null;
		String gdaldata = System.getenv("GDAL_DATA");
		if (gdaldata != null)
		{
			File dir = new File(gdaldata);
			if (dir.exists() && dir.isDirectory())
			{
				gcs = new File(dir, GCS_FILE);
			}
		}

		projectionsSupported = (gcs != null && gcs.isFile());
	}

	public static void init()
	{
	}

	private static boolean projectionsSupported;

	public static boolean isProjectionsSupported()
	{
		return projectionsSupported;
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
		// gX = gt[0] + gt[1] * x + gt[2] * y;
		// gY = gt[3] + gt[4] * x + gt[5] * y;
		double minlon = geoTransformArray[0];
		double maxlat = geoTransformArray[3];
		double maxlon = geoTransformArray[0] + geoTransformArray[1] * width
				+ geoTransformArray[2] * height;
		double minlat = geoTransformArray[3] + geoTransformArray[4] * width
				+ geoTransformArray[5] * height;

		if (isProjectionsSupported())
		{
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
							transform.TransformPoint(transPoint, minlon,
									minlat, 0);
							minlon = transPoint[0];
							minlat = transPoint[1];
							transform.TransformPoint(transPoint, maxlon,
									maxlat, 0);
							maxlon = transPoint[0];
							maxlat = transPoint[1];
							transform.delete();
						}
						geog.delete();
					}
					proj.delete();
				}
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

	public static String getInfoText(Dataset dataset, Sector sector)
	{
		int width = dataset.getRasterXSize();
		int height = dataset.getRasterYSize();
		int bandCount = dataset.getRasterCount();
		String projection = dataset.GetProjection();
		SpatialReference spatialReference = (projection == null
				|| projection.length() == 0 || !GDALUtil
				.isProjectionsSupported()) ? null : new SpatialReference(
				projection);
		String[] dataTypes = new String[bandCount];
		int[] dataTypeSizes = new int[bandCount];
		Double[] nodata = new Double[bandCount];
		double[] min = new double[bandCount];
		double[] max = new double[bandCount];
		for (int i = 0; i < bandCount; i++)
		{
			Band band = dataset.GetRasterBand(i + 1);
			int dataType = band.getDataType();
			dataTypes[i] = gdal.GetDataTypeName(dataType);
			dataTypeSizes[i] = gdal.GetDataTypeSize(dataType);

			Double[] nodataValue = new Double[1];
			band.GetNoDataValue(nodataValue);
			nodata[i] = nodataValue[0];

			double[] minmax = new double[2];
			band.ComputeRasterMinMax(minmax, 1);
			min[i] = minmax[0];
			max[i] = minmax[1];
		}

		StringLineBuilder info = new StringLineBuilder();

		info.appendLine("Dataset information:");
		info.appendLine("Size = " + width + ", " + height);
		info.appendLine("Cell size = " + (sector.getDeltaLongitude() / width)
				+ ", " + (sector.getDeltaLatitude() / height));
		info.appendLine("Bottom left corner = (" + sector.getMinLongitude()
				+ ", " + sector.getMinLatitude() + ")");
		info.appendLine("Top right corner = (" + sector.getMaxLongitude()
				+ ", " + sector.getMaxLatitude() + ")");
		info.appendLine("Raster band count = " + bandCount);
		for (int i = 0; i < bandCount; i++)
		{
			info.appendLine("Band " + (i + 1) + ":");
			info.appendLine("    Data type = " + dataTypes[i] + " ("
					+ dataTypeSizes[i] + " bit)");
			info.appendLine("    No-data value = " + nodata[i]);
			info.appendLine("    Approx minimum = " + min[i]);
			info.appendLine("    Approx maximum = " + max[i]);
		}
		if (spatialReference != null)
		{
			info.appendLine("Coordinate system =");
			info.appendLine(Util.fixNewlines(spatialReference
					.ExportToPrettyWkt()));
		}

		return info.toString(true);
	}

	public static String getTileText(Dataset dataset, Sector sector,
			double lzts, int levels, boolean overviews)
	{
		StringLineBuilder info = new StringLineBuilder();

		int[] tileCount = new int[levels];
		int totalCount = 0;

		for (int i = overviews ? 0 : levels - 1; i < levels; i++)
		{
			tileCount[i] = Util.tileCount(sector, i, lzts);
			totalCount += tileCount[i];
		}

		info.appendLine("Tiling information:");
		info.appendLine("Level count = " + levels);
		if (overviews)
		{
			info.appendLine("Tile count at highest level = "
					+ tileCount[levels - 1]);
			info.appendLine("Overview tile count = "
					+ (totalCount - tileCount[levels - 1]));
		}
		info.appendLine("Total tile count = " + totalCount);

		return info.toString(true);
	}
}
