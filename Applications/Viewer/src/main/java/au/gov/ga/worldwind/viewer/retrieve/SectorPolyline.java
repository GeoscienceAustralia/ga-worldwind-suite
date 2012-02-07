package au.gov.ga.worldwind.viewer.retrieve;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;

import java.util.ArrayList;
import java.util.List;

public class SectorPolyline extends Polyline
{
	private final Sector sector;

	public SectorPolyline(Sector sector)
	{
		this.sector = sector;

		List<LatLon> latlons = new ArrayList<LatLon>();
		latlons.add(new LatLon(sector.getMinLatitude(), sector.getMinLongitude()));
		latlons.add(new LatLon(sector.getMinLatitude(), sector.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector.getMinLongitude()));
		setPositions(latlons, 0);
		setFollowTerrain(true);
		setClosed(true);
		setPathType(RHUMB_LINE);
	}

	@Override
	public void render(DrawContext dc)
	{
		try
		{
			super.render(dc);
		}
		catch (NullPointerException e)
		{
			// catch bug in Position.interpolate
			boolean followTerrain = isFollowTerrain();
			setFollowTerrain(false);
			super.render(dc);
			setFollowTerrain(followTerrain);
		}
	}

	public Sector getSector()
	{
		return sector;
	}
}