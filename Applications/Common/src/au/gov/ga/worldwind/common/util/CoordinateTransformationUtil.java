package au.gov.ga.worldwind.common.util;

import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class CoordinateTransformationUtil
{
	public static CoordinateTransformation getTransformationToWGS84(String wktOrEpsgOrProj4)
	{
		SpatialReference src = stringToSpatialReference(wktOrEpsgOrProj4);
		if (src == null)
		{
			return null;
		}

		SpatialReference dst = new SpatialReference();
		dst.ImportFromEPSG(4326);

		return new CoordinateTransformation(src, dst);
	}

	public static SpatialReference stringToSpatialReference(String s)
	{
		if (s == null)
		{
			return null;
		}

		s = s.trim();

		//remove EPSG: from the front if it exists
		if (s.toLowerCase().startsWith("epsg:"))
		{
			s = s.substring(5);
		}

		//first try a single integer (assume it is an EPSG code)
		try
		{
			int intValue = Integer.parseInt(s);
			SpatialReference reference = new SpatialReference();
			reference.ImportFromEPSG(intValue);
			return reference;
		}
		catch (NumberFormatException e)
		{
			//ignore
		}

		//check for proj4 format
		if (s.startsWith("+"))
		{
			SpatialReference reference = new SpatialReference();
			reference.ImportFromProj4(s);
			return reference;
		}

		//assume wkt format
		return new SpatialReference(s);
	}
}
