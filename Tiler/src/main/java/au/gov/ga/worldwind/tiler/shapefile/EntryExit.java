/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
			return -compare(entry, o.entry);

		double tX = coordinate.x - tileCentroid.x;
		double tY = coordinate.y - tileCentroid.y;
		double oX = o.coordinate.x - tileCentroid.x;
		double oY = o.coordinate.y - tileCentroid.y;

		//negative atan2 for clockwise direction
		double ta = -Math.atan2(tY, tX);
		double oa = -Math.atan2(oY, oX);

		return ta < oa ? -1 : ta == oa ? 0 : 1;
	}

	/**
	 * Compare two booleans, copied from
	 * {@link Boolean#compare(boolean, boolean)} due to this function only being
	 * added in Java 1.7.
	 */
	private static int compare(boolean x, boolean y)
	{
		return (x == y) ? 0 : (x ? 1 : -1);
	}

	@Override
	public String toString()
	{
		return (entry ? "Entry" : "Exit") + " " + coordinate;
	}
}
