package au.gov.ga.worldwind.tiler.shapefile;

import gistoolkit.features.Point;
import au.gov.ga.worldwind.tiler.util.Sector;

public class EntryExitPoint implements Comparable<EntryExitPoint>
{
	public final Point point;
	private final Sector sector;
	private boolean entry;

	public EntryExitPoint(Point point, Sector sector, boolean entry)
	{
		if (point == null)
			throw new NullPointerException();

		this.point = point;
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
	public int compareTo(EntryExitPoint o)
	{
		//point comparison is done via clockwise angle
		//whatever is more clockwise from sector center is 'greater'

		if (o == null)
			throw new NullPointerException();

		if (point.equals(o.point))
			return 0;

		double tX = point.x - sector.getCenterLongitude();
		double tY = point.y - sector.getCenterLatitude();
		double oX = o.point.x - sector.getCenterLongitude();
		double oY = o.point.y - sector.getCenterLatitude();

		//negative atan2 for clockwise direction
		double ta = -Math.atan2(tY, tX);
		double oa = -Math.atan2(oY, oX);

		return ta < oa ? -1 : ta == oa ? 0 : 1;
	}
}
