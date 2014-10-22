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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureWriter;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * A subtile of a shapefile which contains all the geometry of a shapefile
 * within a certain sector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	private final List<TileRecord> records = new ArrayList<TileRecord>();
	private TileRecord current = null;
	private int startPointsIndex = 0;
	private boolean noEntryShapeExists = false;
	private boolean filled = false;
	private Attributes filledAttributes;
	private final List<TileRecord> filledHoles = new ArrayList<TileRecord>();
	private Boolean filledNoHoles = null;

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

	/**
	 * Add a new coordinate to the current shape. If the shapeId is different
	 * from the current shape's id or this coordinate is entering this tile, a
	 * new shape is started.
	 * 
	 * @param shapeId
	 *            Id of the shape that this coordinate is a part of
	 * @param coordinate
	 *            Coordinate to add
	 * @param entry
	 *            Is this coordinate entering this tile?
	 * @param exit
	 *            Is this coordinate exiting this tile?
	 * @param attributes
	 *            Shape attributes
	 */
	public void addCoordinate(int shapeId, Coordinate coordinate, boolean entry, boolean exit, Attributes attributes)
	{
		//create a new pointset if:
		// - the record has entered the tile (each entry creates a new shape)
		// - no current pointset exists (continueShape is called without an enter)
		// - the shape id is different (a new shape has started, through continueShape without an enter)

		boolean newShapeId = current == null || current.shapeId != shapeId;

		if (entry || newShapeId)
		{
			if (newShapeId)
			{
				startPointsIndex = records.size();
			}

			current = new TileRecord(shapeId, entry, attributes);
			records.add(current);

			//if this shape was not created by a record entering the tile, then a 'noEntryShapeExists'
			if (!entry)
			{
				noEntryShapeExists = true;
			}
		}

		if (exit)
		{
			current.exited = true;
		}

		//add the vertex index
		current.coordinates.add(coordinate);
	}

	/**
	 * Add a hole to the current shape. The hole must be fully within both the
	 * current shape and this tile's sector.
	 * 
	 * @param points
	 *            Points involved in the hole
	 * @param attributes
	 *            Shape attributes
	 */
	public void addHole(List<Coordinate> points, Attributes attributes)
	{
		if (current == null && !filled)
			throw new IllegalStateException("Cannot add hole to " + this + ", as it has no current shape");

		TileRecord hole = new TileRecord(-1, false, attributes, points);
		if (current != null)
			current.holes.add(hole);
		else
			filledHoles.add(hole);
	}

	/**
	 * Polygons must either be fully contained in the tile, or both enter AND
	 * exit. If a pointset enters but not exits, there MUST be another pointset
	 * that exits but not enters. Join these two pointsets (they will share an
	 * end point).
	 * 
	 * @param progress
	 */
	public void joinOrphanPolygons(ProgressReporter progress)
	{
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
					progress.getLogger().warning("First entry Coordinate doesn't join up with last exit point");
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

	/**
	 * Mark this tile as fully filled (ie inside a polygon).
	 * 
	 * @param attributes
	 *            Attributes of the polygon that this tile is inside
	 */
	public void markFilled(Attributes attributes)
	{
		filled = true;
		filledAttributes = attributes;
	}

	/**
	 * Does this tile's sector contain the given coordinate?
	 * 
	 * @param coordinate
	 * @return True if coordinate is inside this tile's sector
	 */
	public boolean contains(Coordinate coordinate)
	{
		return sector.containsPoint(coordinate.y, coordinate.x);
	}

	/**
	 * Does this tile's sector contain the given latitude/longitude?
	 * 
	 * @param x
	 * @param y
	 * @return True if lat/lon is inside this tile's sector
	 */
	public boolean contains(double latitude, double longitude)
	{
		return sector.containsPoint(latitude, longitude);
	}

	/**
	 * Limit the given coordinate's latitude/longitude to be within this tile's
	 * sector.
	 * 
	 * @param coordinate
	 *            Coordinate to limit
	 * @return coordinate limited to this tile's sector
	 */
	public Coordinate limitCoordinateWithinTile(Coordinate coordinate)
	{
		double x = Util.clamp(coordinate.x, sector.getMinLongitude(), sector.getMaxLongitude());
		double y = Util.clamp(coordinate.y, sector.getMinLatitude(), sector.getMaxLatitude());
		return new Coordinate(x, y);
	}

	/**
	 * @return Tile's sector's delta latitude in degrees
	 */
	public double deltaLatitude()
	{
		return sector.getDeltaLatitude();
	}

	/**
	 * @return Tile's sector's delta longitude in degrees
	 */
	public double deltaLongitude()
	{
		return sector.getDeltaLongitude();
	}

	/**
	 * @return Tile's sector
	 */
	public Sector sector()
	{
		return sector;
	}

	/**
	 * Fraction of the given coordinate's latitude within the tile's sector's
	 * latitude span
	 * 
	 * @param coordinate
	 * @return
	 */
	public double latitudeFract(Coordinate coordinate)
	{
		return (coordinate.y - sector.getMinLatitude()) / deltaLatitude();
	}

	/**
	 * Fraction of the given coordinate's longitude within the tile's sector's
	 * longitude span
	 * 
	 * @param coordinate
	 * @return
	 */
	public double longitudeFract(Coordinate coordinate)
	{
		return (coordinate.x - sector.getMinLongitude()) / deltaLongitude();
	}

	/**
	 * Join any exits to their neighbouring entries, moving clockwise around the
	 * sector. If an exit is on a different side to it's corresponding entry,
	 * add the sector corner points between the two points when joining.
	 * 
	 * @param minimumArea
	 *            Remove any polygons with area less than this minimum, 0 for no
	 *            removal
	 */
	public void completePolygons(double minimumArea)
	{
		Map<EntryExit, TileRecord> pointMap = new HashMap<EntryExit, TileRecord>();
		Map<TileRecord, EntryExit> reverseExitMap = new HashMap<TileRecord, EntryExit>();

		List<EntryExit> entryExits = new ArrayList<EntryExit>();
		List<EntryExit> entries = new ArrayList<EntryExit>();
		List<EntryExit> exits = new ArrayList<EntryExit>();

		//remove any entry/exit records that have a negligible area
		if (minimumArea > 0)
		{
			for (int i = 0; i < records.size(); i++)
			{
				TileRecord r = records.get(i);
				if (r.entered && r.exited && Math.abs(r.area()) < minimumArea)
				{
					records.remove(i--);
				}
			}
		}

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

		TileRecord exitRecord = null;
		for (int i = 0; i < entries.size(); i++)
		{
			EntryExit exit = exits.get(i);
			EntryExit entry = entries.get((i + offset + entries.size()) % entries.size());

			//join curves from exit to entry

			exitRecord = pointMap.remove(exit);
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
				//copy any holes in the record 
				exitRecord.holes.addAll(entryRecord.holes);
				//remove the entry points from the points list
				records.remove(entryRecord);

				//because we removed entryContour from the list, exitContour now becomes
				//the mapped value for the entryContour's exit point
				EntryExit entrysExit = reverseExitMap.get(entryRecord);
				pointMap.put(entrysExit, exitRecord);
				reverseExitMap.put(exitRecord, entrysExit);
			}
		}

		//because this is not a completely filled tile (due to the existance of an entry/exit),
		//we need to add the holes that were added before there were any entries/exits
		exitRecord.holes.addAll(filledHoles);
	}

	/**
	 * Fill the tile's sector with a square polygon.
	 */
	protected void fillSector()
	{
		TileRecord record = new TileRecord(-1, false, filledAttributes);
		records.add(record);
		record.exited = false;

		for (Coordinate c : clockwise)
			record.coordinates.add(c);

		record.holes.addAll(filledHoles);
		filledNoHoles = filledHoles.isEmpty();
	}

	/**
	 * Add the sector's corner points that lie between the entry and exit points
	 * (in a clockwise direction) to the given record.
	 * 
	 * @param addTo
	 *            Record to add corner points to
	 * @param entry
	 *            Entry point
	 * @param exit
	 *            Exit point
	 */
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

	/**
	 * @return Number of records/geometries in this tile
	 */
	public int recordCount()
	{
		return records.size();
	}

	/**
	 * Create Shapefile records for this tile.
	 * 
	 * @param factory
	 * @param schema
	 * @param polygon
	 *            Should the features created be marked as polygons? Otherwise
	 *            linestrings are created.
	 * @return {@link FeatureCollection} containing all the records in this
	 *         tile.
	 * @throws IOException
	 */
	public void writeFeatures(FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter, SimpleFeatureType schema,
			GeometryFactory factory, boolean polygon) throws IOException
	{
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
				for (int j = 0; j < p.holes.size(); j++)
				{
					TileRecord h = p.holes.get(j);
					Coordinate[] holeCoordinates = h.coordinates.toArray(new Coordinate[h.coordinates.size()]);
					LinearRing hole = factory.createLinearRing(holeCoordinates);
					holes[j] = hole;
				}
				geometry = factory.createPolygon(shell, holes);
			}
			else
			{
				Coordinate[] coordinates = p.coordinates.toArray(new Coordinate[p.coordinates.size()]);
				geometry = factory.createLineString(coordinates);
			}

			SimpleFeature feature = featureWriter.next();
			feature.setDefaultGeometry(geometry);
			p.attributes.saveAttributes(feature);
			featureWriter.write();
		}
	}

	public boolean isFilled()
	{
		if (filledNoHoles == null)
		{
			double sectorArea = sector.getDeltaLatitude() * sector.getDeltaLongitude();
			double polygonArea = 0;
			for (TileRecord record : records)
			{
				polygonArea += record.area();
			}
			filledNoHoles = polygonArea >= sectorArea - 1e-10;
		}
		return filledNoHoles;
	}
}
