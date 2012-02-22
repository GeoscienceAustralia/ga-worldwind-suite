package au.gov.ga.worldwind.tiler.shapefile;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents an entry or an exit point of a piece of shapefile geometry
 * into/out of a shapefile tile. Implements the {@link Comparable} interface,
 * which supports sorting entry/exit points in a clockwise direction.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EntryExit implements Comparable<EntryExit>
{
	/**
	 * Entry/exit point's coordinate
	 */
	public final Coordinate coordinate;
	private final Coordinate tileCentroid;
	private boolean entry;

	public EntryExit(Coordinate coordinate, Coordinate tileCentroid, boolean entry)
	{
		if (coordinate == null)
			throw new NullPointerException();

		this.coordinate = coordinate;
		this.tileCentroid = tileCentroid;
		this.entry = entry;
	}

	/**
	 * @return Is this point an entry point?
	 */
	public boolean isEntry()
	{
		return entry;
	}

	/**
	 * @return Is this point an exit point?
	 */
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

		if (coordinate.equals(o.coordinate))
			return 0;

		double tX = coordinate.x - tileCentroid.x;
		double tY = coordinate.y - tileCentroid.y;
		double oX = o.coordinate.x - tileCentroid.x;
		double oY = o.coordinate.y - tileCentroid.y;

		//negative atan2 for clockwise direction
		double ta = -Math.atan2(tY, tX);
		double oa = -Math.atan2(oY, oX);

		return ta < oa ? -1 : ta == oa ? 0 : 1;
	}

	@Override
	public String toString()
	{
		return (entry ? "Entry" : "Exit") + " " + coordinate;
	}
}
