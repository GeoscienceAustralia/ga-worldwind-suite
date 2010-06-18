package au.gov.ga.worldwind.layers.shapefile.tessellator;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

public class EntryExitLatLon implements Comparable<EntryExitLatLon>
{
	public final LatLon latlon;
	private final Sector sector;
	private boolean entry;

	public EntryExitLatLon(LatLon latlon, Sector sector, boolean entry)
	{
		if (latlon == null)
			throw new NullPointerException();

		this.latlon = latlon;
		this.sector = sector;
		this.entry = entry;
	}

	public boolean isEntry()
	{
		return entry;
	}

	public boolean isExit()
	{
		return !entry;
	}

	@Override
	public int compareTo(EntryExitLatLon o)
	{
		//point comparison is done via clockwise angle
		//whatever is more clockwise from sector center is 'greater'

		if (o == null)
			throw new NullPointerException();

		if (latlon.equals(o.latlon))
			return 0;

		LatLon center = sector.getCentroid();
		double tX = latlon.longitude.degrees - center.longitude.degrees;
		double tY = latlon.latitude.degrees - center.latitude.degrees;
		double oX = o.latlon.longitude.degrees - center.longitude.degrees;
		double oY = o.latlon.latitude.degrees - center.latitude.degrees;

		//negative atan2 for clockwise direction
		double ta = -Math.atan2(tY, tX);
		double oa = -Math.atan2(oY, oX);

		return ta < oa ? -1 : ta == oa ? 0 : 1;
	}

	@Override
	public String toString()
	{
		return (entry ? "Entry" : "Exit") + " " + latlon;
	}
}
