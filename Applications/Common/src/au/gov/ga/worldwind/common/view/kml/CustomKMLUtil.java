package au.gov.ga.worldwind.common.view.kml;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLLocation;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;

public class CustomKMLUtil extends KMLUtil
{
	public static void getPositions(Globe globe, KMLAbstractGeometry geometry, java.util.List<Position> positions)
	{
		KMLUtil.getPositions(globe, geometry, positions);
		if (positions.isEmpty() && geometry instanceof KMLModel)
		{
			positions.add(locationToPosition(((KMLModel) geometry).getLocation()));
		}
	}

	public static Position locationToPosition(KMLLocation location)
	{
		if (location == null)
			return null;

		Double lat = location.getLatitude(), lon = location.getLongitude(), alt = location.getAltitude();
		return new Position(Angle.fromDegrees(lat != null ? lat : 0), Angle.fromDegrees(lon != null ? lon : 0),
				alt != null ? alt : 0);
	}
}
