package au.gov.ga.worldwind.viewer.layers.shapefile.indextessellator;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ga.worldwind.common.util.Util;

public class SubTile
{
	public final int x;
	public final int y;

	public final IndexedLatLon minmin;
	public final IndexedLatLon minmax;
	public final IndexedLatLon maxmax;
	public final IndexedLatLon maxmin;
	public final LatLon centroid;

	private final List<IndexedContour> contours = new ArrayList<IndexedContour>();
	private IndexedContour current = null;
	private int startPointsIndex = 0;
	private boolean noEntryShapeExists = false;
	private boolean filled = false;

	public SubTile(int x, int y, IndexedLatLon minmin, IndexedLatLon minmax, IndexedLatLon maxmax,
			IndexedLatLon maxmin)
	{
		this.x = x;
		this.y = y;
		this.minmin = minmin;
		this.minmax = minmax;
		this.maxmax = maxmax;
		this.maxmin = maxmin;
		centroid =
				LatLon.fromDegrees(0.5 * (minmin.latitude.degrees + maxmax.latitude.degrees),
						0.5 * (minmin.longitude.degrees + maxmax.longitude.degrees));
	}

	public List<IndexedContour> getContours()
	{
		return contours;
	}

	public void addVertex(int shapeId, int vertexIndex, boolean entry, boolean exit)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entry || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = contours.size();

			current = new IndexedContour(shapeId, entry);
			contours.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entry)
				noEntryShapeExists = true;
		}

		if (exit)
		{
			current.exited = true;
		}

		//add the vertex index
		current.indices.add(vertexIndex);
	}

	public void joinOrphanPolygons(int shapeId)
	{
		//Polygons must either be fully contained in the tile, or both enter AND exit.
		//If a pointset enters but not exits, there MUST be another pointset that exits
		//but not enters. Join these two pointsets (they will share a end point).

		if (noEntryShapeExists)
		{
			int noExitIndex = -1, noEntryIndex = -1;
			for (int i = startPointsIndex; i < contours.size(); i++)
			{
				IndexedContour p = contours.get(i);
				if (p.entered != p.exited)
				{
					if (!p.entered)
						noEntryIndex = i;
					else if (!p.exited)
						noExitIndex = i;
				}

				if (noEntryIndex >= 0 && noExitIndex >= 0)
					break;
			}

			if (noEntryIndex >= 0 && noExitIndex >= 0)
			{
				IndexedContour noEntry = contours.get(noEntryIndex);
				IndexedContour noExit = contours.get(noExitIndex);

				//check if the two pointsets share a point
				int i1 = noEntry.indices.get(0);
				int i2 = noExit.indices.get(noExit.indices.size() - 1);
				int start = 1;
				if (i1 != i2)
				{
					String message = "First entry point doesn't join up with last exit point";
					Logging.logger().warning(message);
					start = 0;
				}

				//add all noEntry's points (except the shared point) to the noExit
				//pointset (so that it has an exit), and then remove the noEntry pointset
				for (int i = start; i < noEntry.indices.size(); i++)
				{
					noExit.indices.add(noEntry.indices.get(i));
				}
				contours.remove(noEntryIndex);
				noExit.exited = true;
			}

			//calling this function twice shouldn't have any affect; set flag to false
			noEntryShapeExists = false;
		}
	}

	public void markFilled()
	{
		filled = true;
	}

	public boolean contains(LatLon latlon)
	{
		return containsDegrees(latlon.latitude.degrees, latlon.longitude.degrees);
	}

	public boolean containsDegrees(double lat, double lon)
	{
		return lat >= this.minmin.latitude.degrees && lat <= this.maxmax.latitude.degrees
				&& lon >= this.minmin.longitude.degrees && lon <= this.maxmax.longitude.degrees;
	}

	public LatLon limitLatLonWithinTile(LatLon latlon)
	{
		double lat =
				Util.clamp(latlon.latitude.degrees, minmin.latitude.degrees,
						maxmax.latitude.degrees);
		double lon =
				Util.clamp(latlon.longitude.degrees, minmin.longitude.degrees,
						maxmax.longitude.degrees);

		return LatLon.fromDegrees(lat, lon);
	}

	public double deltaLatitudeDegrees()
	{
		return maxmax.latitude.degrees - minmin.latitude.degrees;
	}

	public double deltaLongitudeDegrees()
	{
		return maxmax.longitude.degrees - minmin.longitude.degrees;
	}

	public double latitudeFract(LatLon latlon)
	{
		return (latlon.latitude.degrees - minmin.latitude.degrees) / deltaLatitudeDegrees();
	}

	public double longitudeFract(LatLon latlon)
	{
		return (latlon.longitude.degrees - minmin.longitude.degrees) / deltaLongitudeDegrees();
	}

	public void completePolygons(List<IndexedLatLon> vertices)
	{
		Map<EntryExit, IndexedContour> pointMap = new HashMap<EntryExit, IndexedContour>();
		Map<IndexedContour, EntryExit> reverseExitMap = new HashMap<IndexedContour, EntryExit>();

		List<EntryExit> entryExits = new ArrayList<EntryExit>();
		List<EntryExit> entries = new ArrayList<EntryExit>();
		List<EntryExit> exits = new ArrayList<EntryExit>();

		for (IndexedContour c : contours)
		{
			if (c.entered && c.exited)
			{
				LatLon en = vertices.get(c.indices.get(0));
				LatLon ex = vertices.get(c.indices.get(c.indices.size() - 1));

				EntryExit entry = new EntryExit(en, centroid, true);
				EntryExit exit = new EntryExit(ex, centroid, false);

				pointMap.put(entry, c);
				pointMap.put(exit, c);
				reverseExitMap.put(c, exit);

				entryExits.add(entry);
				entryExits.add(exit);
				entries.add(entry);
				exits.add(exit);
			}
		}

		if (entries.size() != exits.size())
			throw new IllegalStateException("Number of entries doesn't equal number of exits");

		//no polygons have entered/exited this tile's sector's edges
		if (entries.isEmpty())
		{
			//if this tile's sector is inside a polygon, then fill the entire sector
			//(if it is inside a polygon, but there are some entry/exits, then the code
			//below will fill the sector properly)
			if (filled)
			{
				fillSector(vertices);
			}
			return;
		}

		//sort the list and ensure entry's/exit's alternate
		Collections.sort(entryExits);
		Collections.sort(entries);
		Collections.sort(exits);

		int offset = 0;
		//because of the checks above, we are sure that entryExits list contains at least 2 items
		EntryExit previous = entryExits.get(entryExits.size() - 2);
		EntryExit current = entryExits.get(entryExits.size() - 1);
		for (int i = 0; i < entryExits.size(); i++)
		{
			EntryExit next = entryExits.get(i);
			if (current.isExit() && next.isEntry() && !current.latlon.equals(next.latlon)
					&& !(previous.isEntry() && current.latlon.equals(previous.latlon)))
			{
				offset = entries.indexOf(next) - exits.indexOf(current);
				break;
			}

			previous = current;
			current = next;
		}

		for (int i = 0; i < entries.size(); i++)
		{
			EntryExit exit = exits.get(i);
			EntryExit entry = entries.get((i + offset + entries.size()) % entries.size());

			//join curves from exit to entry

			IndexedContour exitContour = pointMap.remove(exit);
			IndexedContour entryContour = pointMap.remove(entry);

			//add any required sector corner points clockwise from exit to entry
			addCornerPoints(exitContour, entry.latlon, exit.latlon, vertices);

			if (exitContour == entryContour)
			{
				//same pointset, so do nothing
			}
			else
			{
				//add all points from entry points to the start of the exit points
				exitContour.indices.addAll(entryContour.indices);
				//remove the entry points from the points list
				contours.remove(entryContour);

				//because we removed entryContour from the list, exitContour now becomes
				//the mapped value for the entryContour's exit point
				EntryExit entrysExit = reverseExitMap.get(entryContour);
				pointMap.put(entrysExit, exitContour);
				reverseExitMap.put(exitContour, entrysExit);
			}
		}
	}

	private void fillSector(List<IndexedLatLon> vertices)
	{
		IndexedContour current = new IndexedContour(-1, false);
		contours.add(current);
		current.exited = false;
		//add corners backwards (clockwise)

		minmin.indexIfNot(vertices);
		maxmin.indexIfNot(vertices);
		maxmax.indexIfNot(vertices);
		minmax.indexIfNot(vertices);

		current.indices.add(minmin.getIndex());
		current.indices.add(maxmin.getIndex());
		current.indices.add(maxmax.getIndex());
		current.indices.add(minmax.getIndex());
	}

	private void addCornerPoints(IndexedContour addTo, LatLon entry, LatLon exit,
			List<IndexedLatLon> vertices)
	{
		if (entry.equals(exit))
			return;

		double enX = entry.longitude.degrees - centroid.longitude.degrees;
		double enY = entry.latitude.degrees - centroid.latitude.degrees;
		double exX = exit.longitude.degrees - centroid.longitude.degrees;
		double exY = exit.latitude.degrees - centroid.latitude.degrees;

		//negative atan2 for clockwise direction
		double entryAngle = -Math.atan2(enY, enX);
		double exitAngle = -Math.atan2(exY, exX);

		double twoPI = Math.PI * 2d; //360 degrees
		double halfPI = Math.PI * 0.5d; //90 degrees
		double clockwiseDelta = (twoPI + entryAngle - exitAngle) % twoPI;

		double diff90 = (exitAngle + twoPI - Math.PI / 4d) % halfPI;
		int addCount = (int) ((clockwiseDelta + diff90) / halfPI);
		int firstCorner = (int) (((exitAngle - diff90 + twoPI) % twoPI) / halfPI);

		IndexedLatLon[] clockwise = new IndexedLatLon[] { minmin, maxmin, maxmax, minmax };

		for (int i = firstCorner; i < addCount + firstCorner; i++)
		{
			IndexedLatLon ill = clockwise[i % 4];
			ill.indexIfNot(vertices);
			addTo.indices.add(ill.getIndex());
		}
	}
}
