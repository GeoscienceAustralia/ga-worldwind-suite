package retrieve;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.Polyline;

import java.util.ArrayList;
import java.util.List;

public class SectorPolyline extends Polyline
{
	public SectorPolyline(Sector sector)
	{
		List<LatLon> latlons = new ArrayList<LatLon>();
		latlons.add(new LatLon(sector.getMinLatitude(), sector
				.getMinLongitude()));
		latlons.add(new LatLon(sector.getMinLatitude(), sector
				.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector
				.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector
				.getMinLongitude()));
		setPositions(latlons, 0);
		setFollowTerrain(true);
		setClosed(true);
		setPathType(RHUMB_LINE);
	}
}
