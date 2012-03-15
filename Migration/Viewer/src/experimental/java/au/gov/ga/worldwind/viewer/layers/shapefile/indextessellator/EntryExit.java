package au.gov.ga.worldwind.viewer.layers.shapefile.indextessellator;

import gov.nasa.worldwind.geom.LatLon;

public class EntryExit implements Comparable<EntryExit>
{
	public final LatLon latlon;
	private final LatLon tileCentroid;
	private boolean entry;

	public EntryExit(LatLon latlon, LatLon tileCentroid, boolean entry)
	{
		if (latlon == null)
			throw new NullPointerException();

		this.latlon = latlon;
		this.tileCentroid = tileCentroid;
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
	public int compareTo(EntryExit o)
	{
		//point comparison is done via clockwise angle
		//whatever is more clockwise from sector center is 'greater'

		if (o == null)
			throw new NullPointerException();

		if (latlon.equals(o.latlon))
			return 0;

		double tX = latlon.longitude.degrees - tileCentroid.longitude.degrees;
		double tY = latlon.latitude.degrees - tileCentroid.latitude.degrees;
		double oX = o.latlon.longitude.degrees - tileCentroid.longitude.degrees;
		double oY = o.latlon.latitude.degrees - tileCentroid.latitude.degrees;

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
