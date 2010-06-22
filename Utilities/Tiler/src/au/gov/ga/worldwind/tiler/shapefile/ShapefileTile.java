package au.gov.ga.worldwind.tiler.shapefile;

import gistoolkit.features.AttributeType;
import gistoolkit.features.Envelope;
import gistoolkit.features.LineString;
import gistoolkit.features.LinearRing;
import gistoolkit.features.Point;
import gistoolkit.features.Polygon;
import gistoolkit.features.Record;
import gistoolkit.features.Shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ga.worldwind.tiler.util.Sector;


public class ShapefileTile
{
	public final Sector sector;
	public final int level;
	public final int row;
	public final int col;

	List<TileRecord> points = new ArrayList<TileRecord>();

	private TileRecord current = null;

	private int startPointsIndex;
	private boolean noEntryShapeExists = false;
	private boolean filled = false;
	private Attributes filledAttributes;

	public ShapefileTile(Sector sector, int level, int col, int row)
	{
		this.sector = sector;
		this.level = level;
		this.row = row;
		this.col = col;
	}

	//x = longitude
	//y = latitude

	public void enterShape(int shapeId, Point outsidePoint, Point insidePoint, Attributes attributes)
	{
		updateCurrentShapeIfRequired(shapeId, true, attributes);

		Point edge = edgePoint(outsidePoint, insidePoint, sector);
		current.points.add(edge);
		current.points.add(insidePoint);
	}

	public void continueShape(int shapeId, Point insidePoint, Attributes attributes)
	{
		updateCurrentShapeIfRequired(shapeId, false, attributes);

		current.points.add(insidePoint);
	}

	public void exitShape(int shapeId, Point insidePoint, Point outsidePoint)
	{
		Point edge = edgePoint(insidePoint, outsidePoint, sector);
		current.points.add(edge);
		current.exited = true;
	}

	public boolean crossShape(int shapeId, Point fromPoint1, Point toPoint2, Attributes attributes)
	{
		Point minXminY = new Point(sector.getMinLongitude(), sector.getMinLatitude());
		Point minXmaxY = new Point(sector.getMinLongitude(), sector.getMaxLatitude());
		Point maxXminY = new Point(sector.getMaxLongitude(), sector.getMinLatitude());
		Point maxXmaxY = new Point(sector.getMaxLongitude(), sector.getMaxLatitude());

		Point minXintersect = lineSegmentIntersection(fromPoint1, toPoint2, minXminY, minXmaxY);
		Point minYintersect = lineSegmentIntersection(fromPoint1, toPoint2, minXminY, maxXminY);
		Point maxXintersect = lineSegmentIntersection(fromPoint1, toPoint2, maxXminY, maxXmaxY);
		Point maxYintersect = lineSegmentIntersection(fromPoint1, toPoint2, minXmaxY, maxXmaxY);
		Point[] intersects =
				new Point[] { minXintersect, minYintersect, maxXintersect, maxYintersect };

		int count = 0;
		Point[] notNull = new Point[2];
		for (Point p : intersects)
		{
			if (p != null)
			{
				if (count < 2)
					notNull[count] = p;
				count++;
			}
		}

		//if more or less than two intersections were found, then the line either
		//doesn't cross this shape or something strange has happened, so return false
		if (count != 2)
			return false;

		if (fromPoint1.distance(notNull[0]) > fromPoint1.distance(notNull[1]))
		{
			Point temp = notNull[0];
			notNull[0] = notNull[1];
			notNull[1] = temp;
		}

		updateCurrentShapeIfRequired(shapeId, true, attributes);
		current.points.add(notNull[0]);
		current.points.add(notNull[1]);
		current.exited = true;
		return true;
	}

	public void addHole(List<Point> points, Attributes attributes)
	{
		if (current == null)
			throw new IllegalStateException("Cannot add hole to " + this
					+ ", as it has no current shape");

		TileRecord hole = new TileRecord(-1, false, attributes, points);
		current.holes.add(hole);
	}

	private static Point lineSegmentIntersection(Point p1, Point p2, Point p3, Point p4)
	{
		double ud = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);

		//lines are parallel (don't care about coincident lines)
		if (ud == 0)
			return null;

		double uan = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x);
		double ubn = (p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x);
		double ua = uan / ud;
		double ub = ubn / ud;

		//intersection is not within the line segments
		if (ua < 0 || 1 < ua || ub < 0 || 1 < ub)
			return null;

		double x = p1.x + ua * (p2.x - p1.x);
		double y = p1.y + ua * (p2.y - p1.y);
		return new Point(x, y);
	}

	public void markFilled(Attributes attributes)
	{
		filled = true;
		filledAttributes = attributes;
	}

	private void fillSector()
	{
		TileRecord current = new TileRecord(-1, false, filledAttributes);
		points.add(current);
		current.exited = false;
		current.points.add(new Point(sector.getMinLongitude(), sector.getMinLatitude()));
		current.points.add(new Point(sector.getMinLongitude(), sector.getMaxLatitude()));
		current.points.add(new Point(sector.getMaxLongitude(), sector.getMaxLatitude()));
		current.points.add(new Point(sector.getMaxLongitude(), sector.getMinLatitude()));
	}

	private void updateCurrentShapeIfRequired(int shapeId, boolean entered, Attributes attributes)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entered || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = points.size();

			current = new TileRecord(shapeId, entered, attributes);
			points.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entered)
				noEntryShapeExists = true;
		}
	}

	/**
	 * Returns a point on the edge of the sector that exists on the line between
	 * p1 and p2. One of p1 or p2 must be inside the sector.
	 */
	private static Point edgePoint(Point p1, Point p2, Sector sector)
	{
		double minX = Math.min(p1.x, p2.x);
		double maxX = Math.max(p1.x, p2.x);
		double minY = Math.min(p1.y, p2.y);
		double maxY = Math.max(p1.y, p2.y);

		double yOfMinX = minX == p1.x ? p1.y : p2.y;
		double yOfMaxX = maxX == p1.x ? p1.y : p2.y;
		double xOfMinY = minY == p1.y ? p1.x : p2.x;
		double xOfMaxY = maxY == p1.y ? p1.x : p2.x;

		boolean crossMinLon = minX <= sector.getMinLongitude() && sector.getMinLongitude() <= maxX;
		boolean crossMaxLon = minX <= sector.getMaxLongitude() && sector.getMaxLongitude() <= maxX;
		boolean crossMinLat = minY <= sector.getMinLatitude() && sector.getMinLatitude() <= maxY;
		boolean crossMaxLat = minY <= sector.getMaxLatitude() && sector.getMaxLatitude() <= maxY;
		boolean crossLon = crossMinLon || crossMaxLon;
		boolean crossLat = crossMinLat || crossMaxLat;

		if (crossLon && crossLat)
		{
			//line is crossing two boundaries, find the actual crossing point

			double lonCrossing = crossMinLon ? sector.getMinLongitude() : sector.getMaxLongitude();
			double xpos = (lonCrossing - minX) / (maxX - minX);
			double crossY = yOfMinX + xpos * (yOfMaxX - yOfMinX);
			if (sector.getMinLatitude() <= crossY && crossY <= sector.getMaxLatitude())
			{
				return new Point(lonCrossing, crossY);
			}
			else
			{
				crossMinLon = crossMaxLon = false;
				crossMinLat = crossY < sector.getMinLatitude();
				crossMaxLat = !crossMinLat;
			}
		}

		if (crossMinLon || crossMaxLon)
		{
			double lonCrossing = crossMinLon ? sector.getMinLongitude() : sector.getMaxLongitude();
			double xpos = (lonCrossing - minX) / (maxX - minX);
			double crossY = yOfMinX + xpos * (yOfMaxX - yOfMinX);
			return new Point(lonCrossing, crossY);
		}
		else
		{
			double latCrossing = crossMinLat ? sector.getMinLatitude() : sector.getMaxLatitude();
			double ypos = (latCrossing - minY) / (maxY - minY);
			double crossX = xOfMinY + ypos * (xOfMaxY - xOfMinY);
			return new Point(crossX, latCrossing);
		}
	}

	public void joinOrphanPolygons(int shapeId)
	{
		//Polygons must either be fully contained in the tile, or both enter AND exit.
		//If a pointset enters but not exits, there MUST be another pointset that exits
		//but not enters. Join these two pointsets (they will share a end point).

		if (noEntryShapeExists)
		{
			int noExitIndex = -1, noEntryIndex = -1;
			for (int i = startPointsIndex; i < points.size(); i++)
			{
				TileRecord p = points.get(i);
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
				TileRecord noEntry = points.get(noEntryIndex);
				TileRecord noExit = points.get(noExitIndex);

				//check if the two pointsets share a point
				Point p1 = noEntry.points.get(0);
				Point p2 = noExit.points.get(noExit.points.size() - 1);
				int start;
				if (p1.equals(p2))
				{
					start = 1;
				}
				else
				{
					System.out
							.println("WARNING: first entry point doesn't join up with last exit point");
					start = 0;
				}

				//add all noEntry's points (except the shared point) to the noExit
				//pointset (so that it has an exit), and then remove the noEntry pointset
				for (int i = start; i < noEntry.points.size(); i++)
				{
					noExit.points.add(noEntry.points.get(i));
				}
				points.remove(noEntryIndex);
				noExit.exited = true;
			}

			//calling this function twice shouldn't have any affect; set flag to false
			noEntryShapeExists = false;
		}
	}

	public void completePolygons()
	{
		Map<EntryExitPoint, TileRecord> pointMap = new HashMap<EntryExitPoint, TileRecord>();
		Map<TileRecord, EntryExitPoint> reverseExitMap = new HashMap<TileRecord, EntryExitPoint>();
		List<EntryExitPoint> entryExits = new ArrayList<EntryExitPoint>();

		for (TileRecord p : points)
		{
			if (p.entered && p.exited)
			{
				Point en = p.points.get(0);
				Point ex = p.points.get(p.points.size() - 1);

				EntryExitPoint entry = new EntryExitPoint(en, sector, true);
				EntryExitPoint exit = new EntryExitPoint(ex, sector, false);

				pointMap.put(entry, p);
				pointMap.put(exit, p);
				reverseExitMap.put(p, exit);
				entryExits.add(entry);
				entryExits.add(exit);
			}
		}

		//no polygons have entered/exited this tile's sector's edges
		if (entryExits.isEmpty())
		{
			//if this tile's sector is inside a polygon, then fill the entire sector
			//(if it is inside a polygon, but there are some entry/exits, then the code
			//below will fill the sector properly)
			if (filled)
			{
				fillSector();
			}
			return;
		}

		//sort the list and ensure entry's/exit's alternate
		sortEntryExitList(entryExits);

		int firstExit = entryExits.get(0).isExit() ? 0 : 1;
		for (int i = firstExit; i < entryExits.size(); i += 2)
		{
			EntryExitPoint exit = entryExits.get(i);
			EntryExitPoint entry = entryExits.get((i + 1) % entryExits.size());

			if (exit.isEntry() || entry.isExit())
			{
				throw new IllegalStateException(
						"Exit/Entry points don't alternate clockwise around the tile's sector");
			}

			//join curves from exit to entry

			TileRecord exitPoints = pointMap.remove(exit);
			TileRecord entryPoints = pointMap.remove(entry);

			//add any required sector corner points clockwise from exit to entry
			addCornerPoints(exitPoints, entry.point, exit.point, sector);

			if (exitPoints == entryPoints)
			{
				//same pointset, so do nothing
			}
			else
			{
				//add all points from entry points to the start of the exit points
				exitPoints.points.addAll(entryPoints.points);
				//remove the entry points from the points list
				points.remove(entryPoints);

				//because we removed entryPoints from the list, exitPoints now becomes
				//the mapped value for the entryPoints' exit point
				EntryExitPoint entrysExit = reverseExitMap.get(entryPoints);
				pointMap.put(entrysExit, exitPoints);
				reverseExitMap.put(exitPoints, entrysExit);
			}
		}
	}

	private static void sortEntryExitList(List<EntryExitPoint> list)
	{
		//sort entry/exit points in clockwise order
		Collections.sort(list);

		//Now if the shapefile is valid, the list should alternate in entry/exit order.
		//However, if entry/exit points are the same, the list may not alternate.
		//Now check the list for this case.

		//first check to see if the bulk of the exits are in even or odd indices
		int evenExits = 0, oddExits = 0;
		for (int i = 0; i < list.size(); i++)
		{
			EntryExitPoint p = list.get(i);
			if (p.isExit())
			{
				//if this point is the same as the point either side of it, then
				//don't let it count toward even/odd weight
				if (i > 0 && p.point.equals(list.get(i - 1).point))
					continue;
				if (i < list.size() - 1 && p.point.equals(list.get(i + 1).point))
					continue;

				if (i % 2 == 0)
					evenExits++;
				else
					oddExits++;
			}
		}

		//now swap any entries/exits that are the same but in the incorrect spot
		int start = evenExits > oddExits ? 0 : 1;
		for (int i = start; i < list.size(); i += 2)
		{
			EntryExitPoint current = list.get(i);
			if (!current.isExit())
			{
				boolean swapped = moveEntryIfEqualToExit(list, i, -1);
				if (!swapped)
					swapped = moveEntryIfEqualToExit(list, i, 1);
			}
		}
	}

	private static boolean moveEntryIfEqualToExit(List<EntryExitPoint> list, int index, int offset)
	{
		if (index >= 0 && index + offset >= 0 && index < list.size()
				&& index + offset < list.size())
		{
			EntryExitPoint p1 = list.get(index);
			EntryExitPoint p2 = list.get(index + offset);
			if (p2.isExit() && p2.point.equals(p1.point))
			{
				list.remove(index);
				list.add(index + offset, p1);
			}
		}
		return false;
	}

	private static void addCornerPoints(TileRecord addTo, Point entry, Point exit, Sector sector)
	{
		if (entry.equals(exit))
			return;

		double enX = entry.x - sector.getCenterLongitude();
		double enY = entry.y - sector.getCenterLatitude();
		double exX = exit.x - sector.getCenterLongitude();
		double exY = exit.y - sector.getCenterLatitude();

		//negative atan2 for clockwise direction
		double entryAngle = -Math.atan2(enY, enX);
		double exitAngle = -Math.atan2(exY, exX);

		double twoPI = Math.PI * 2d; //360 degrees
		double halfPI = Math.PI * 0.5d; //90 degrees
		double clockwiseDelta = (twoPI + entryAngle - exitAngle) % twoPI;

		double diff90 = (exitAngle + twoPI - Math.PI / 4d) % halfPI;
		int addCount = (int) ((clockwiseDelta + diff90) / halfPI);
		int firstCorner = (int) (((exitAngle - diff90 + twoPI) % twoPI) / halfPI);

		Point minXminY = new Point(sector.getMinLongitude(), sector.getMinLatitude());
		Point maxXminY = new Point(sector.getMaxLongitude(), sector.getMinLatitude());
		Point minXmaxY = new Point(sector.getMinLongitude(), sector.getMaxLatitude());
		Point maxXmaxY = new Point(sector.getMaxLongitude(), sector.getMaxLatitude());

		Point[] clockwise = new Point[] { minXminY, minXmaxY, maxXmaxY, maxXminY };

		for (int i = firstCorner; i < addCount + firstCorner; i++)
		{
			addTo.points.add(clockwise[i % 4]);
		}
	}

	public List<Record> createRecords(boolean polygon)
	{
		List<Record> records = new ArrayList<Record>();
		for (TileRecord p : points)
		{
			Point[] points = p.points.toArray(new Point[p.points.size()]);

			Shape shp;
			if (polygon)
			{
				LinearRing ring = new LinearRing(points);
				Polygon poly = new Polygon(ring);
				shp = poly;

				if (!p.holes.isEmpty())
				{
					int i = 0;
					for (TileRecord h : p.holes)
					{
						Point[] holePoints = h.points.toArray(new Point[h.points.size()]);
						LinearRing hole = new LinearRing(holePoints);
						poly.addHole(i, hole);
						i++;
					}
				}
			}
			else
			{
				shp = new LineString(points);
			}

			Record record = new Record();
			record.setShape(shp);
			records.add(record);

			//set the records attributes
			Attributes attributes = p.attributes;
			if (attributes == null || attributes.getAttributeNames() == null
					|| attributes.getAttributeNames().length == 0)
			{
				//shapefile writer throws an error if there are no attributes, so add level as an attribute
				record.setAttributeNames(new String[] { "level" });
				record.setAttributeTypes(new AttributeType[] { new AttributeType(
						AttributeType.INTEGER, -1, 0) });
				record.setAttributes(new Object[] { level });
			}
			else
			{
				record.setAttributeNames(attributes.getAttributeNames());
				record.setAttributeTypes(attributes.getAttributeTypes());
				record.setAttributes(attributes.getAttributes());
			}
		}
		return records;
	}

	public boolean contains(Point point)
	{
		return sector.containsPoint(point.y, point.x);
	}

	public boolean overlaps(Envelope inEnvelope)
	{
		if (inEnvelope == null)
			return false;

		// check the sides.
		if (inEnvelope.getMaxX() < sector.getMinLongitude())
			return false;
		if (inEnvelope.getMinX() > sector.getMaxLongitude())
			return false;
		if (inEnvelope.getMinY() > sector.getMaxLatitude())
			return false;
		if (inEnvelope.getMaxY() < sector.getMinLatitude())
			return false;
		return true;
	}
}
