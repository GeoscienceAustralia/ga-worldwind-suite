package au.gov.ga.worldwind.viewer.layers.shapefile.tessellator;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubTile
{
	public final Sector sector;
	public final int x;
	public final int y;

	private List<SubTileContour> contours = new ArrayList<SubTileContour>();

	private SubTileContour current = null;
	private int startPointsIndex;
	private boolean noEntryShapeExists = false;
	private boolean sectorFilled = false;

	public SubTile(Sector sector, int x, int y)
	{
		this.sector = sector;
		this.x = x;
		this.y = y;
	}

	public List<SubTileContour> getContours()
	{
		return contours;
	}

	public void enterShape(int shapeId, LatLon outsidePoint, LatLon insidePoint)
	{
		updateCurrentShapeIfRequired(shapeId, true);

		LatLon edge = edgePoint(insidePoint, outsidePoint, sector);
		current.points.add(edge);
		current.points.add(insidePoint);
	}

	public void continueShape(int shapeId, LatLon insidePoint)
	{
		updateCurrentShapeIfRequired(shapeId, false);

		current.points.add(insidePoint);
	}

	public void exitShape(int shapeId, LatLon insidePoint, LatLon outsidePoint)
	{
		LatLon edge = edgePoint(insidePoint, outsidePoint, sector);
		current.points.add(edge);
		current.exited = true;
	}

	public boolean crossShape(int shapeId, LatLon fromPoint, LatLon toPoint)
	{
		LatLon[] corners = sector.getCorners();
		LatLon minXminY = corners[0];
		LatLon maxXminY = corners[1];
		LatLon maxXmaxY = corners[2];
		LatLon minXmaxY = corners[3];

		LatLon minXintersect = lineSegmentIntersection(fromPoint, toPoint, minXminY, minXmaxY);
		LatLon minYintersect = lineSegmentIntersection(fromPoint, toPoint, minXminY, maxXminY);
		LatLon maxXintersect = lineSegmentIntersection(fromPoint, toPoint, maxXminY, maxXmaxY);
		LatLon maxYintersect = lineSegmentIntersection(fromPoint, toPoint, minXmaxY, maxXmaxY);
		LatLon[] allIntersections =
				new LatLon[] { minXintersect, minYintersect, maxXintersect, maxYintersect };

		LatLon[] intersections = new LatLon[2];
		int count = 0;
		for (LatLon ll : allIntersections)
		{
			if (ll != null)
			{
				if (count < 2)
					intersections[count] = ll;
				count++;
			}
		}

		//if more or less than two intersections were found, then the line either
		//doesn't cross this shape or something strange has happened, so return false
		if (count != 2)
			return false;

		if (distanceSquared(fromPoint, intersections[0]) > distanceSquared(fromPoint,
				intersections[1]))
		{
			LatLon temp = intersections[0];
			intersections[0] = intersections[1];
			intersections[1] = temp;
		}

		updateCurrentShapeIfRequired(shapeId, true);
		current.points.add(intersections[0]);
		current.points.add(intersections[1]);
		current.exited = true;
		return true;
	}

	private void updateCurrentShapeIfRequired(int shapeId, boolean entered)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entered || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = contours.size();

			current = new SubTileContour(shapeId, entered);
			contours.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entered)
				noEntryShapeExists = true;
		}
	}

	public void markFilled()
	{
		sectorFilled = true;
	}

	private void fillSector()
	{
		SubTileContour current = new SubTileContour(-1, false);
		contours.add(current);
		current.exited = false;
		LatLon[] corners = sector.getCorners();
		//add corners backwards (clockwise)
		for (int i = 3; i >= 0; i--)
			current.points.add(corners[i]);
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
				SubTileContour p = contours.get(i);
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
				SubTileContour noEntry = contours.get(noEntryIndex);
				SubTileContour noExit = contours.get(noExitIndex);

				//check if the two pointsets share a point
				LatLon p1 = noEntry.points.get(0);
				LatLon p2 = noExit.points.get(noExit.points.size() - 1);
				int start;
				if (p1.equals(p2))
				{
					start = 1;
				}
				else
				{
					String message = "First entry point doesn't join up with last exit point";
					Logging.logger().warning(message);
					start = 0;
				}

				//add all noEntry's points (except the shared point) to the noExit
				//pointset (so that it has an exit), and then remove the noEntry pointset
				for (int i = start; i < noEntry.points.size(); i++)
				{
					noExit.points.add(noEntry.points.get(i));
				}
				contours.remove(noEntryIndex);
				noExit.exited = true;
			}

			//calling this function twice shouldn't have any affect; set flag to false
			noEntryShapeExists = false;
		}
	}

	public void completePolygons()
	{
		Map<EntryExitLatLon, SubTileContour> pointMap =
				new HashMap<EntryExitLatLon, SubTileContour>();
		Map<SubTileContour, EntryExitLatLon> reverseExitMap =
				new HashMap<SubTileContour, EntryExitLatLon>();

		List<EntryExitLatLon> entryExits = new ArrayList<EntryExitLatLon>();
		List<EntryExitLatLon> entries = new ArrayList<EntryExitLatLon>();
		List<EntryExitLatLon> exits = new ArrayList<EntryExitLatLon>();

		for (SubTileContour p : contours)
		{
			if (p.entered && p.exited)
			{
				LatLon en = p.points.get(0);
				LatLon ex = p.points.get(p.points.size() - 1);

				EntryExitLatLon entry = new EntryExitLatLon(en, sector, true);
				EntryExitLatLon exit = new EntryExitLatLon(ex, sector, false);

				pointMap.put(entry, p);
				pointMap.put(exit, p);
				reverseExitMap.put(p, exit);

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
			if (sectorFilled)
			{
				fillSector();
			}
			return;
		}

		//sort the list and ensure entry's/exit's alternate
		Collections.sort(entryExits);
		Collections.sort(entries);
		Collections.sort(exits);

		int offset = 0;
		//because of the checks above, we are sure that entryExits list contains at least 2 items
		EntryExitLatLon previous = entryExits.get(entryExits.size() - 2);
		EntryExitLatLon current = entryExits.get(entryExits.size() - 1);
		for (int i = 0; i < entryExits.size(); i++)
		{
			EntryExitLatLon next = entryExits.get(i);
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
			EntryExitLatLon exit = exits.get(i);
			EntryExitLatLon entry = entries.get((i + offset + entries.size()) % entries.size());

			//join curves from exit to entry

			SubTileContour exitContour = pointMap.remove(exit);
			SubTileContour entryContour = pointMap.remove(entry);

			//add any required sector corner points clockwise from exit to entry
			addCornerPoints(exitContour, entry.latlon, exit.latlon, sector);

			if (exitContour == entryContour)
			{
				//same pointset, so do nothing
			}
			else
			{
				//add all points from entry points to the start of the exit points
				exitContour.points.addAll(entryContour.points);
				//remove the entry points from the points list
				contours.remove(entryContour);

				//because we removed entryPoints from the list, exitPoints now becomes
				//the mapped value for the entryPoints' exit point
				EntryExitLatLon entrysExit = reverseExitMap.get(entryContour);
				pointMap.put(entrysExit, exitContour);
				reverseExitMap.put(exitContour, entrysExit);
			}
		}
	}

	private static LatLon lineSegmentIntersection(LatLon p1, LatLon p2, LatLon p3, LatLon p4)
	{
		double p1x = p1.longitude.degrees;
		double p1y = p1.latitude.degrees;
		double p2x = p2.longitude.degrees;
		double p2y = p2.latitude.degrees;
		double p3x = p3.longitude.degrees;
		double p3y = p3.latitude.degrees;
		double p4x = p4.longitude.degrees;
		double p4y = p4.latitude.degrees;

		double ud = (p4y - p3y) * (p2x - p1x) - (p4x - p3x) * (p2y - p1y);

		//lines are parallel (don't care about coincident lines)
		if (ud == 0)
			return null;

		double uan = (p4x - p3x) * (p1y - p3y) - (p4y - p3y) * (p1x - p3x);
		double ubn = (p2x - p1x) * (p1y - p3y) - (p2y - p1y) * (p1x - p3x);
		double ua = uan / ud;
		double ub = ubn / ud;

		//intersection is not within the line segments
		if (ua < 0 || 1 < ua || ub < 0 || 1 < ub)
			return null;

		double x = p1x + ua * (p2x - p1x);
		double y = p1y + ua * (p2y - p1y);
		return LatLon.fromDegrees(y, x);
	}

	private static double distanceSquared(LatLon p1, LatLon p2)
	{
		double delta;
		double result = 0d;
		delta = p1.latitude.degrees - p2.latitude.degrees;
		result += delta * delta;
		delta = p1.longitude.degrees - p2.longitude.degrees;
		result += delta * delta;
		return result;
	}

	/**
	 * Returns a point on the edge of the sector that exists on the line between
	 * inside and outside. Inside must be inside the sector.
	 */
	private static LatLon edgePoint(LatLon inside, LatLon outside, Sector sector)
	{
		LatLon[] corners = sector.getCorners();
		LatLon minXminY = corners[0];
		LatLon maxXminY = corners[1];
		LatLon maxXmaxY = corners[2];
		LatLon minXmaxY = corners[3];

		LatLon minXintersect = lineSegmentIntersection(inside, outside, minXminY, minXmaxY);
		LatLon minYintersect = lineSegmentIntersection(inside, outside, minXminY, maxXminY);
		LatLon maxXintersect = lineSegmentIntersection(inside, outside, maxXminY, maxXmaxY);
		LatLon maxYintersect = lineSegmentIntersection(inside, outside, minXmaxY, maxXmaxY);
		LatLon[] intersects =
				new LatLon[] { minXintersect, minYintersect, maxXintersect, maxYintersect };
		double minDistance = Double.MAX_VALUE;
		LatLon minDistanceIntersect = null;

		for (LatLon intersect : intersects)
		{
			if (intersect != null)
			{
				double distance = distanceSquared(outside, intersect);
				if (distance < minDistance)
				{
					minDistance = distance;
					minDistanceIntersect = intersect;
				}
			}
		}

		if (minDistanceIntersect != null)
			return minDistanceIntersect;

		//TODO find out why we get here!
		String message = "Did not find an edge point in the sector";
		Logging.logger().fine(message);
		return inside;
	}

	private static void addCornerPoints(SubTileContour addTo, LatLon entry, LatLon exit,
			Sector sector)
	{
		if (entry.equals(exit))
			return;

		LatLon center = sector.getCentroid();
		double enX = entry.longitude.degrees - center.longitude.degrees;
		double enY = entry.latitude.degrees - center.latitude.degrees;
		double exX = exit.longitude.degrees - center.longitude.degrees;
		double exY = exit.latitude.degrees - center.latitude.degrees;

		//negative atan2 for clockwise direction
		double entryAngle = -Math.atan2(enY, enX);
		double exitAngle = -Math.atan2(exY, exX);

		double twoPI = Math.PI * 2d; //360 degrees
		double halfPI = Math.PI * 0.5d; //90 degrees
		double clockwiseDelta = (twoPI + entryAngle - exitAngle) % twoPI;

		double diff90 = (exitAngle + twoPI - Math.PI / 4d) % halfPI;
		int addCount = (int) ((clockwiseDelta + diff90) / halfPI);
		int firstCorner = (int) (((exitAngle - diff90 + twoPI) % twoPI) / halfPI);

		LatLon[] corners = sector.getCorners();
		LatLon minXminY = corners[0];
		LatLon maxXminY = corners[1];
		LatLon maxXmaxY = corners[2];
		LatLon minXmaxY = corners[3];

		LatLon[] clockwise = new LatLon[] { minXminY, minXmaxY, maxXmaxY, maxXminY };

		for (int i = firstCorner; i < addCount + firstCorner; i++)
		{
			addTo.points.add(clockwise[i % 4]);
		}
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + "), " + sector;
	}
}
