package au.gov.ga.worldwind.tiler.shapefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class ShapefileTile
{
	public final int row;
	public final int col;

	private final Sector sector;
	private final Coordinate centroid;
	public final Coordinate minmin;
	public final Coordinate minmax;
	public final Coordinate maxmax;
	public final Coordinate maxmin;
	private final Coordinate[] clockwise;

	private List<TileRecord> records = new ArrayList<TileRecord>();
	private TileRecord current = null;
	private int startPointsIndex = 0;
	private boolean noEntryShapeExists = false;
	private boolean filled = false;
	private Attributes filledAttributes;

	public ShapefileTile(Sector sector, int col, int row)
	{
		this.sector = sector;
		this.row = row;
		this.col = col;
		centroid = new Coordinate(sector.getCenterLongitude(), sector.getCenterLatitude());
		minmin = new Coordinate(sector.getMinLongitude(), sector.getMinLatitude());
		maxmin = new Coordinate(sector.getMinLongitude(), sector.getMaxLatitude());
		maxmax = new Coordinate(sector.getMaxLongitude(), sector.getMaxLatitude());
		minmax = new Coordinate(sector.getMaxLongitude(), sector.getMinLatitude());
		clockwise = new Coordinate[] { minmin, maxmin, maxmax, minmax };
	}

	//x = longitude
	//y = latitude

	public void addCoordinate(int shapeId, Coordinate coordinate, boolean entry, boolean exit,
			Attributes attributes)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entry || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = records.size();

			current = new TileRecord(shapeId, entry, attributes);
			records.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entry)
				noEntryShapeExists = true;
		}

		if (exit)
		{
			current.exited = true;
		}

		//add the vertex index
		current.coordinates.add(coordinate);
	}

	public void addHole(List<Coordinate> points, Attributes attributes)
	{
		if (current == null)
			throw new IllegalStateException("Cannot add hole to " + this
					+ ", as it has no current shape");

		TileRecord hole = new TileRecord(-1, false, attributes, points);
		current.holes.add(hole);
	}

	public void joinOrphanPolygons(int shapeId, ProgressReporter progress)
	{
		//Polygons must either be fully contained in the tile, or both enter AND exit.
		//If a pointset enters but not exits, there MUST be another pointset that exits
		//but not enters. Join these two pointsets (they will share a end point).

		if (noEntryShapeExists)
		{
			int noExitIndex = -1, noEntryIndex = -1;
			for (int i = startPointsIndex; i < records.size(); i++)
			{
				TileRecord p = records.get(i);
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
				TileRecord noEntry = records.get(noEntryIndex);
				TileRecord noExit = records.get(noExitIndex);

				//check if the two pointsets share a point
				Coordinate p1 = noEntry.coordinates.get(0);
				Coordinate p2 = noExit.coordinates.get(noExit.coordinates.size() - 1);
				int start = 1;
				if (!p1.equals(p2))
				{
					progress.getLogger().warning(
							"First entry Coordinate doesn't join up with last exit point");
					start = 0;
				}

				//add all noEntry's points (except the shared point) to the noExit
				//pointset (so that it has an exit), and then remove the noEntry pointset
				for (int i = start; i < noEntry.coordinates.size(); i++)
				{
					noExit.coordinates.add(noEntry.coordinates.get(i));
				}
				records.remove(noEntryIndex);
				noExit.exited = true;
			}

			//calling this function twice shouldn't have any affect; set flag to false
			noEntryShapeExists = false;
		}
	}

	public void markFilled(Attributes attributes)
	{
		filled = true;
		filledAttributes = attributes;
	}

	public boolean contains(Coordinate coordinate)
	{
		return sector.containsPoint(coordinate.y, coordinate.x);
	}

	public Coordinate limitCoordinateWithinTile(Coordinate coordinate)
	{
		double x =
				Util.limitRange(coordinate.x, sector.getMinLongitude(), sector.getMaxLongitude());
		double y = Util.limitRange(coordinate.y, sector.getMinLatitude(), sector.getMaxLatitude());
		return new Coordinate(x, y);
	}

	public double deltaLatitude()
	{
		return sector.getDeltaLatitude();
	}

	public double deltaLongitude()
	{
		return sector.getDeltaLongitude();
	}

	public double latitudeFract(Coordinate coordinate)
	{
		return (coordinate.y - sector.getMinLatitude()) / deltaLatitude();
	}

	public double longitudeFract(Coordinate coordinate)
	{
		return (coordinate.x - sector.getMinLongitude()) / deltaLongitude();
	}

	public void completePolygons()
	{
		Map<EntryExit, TileRecord> pointMap = new HashMap<EntryExit, TileRecord>();
		Map<TileRecord, EntryExit> reverseExitMap = new HashMap<TileRecord, EntryExit>();

		List<EntryExit> entryExits = new ArrayList<EntryExit>();
		List<EntryExit> entries = new ArrayList<EntryExit>();
		List<EntryExit> exits = new ArrayList<EntryExit>();

		for (TileRecord r : records)
		{
			if (r.entered && r.exited)
			{
				Coordinate en = r.coordinates.get(0);
				Coordinate ex = r.coordinates.get(r.coordinates.size() - 1);

				EntryExit entry = new EntryExit(en, centroid, true);
				EntryExit exit = new EntryExit(ex, centroid, false);

				pointMap.put(entry, r);
				pointMap.put(exit, r);
				reverseExitMap.put(r, exit);

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
		EntryExit previous = entryExits.get(entryExits.size() - 2);
		EntryExit current = entryExits.get(entryExits.size() - 1);
		for (int i = 0; i < entryExits.size(); i++)
		{
			EntryExit next = entryExits.get(i);
			if (current.isExit() && next.isEntry() && !current.coordinate.equals(next.coordinate)
					&& !(previous.isEntry() && current.coordinate.equals(previous.coordinate)))
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

			TileRecord exitRecord = pointMap.remove(exit);
			TileRecord entryRecord = pointMap.remove(entry);

			//add any required sector corner points clockwise from exit to entry
			addCornerPoints(exitRecord, entry.coordinate, exit.coordinate);

			if (exitRecord == entryRecord)
			{
				//same pointset, so do nothing
			}
			else
			{
				//add all points from entry points to the start of the exit points
				exitRecord.coordinates.addAll(entryRecord.coordinates);
				//remove the entry points from the points list
				records.remove(entryRecord);

				//because we removed entryContour from the list, exitContour now becomes
				//the mapped value for the entryContour's exit point
				EntryExit entrysExit = reverseExitMap.get(entryRecord);
				pointMap.put(entrysExit, exitRecord);
				reverseExitMap.put(exitRecord, entrysExit);
			}
		}
	}

	protected void fillSector()
	{
		TileRecord current = new TileRecord(-1, false, filledAttributes);
		records.add(current);
		current.exited = false;

		for (Coordinate c : clockwise)
			current.coordinates.add(c);
	}

	protected void addCornerPoints(TileRecord addTo, Coordinate entry, Coordinate exit)
	{
		if (entry.equals(exit))
			return;

		double enX = entry.x - centroid.x;
		double enY = entry.y - centroid.y;
		double exX = exit.x - centroid.x;
		double exY = exit.y - centroid.y;

		//negative atan2 for clockwise direction
		double entryAngle = -Math.atan2(enY, enX);
		double exitAngle = -Math.atan2(exY, exX);

		double twoPI = Math.PI * 2d; //360 degrees
		double halfPI = Math.PI * 0.5d; //90 degrees
		double clockwiseDelta = (twoPI + entryAngle - exitAngle) % twoPI;

		double diff90 = (exitAngle + twoPI - Math.PI / 4d) % halfPI;
		int addCount = (int) ((clockwiseDelta + diff90) / halfPI);
		int firstCorner = (int) (((exitAngle - diff90 + twoPI) % twoPI) / halfPI);

		for (int i = firstCorner; i < addCount + firstCorner; i++)
		{
			addTo.coordinates.add(clockwise[i % 4]);
		}
	}

	/*public void enterShape(int shapeId, Coordinate outsidePoint, Coordinate insidePoint,
			Attributes attributes)
	{
		updateCurrentShapeIfRequired(shapeId, true, attributes);

		Coordinate edge = edgePoint(outsidePoint, insidePoint, sector);
		current.coordinates.add(edge);
		current.coordinates.add(insidePoint);
	}

	public void continueShape(int shapeId, Coordinate insidePoint, Attributes attributes)
	{
		updateCurrentShapeIfRequired(shapeId, false, attributes);

		current.coordinates.add(insidePoint);
	}

	public void exitShape(int shapeId, Coordinate insidePoint, Coordinate outsidePoint)
	{
		Coordinate edge = edgePoint(insidePoint, outsidePoint, sector);
		current.coordinates.add(edge);
		current.exited = true;
	}

	public boolean crossShape(int shapeId, Coordinate fromPoint1, Coordinate toPoint2,
			Attributes attributes)
	{
		Coordinate minXminY = new Coordinate(sector.getMinLongitude(), sector.getMinLatitude());
		Coordinate minXmaxY = new Coordinate(sector.getMinLongitude(), sector.getMaxLatitude());
		Coordinate maxXminY = new Coordinate(sector.getMaxLongitude(), sector.getMinLatitude());
		Coordinate maxXmaxY = new Coordinate(sector.getMaxLongitude(), sector.getMaxLatitude());

		Coordinate minXintersect =
				lineSegmentIntersection(fromPoint1, toPoint2, minXminY, minXmaxY);
		Coordinate minYintersect =
				lineSegmentIntersection(fromPoint1, toPoint2, minXminY, maxXminY);
		Coordinate maxXintersect =
				lineSegmentIntersection(fromPoint1, toPoint2, maxXminY, maxXmaxY);
		Coordinate maxYintersect =
				lineSegmentIntersection(fromPoint1, toPoint2, minXmaxY, maxXmaxY);
		Coordinate[] intersects =
				new Coordinate[] { minXintersect, minYintersect, maxXintersect, maxYintersect };

		int count = 0;
		Coordinate[] notNull = new Coordinate[2];
		for (Coordinate p : intersects)
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
			Coordinate temp = notNull[0];
			notNull[0] = notNull[1];
			notNull[1] = temp;
		}

		updateCurrentShapeIfRequired(shapeId, true, attributes);
		current.coordinates.add(notNull[0]);
		current.coordinates.add(notNull[1]);
		current.exited = true;
		return true;
	}*/

	/*private static Coordinate lineSegmentIntersection(Coordinate p1, Coordinate p2, Coordinate p3,
			Coordinate p4)
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
		return new Coordinate(x, y);
	}*/

	/*private void updateCurrentShapeIfRequired(int shapeId, boolean entered, Attributes attributes)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entered || newShapeId)
		{
			if (newShapeId)
				startPointsIndex = records.size();

			current = new TileRecord(shapeId, entered, attributes);
			records.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entered)
				noEntryShapeExists = true;
		}
	}*/

	/**
	 * Returns a Coordinate on the edge of the sector that exists on the line
	 * between p1 and p2. One of p1 or p2 must be inside the sector.
	 */
	/*private static Coordinate edgePoint(Coordinate p1, Coordinate p2, Sector sector)
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
				return new Coordinate(lonCrossing, crossY);
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
			return new Coordinate(lonCrossing, crossY);
		}
		else
		{
			double latCrossing = crossMinLat ? sector.getMinLatitude() : sector.getMaxLatitude();
			double ypos = (latCrossing - minY) / (maxY - minY);
			double crossX = xOfMinY + ypos * (xOfMaxY - xOfMinY);
			return new Coordinate(crossX, latCrossing);
		}
	}*/

	/*public void completePolygons()
	{
		Map<EntryExit, TileRecord> pointMap = new HashMap<EntryExit, TileRecord>();
		Map<TileRecord, EntryExit> reverseExitMap = new HashMap<TileRecord, EntryExit>();
		List<EntryExit> entryExits = new ArrayList<EntryExit>();

		for (TileRecord p : records)
		{
			if (p.entered && p.exited)
			{
				Coordinate en = p.coordinates.get(0);
				Coordinate ex = p.coordinates.get(p.coordinates.size() - 1);

				EntryExit entry = new EntryExit(en, centroid, true);
				EntryExit exit = new EntryExit(ex, centroid, false);

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
			EntryExit exit = entryExits.get(i);
			EntryExit entry = entryExits.get((i + 1) % entryExits.size());

			if (exit.isEntry() || entry.isExit())
			{
				throw new IllegalStateException(
						"Exit/Entry points don't alternate clockwise around the tile's sector");
			}

			//join curves from exit to entry

			TileRecord exitPoints = pointMap.remove(exit);
			TileRecord entryPoints = pointMap.remove(entry);

			//add any required sector corner points clockwise from exit to entry
			addCornerPoints(exitPoints, entry.coordinate, exit.coordinate, sector);

			if (exitPoints == entryPoints)
			{
				//same pointset, so do nothing
			}
			else
			{
				//add all points from entry points to the start of the exit points
				exitPoints.coordinates.addAll(entryPoints.coordinates);
				//remove the entry points from the points list
				records.remove(entryPoints);

				//because we removed entryPoints from the list, exitPoints now becomes
				//the mapped value for the entryPoints' exit point
				EntryExit entrysExit = reverseExitMap.get(entryPoints);
				pointMap.put(entrysExit, exitPoints);
				reverseExitMap.put(exitPoints, entrysExit);
			}
		}
	}

	private static void sortEntryExitList(List<EntryExit> list)
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
			EntryExit p = list.get(i);
			if (p.isExit())
			{
				//if this Coordinate is the same as the Coordinate either side of it, then
				//don't let it count toward even/odd weight
				if (i > 0 && p.coordinate.equals(list.get(i - 1).coordinate))
					continue;
				if (i < list.size() - 1 && p.coordinate.equals(list.get(i + 1).coordinate))
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
			EntryExit current = list.get(i);
			if (!current.isExit())
			{
				boolean swapped = moveEntryIfEqualToExit(list, i, -1);
				if (!swapped)
					swapped = moveEntryIfEqualToExit(list, i, 1);
			}
		}
	}

	private static boolean moveEntryIfEqualToExit(List<EntryExit> list, int index, int offset)
	{
		if (index >= 0 && index + offset >= 0 && index < list.size()
				&& index + offset < list.size())
		{
			EntryExit p1 = list.get(index);
			EntryExit p2 = list.get(index + offset);
			if (p2.isExit() && p2.coordinate.equals(p1.coordinate))
			{
				list.remove(index);
				list.add(index + offset, p1);
			}
		}
		return false;
	}

	private static void addCornerPoints(TileRecord addTo, Coordinate entry, Coordinate exit,
			Sector sector)
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

		Coordinate minXminY = new Coordinate(sector.getMinLongitude(), sector.getMinLatitude());
		Coordinate maxXminY = new Coordinate(sector.getMaxLongitude(), sector.getMinLatitude());
		Coordinate minXmaxY = new Coordinate(sector.getMinLongitude(), sector.getMaxLatitude());
		Coordinate maxXmaxY = new Coordinate(sector.getMaxLongitude(), sector.getMaxLatitude());

		Coordinate[] clockwise = new Coordinate[] { minXminY, minXmaxY, maxXmaxY, maxXminY };

		for (int i = firstCorner; i < addCount + firstCorner; i++)
		{
			addTo.coordinates.add(clockwise[i % 4]);
		}
	}*/

	public FeatureCollection createRecords(GeometryFactory factory, FeatureSchema schema,
			boolean polygon)
	{
		FeatureCollection fc = new FeatureDataset(schema);
		for (TileRecord p : records)
		{
			Geometry geometry;
			if (polygon)
			{
				Coordinate[] coordinates = new Coordinate[p.coordinates.size() + 1];
				coordinates = p.coordinates.toArray(coordinates);
				coordinates[coordinates.length - 1] = p.coordinates.get(0);

				LinearRing shell = factory.createLinearRing(coordinates);
				LinearRing[] holes = new LinearRing[p.holes.size()];
				for (int i = 0; i < p.holes.size(); i++)
				{
					TileRecord h = p.holes.get(i);
					Coordinate[] holeCoordinates =
							h.coordinates.toArray(new Coordinate[h.coordinates.size()]);
					LinearRing hole = factory.createLinearRing(holeCoordinates);
					holes[i] = hole;
				}
				geometry = factory.createPolygon(shell, holes);
			}
			else
			{
				Coordinate[] coordinates =
						p.coordinates.toArray(new Coordinate[p.coordinates.size()]);
				geometry = factory.createLineString(coordinates);
			}

			Feature feature = new BasicFeature(schema);
			feature.setGeometry(geometry);
			p.attributes.saveAttributes(feature);

			fc.add(feature);
		}
		return fc;
	}

	/*public boolean overlaps(Envelope inEnvelope)
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
	}*/
}
