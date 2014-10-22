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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import au.gov.ga.worldwind.tiler.util.LatLon;
import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class used to tile shapefiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShapefileTiler
{
	/**
	 * Tile the given shapefile.
	 * 
	 * @param input
	 *            Input shapefile
	 * @param output
	 *            Output directory
	 * @param level
	 *            Level at which to tile
	 * @param lzts
	 *            Level zero tile size (in degrees)
	 * @param origin
	 *            Origin to begin tiling at
	 * @param progress
	 *            Object to report progress
	 */
	public static void tile(File input, File output, int level, double lzts, LatLon origin, ProgressReporter progress)
	{
		ShapefileDataStore dataStore = null;
		try
		{
			progress.getLogger().info("Parsing " + input);

			ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
			dataStore = (ShapefileDataStore) factory.createDataStore(input.toURI().toURL());
			ContentFeatureSource featureSource = dataStore.getFeatureSource();
			ContentFeatureCollection featureCollection = featureSource.getFeatures();
			SimpleFeatureType schema = dataStore.getSchema();

			GeometryFactory geometryFactory = new GeometryFactory();

			ReferencedEnvelope bounds = featureSource.getBounds();
			Sector sector = new Sector(bounds.getMinY(), bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX());

			//TEMP
			//sector = new Sector(85, -180, 90, 180);
			//TEMP

			double tilesizedegrees = Math.pow(0.5, level) * lzts;
			int minX = Util.getTileX(sector.getMinLongitude() + 1e-10, origin, level, lzts);
			int maxX = Util.getTileX(sector.getMaxLongitude() - 1e-10, origin, level, lzts);
			int minY = Util.getTileY(sector.getMinLatitude() + 1e-10, origin, level, lzts);
			int maxY = Util.getTileY(sector.getMaxLatitude() - 1e-10, origin, level, lzts);

			java.awt.Point min = new java.awt.Point(minX, minY);
			Dimension size = new Dimension(maxX - minX + 1, maxY - minY + 1);
			ShapefileTile[] tiles = new ShapefileTile[size.width * size.height];

			for (int y = minY; y <= maxY; y++)
			{
				int y0 = y - minY;
				for (int x = minX; x <= maxX; x++)
				{
					int x0 = x - minX;
					double lat1 = (y * tilesizedegrees) - 90;
					double lon1 = (x * tilesizedegrees) - 180;
					double lat2 = lat1 + tilesizedegrees;
					double lon2 = lon1 + tilesizedegrees;
					Sector ts = new Sector(lat1, lon1, lat2, lon2);

					ShapefileTile tile = new ShapefileTile(ts, x, y);
					//TEMP
					//Attributes attributes = new Attributes(schema);
					//attributes.values[0] = -1;
					//tile.markFilled(attributes);
					//TEMP
					tiles[y0 * size.width + x0] = tile;
				}
			}

			boolean anyPolygons = false;
			Boolean lastPolygon = null;

			//TEMP
			//anyPolygons = true;
			//TEMP

			progress.getLogger().info("Reading records");
			int shapeId = 0;
			SimpleFeatureIterator features = featureCollection.features();
			try
			{
				while (features.hasNext())
				{
					if (progress.isCancelled())
						return;

					SimpleFeature feature = features.next();
					Object geometry = feature.getDefaultGeometry();
					boolean polygon = geometry instanceof MultiPolygon || geometry instanceof Polygon;

					anyPolygons |= polygon;
					if (lastPolygon != null && polygon != lastPolygon.booleanValue())
					{
						progress.getLogger().warning("Polygons mixed with non-polygons");
					}
					lastPolygon = polygon;

					Attributes attributes = new Attributes(schema);
					attributes.loadAttributes(feature);

					if (geometry instanceof MultiPolygon)
					{
						MultiPolygon mp = (MultiPolygon) geometry;
						shapeId =
								addMultiPolygon(shapeId, mp, attributes, tiles, level, lzts, origin, min, size,
										progress);
					}
					else if (geometry instanceof Polygon)
					{
						Polygon p = (Polygon) geometry;
						shapeId = addPolygon(shapeId, p, attributes, tiles, level, lzts, origin, min, size, progress);
					}
					else if (geometry instanceof LinearRing)
					{
						LinearRing lr = (LinearRing) geometry;
						shapeId =
								addLinearRing(shapeId, lr, attributes, tiles, level, lzts, origin, min, size, true,
										progress);
					}
					else if (geometry instanceof MultiLineString)
					{
						MultiLineString mls = (MultiLineString) geometry;
						shapeId =
								addMultiLineString(shapeId, mls, attributes, tiles, level, lzts, origin, min, size,
										progress);
					}
					else if (geometry instanceof LineString)
					{
						LineString ls = (LineString) geometry;
						shapeId =
								addLineString(shapeId, ls, attributes, tiles, level, lzts, origin, min, size, progress);
					}
					else
					{
						progress.getLogger().severe("Unsupported shape type: " + geometry);
					}
				}
			}
			finally
			{
				features.close();
			}

			progress.getLogger().info("Saving tiles");

			int amount = 0;
			for (ShapefileTile tile : tiles)
			{
				if (progress.isCancelled())
					return;

				amount++;
				progress.progress(amount / (double) tiles.length);

				double minimumArea = 0; //1e-8;
				if (anyPolygons)
					tile.completePolygons(minimumArea);

				File rowDir = new File(output, String.valueOf(level));
				rowDir = new File(rowDir, Util.paddedInt(tile.row, 4));
				if (!rowDir.exists())
					rowDir.mkdirs();

				File dst = new File(rowDir, Util.paddedInt(tile.row, 4) + "_" + Util.paddedInt(tile.col, 4) + ".zip");

				saveShapefileZip(tile, schema, geometryFactory, dst, anyPolygons, progress);
			}

			progress.done();
		}
		catch (Exception e)
		{
			progress.getLogger().log(Level.SEVERE, "Error tiling shapefile", e);
			e.printStackTrace();
		}
		finally
		{
			if (dataStore != null)
				dataStore.dispose();
		}
	}

	protected static void saveShapefileZip(ShapefileTile tile, SimpleFeatureType schema, GeometryFactory factory,
			File file, boolean polygon, ProgressReporter progress) throws IOException
	{
		if (file.exists())
		{
			progress.getLogger().warning(file + " already exists");
			return;
		}
		if (tile.recordCount() <= 0)
		{
			return;
		}

		int indexOfDot = file.getName().lastIndexOf('.');
		if (indexOfDot < 0)
			throw new IllegalArgumentException("Filename does not have an extension");

		final String filenameNoExt = file.getName().substring(0, indexOfDot);
		File shpFile = new File(file.getParentFile(), filenameNoExt + ".shp");

		boolean deleteZippedFiles = false;
		if (shpFile.exists())
		{
			progress.getLogger().warning(shpFile + " already exists");
		}
		else
		{
			ShapefileDataStore store = null;
			try
			{
				store = new ShapefileDataStore(shpFile.toURI().toURL());
				store.createSchema(schema);
				FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter =
						store.getFeatureWriter(store.getTypeNames()[0], Transaction.AUTO_COMMIT);
				try
				{
					tile.writeFeatures(featureWriter, schema, factory, polygon);
				}
				finally
				{
					featureWriter.close();
				}
			}
			finally
			{
				store.dispose();
			}

			deleteZippedFiles = true;
		}

		//list all filenames with the same prefix (different extensions)
		File[] files = file.getParentFile().listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().startsWith(filenameNoExt);
			}
		});

		//zip all files into the one zip file
		ZipOutputStream out = null;
		try
		{
			byte[] buf = new byte[1024];
			out = new ZipOutputStream(new FileOutputStream(file));
			for (File f : files)
			{
				FileInputStream in = null;
				try
				{
					in = new FileInputStream(f);
					out.putNextEntry(new ZipEntry(f.getName()));
					int len;
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					out.closeEntry();
				}
				finally
				{
					if (in != null)
						in.close();
				}
			}
		}
		finally
		{
			if (out != null)
				out.close();
		}

		if (deleteZippedFiles)
		{
			//delete all the files that we just zipped
			for (File f : files)
			{
				f.delete();
			}
		}
	}

	protected static int addMultiPolygon(int shapeId, MultiPolygon polygon, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, LatLon origin, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		for (int i = 0; i < polygon.getNumGeometries(); i++)
		{
			Geometry g = polygon.getGeometryN(i);
			if (g instanceof Polygon)
			{
				Polygon p = (Polygon) g;
				shapeId = addPolygon(shapeId, p, attributes, tiles, level, lzts, origin, min, size, progress);
			}
		}
		return shapeId;
	}

	protected static int addPolygon(int shapeId, Polygon polygon, Attributes attributes, ShapefileTile[] tiles,
			int level, double lzts, LatLon origin, java.awt.Point min, Dimension size, ProgressReporter progress)
	{
		LineString shell = polygon.getExteriorRing();
		shapeId = addLinearRing(shapeId, shell, attributes, tiles, level, lzts, origin, min, size, true, progress);

		for (int i = 0; i < polygon.getNumInteriorRing(); i++)
		{
			LineString hole = polygon.getInteriorRingN(i);
			ShapefileTile containing = allPointsWithin(hole, tiles, level, lzts, origin, min, size);
			if (containing != null)
			{
				addHole(hole, containing, attributes);
			}
			else
			{
				shapeId =
						addLinearRing(shapeId, hole, attributes, tiles, level, lzts, origin, min, size, false, progress);
			}
		}
		return shapeId;
	}

	protected static int addLinearRing(int shapeId, LineString ring, Attributes attributes, ShapefileTile[] tiles,
			int level, double lzts, LatLon origin, java.awt.Point min, Dimension size, boolean fillInside,
			ProgressReporter progress)
	{
		return addPoints(shapeId, ring, attributes, true, fillInside, tiles, level, lzts, origin, min, size, progress);
	}

	protected static int addMultiLineString(int shapeId, MultiLineString multiLineString, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, LatLon origin, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		for (int i = 0; i < multiLineString.getNumGeometries(); i++)
		{
			Geometry g = multiLineString.getGeometryN(i);
			if (g instanceof LineString)
			{
				LineString ls = (LineString) g;
				shapeId = addLineString(shapeId, ls, attributes, tiles, level, lzts, origin, min, size, progress);
			}
		}
		return shapeId;
	}

	protected static int addLineString(int shapeId, LineString lineString, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, LatLon origin, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		return addPoints(shapeId, lineString, attributes, false, false, tiles, level, lzts, origin, min, size, progress);
	}

	protected static int addPoints(int shapeId, LineString lineString, Attributes attributes, boolean polygon,
			boolean fillInside, ShapefileTile[] tiles, int level, double lzts, LatLon origin, java.awt.Point min,
			Dimension size, ProgressReporter progress)
	{
		Coordinate lastCoordinate = null;
		ShapefileTile lastTile = null;
		int lastTileIndex = -1;

		List<ShapefileTile> tilesAffected = new ArrayList<ShapefileTile>();
		for (int i = 0; i < lineString.getNumPoints(); i++)
		{
			Coordinate coordinate = lineString.getCoordinateN(i);

			int x = Util.getTileX(coordinate.x, origin, level, lzts);
			int y = Util.getTileY(coordinate.y, origin, level, lzts);

			//limit tile x/y on the edges
			//(eg lon=180 will resolve to x=11 at level 0 lzts 36, but should be x=10)
			int x0 = Util.clamp(x - min.x, 0, size.width - 1);
			int y0 = Util.clamp(y - min.y, 0, size.height - 1);

			int tileIndex = y0 * size.width + x0;
			ShapefileTile tile = tiles[tileIndex];

			if (!tile.contains(coordinate))
			{
				progress.getLogger().warning(coordinate + " outside bounds " + tile.sector());

				//move coordinate within the tile's bounds
				coordinate = tile.limitCoordinateWithinTile(coordinate);
			}

			if (tile != lastTile)
			{
				if (lastTile != null)
				{
					//get the fractional position of the points within their corresponding tile's
					//sector, in the range -0.5 to 0.5
					double x1 = lastTile.longitudeFract(lastCoordinate) - 0.5;
					double y1 = lastTile.latitudeFract(lastCoordinate) - 0.5;
					double x2 = tile.longitudeFract(coordinate) - 0.5;
					double y2 = tile.latitudeFract(coordinate) - 0.5;

					//create a list of possible tiles that may be affected by the line between the two tiles
					List<Point> line = linePoints(
							(lastTile.col - min.x) + x1, (lastTile.row - min.y) + y1,
							(tile.col - min.x) + x2, (tile.row - min.y) + y2);

					ShapefileTile lastCrossTile = lastTile;
					for (int j = 1; j < line.size() - 1; j++)
					{
						Point p = line.get(j);
						p.x = Util.clamp(p.x, 0, size.width - 1);
						p.y = Util.clamp(p.y, 0, size.height - 1);

						int crossTileIndex = p.y * size.width + p.x;
						//ignore first and last
						if (crossTileIndex == tileIndex || crossTileIndex == lastTileIndex)
							continue;

						ShapefileTile crossTile = tiles[crossTileIndex];
						if (crossTile == lastCrossTile) //not required?
							continue;

						Coordinate edge = edgePoint(lastCoordinate, coordinate, crossTile);
						if (edge != null)
						{
							if (lastCrossTile.col != crossTile.col && lastCrossTile.row != crossTile.row)
							{
								String message = "Crossed tiles are not adjacent";
								progress.getLogger().warning(message);
							}

							lastCrossTile.addCoordinate(shapeId, edge, false, true, attributes);
							crossTile.addCoordinate(shapeId, edge, true, false, attributes);

							tilesAffected.add(crossTile);
							lastCrossTile = crossTile;
						}
					}

					Coordinate edge = edgePoint(lastCoordinate, coordinate, tile);
					if (edge == null)
					{
						String message = "Did not find an edge point in the sector";
						progress.getLogger().warning(message);

						//edge = latlon; TODO ???
					}

					if (edge != null)
					{
						lastCrossTile.addCoordinate(shapeId, edge, false, true, attributes);
						tile.addCoordinate(shapeId, edge, true, false, attributes);
					}
				}

				if (polygon)
					tilesAffected.add(tile);
			}

			tile.addCoordinate(shapeId, coordinate, false, false, attributes);

			lastTile = tile;
			lastCoordinate = coordinate;
			lastTileIndex = tileIndex;
		}

		if (polygon)
		{
			for (ShapefileTile tile : tilesAffected)
			{
				tile.joinOrphanPolygons(progress);
			}

			if (fillInside)
			{
				markFilledTilesInside(tiles, tilesAffected, min, size, attributes);
			}
		}

		return shapeId + 1;
	}

	protected static ShapefileTile allPointsWithin(LineString lineString, ShapefileTile[] tiles, int level,
			double lzts, LatLon origin, java.awt.Point min, Dimension size)
	{
		if (lineString.isEmpty())
			return null;

		Integer X = null, Y = null;
		for (int i = 0; i < lineString.getNumPoints(); i++)
		{
			Coordinate coordinate = lineString.getCoordinateN(i);
			int x = Util.getTileX(coordinate.x, origin, level, lzts);
			int y = Util.getTileY(coordinate.y, origin, level, lzts);

			x = Util.clamp(x, min.x, min.x + size.width - 1);
			y = Util.clamp(y, min.y, min.y + size.height - 1);

			//if tile has changed
			if (X != null && X != x)
				return null;
			if (Y != null && Y != y)
				return null;

			X = x;
			Y = y;
		}

		int x0 = X - min.x;
		int y0 = Y - min.y;
		int tileIndex = y0 * size.width + x0;
		return tiles[tileIndex];
	}

	protected static void addHole(LineString lineString, ShapefileTile tile, Attributes attributes)
	{
		List<Coordinate> coordinates = new ArrayList<Coordinate>(lineString.getNumPoints());
		for (int i = 0; i < lineString.getNumPoints(); i++)
		{
			Coordinate coordinate = lineString.getCoordinateN(i);
			coordinates.add(coordinate);
		}

		tile.addHole(coordinates, attributes);
	}

	protected static void markFilledTilesInside(ShapefileTile[] tiles, List<ShapefileTile> tilesAffected,
			java.awt.Point min, Dimension size, Attributes attributes)
	{
		if (tilesAffected.get(0) != tilesAffected.get(tilesAffected.size() - 1))
		{
			throw new IllegalStateException("First tile doesn't equal last tile; cannot fill inside polygon");
		}

		//ignore the last tile (it is the same as the first)
		int count = tilesAffected.size() - 1;

		//check that the list is not empty (ignoring the last tile)
		if (count > 0)
		{
			Map<Integer, Integer> crossingsMap = new HashMap<Integer, Integer>();
			int minx, miny, maxx, maxy;
			minx = miny = Integer.MAX_VALUE;
			maxx = maxy = Integer.MIN_VALUE;
			for (int i = 0; i < count; i++)
			{
				//note: tiles may appear multiple tiles in the tilesAffected list;
				//we need to increment crossings int rather than set it

				ShapefileTile curr = tilesAffected.get(i);
				int index = (curr.row - min.y) * size.width + (curr.col - min.x);

				//update col/row bounds of tiles affected (to make loop below faster)
				minx = Math.min(minx, curr.col);
				maxx = Math.max(maxx, curr.col);
				miny = Math.min(miny, curr.row);
				maxy = Math.max(maxy, curr.row);

				//has this tile appeared in the list before? retrieve the last value
				int crossings = 0;
				if (crossingsMap.containsKey(index))
					crossings = crossingsMap.get(index);

				//find out the spatial relationship between the prev/next tiles
				ShapefileTile prev = tilesAffected.get((i - 1 + count) % count);
				ShapefileTile next = tilesAffected.get((i + 1) % count);
				int prevRowDelta = curr.row - prev.row;
				int nextRowDelta = curr.row - next.row;

				//increment crossings if either the previous tile or next tile is above
				//the current tile, but not both
				if (prevRowDelta >= 1 ^ nextRowDelta >= 1)
					crossings++;

				//update the crossings map
				crossingsMap.put(index, crossings);
			}

			//fill in the tiles within the polygon but not touched by the polygon
			for (int y = miny; y <= maxy; y++)
			{
				int y0 = y - min.y;
				int crossings = 0;

				//move along each scanline, filling in tiles for which the sum of crossings before the tile is odd
				for (int x = minx; x <= maxx; x++)
				{
					int x0 = x - min.x;
					int index = y0 * size.width + x0;
					if (crossingsMap.containsKey(index))
					{
						//tile was entered by polygon, so don't need to fill
						crossings += crossingsMap.get(index);
					}
					else if (crossings % 2 == 1)
					{
						//crossings is odd, so fill in tile
						ShapefileTile tile = tiles[index];
						tile.markFilled(attributes);
					}
				}
			}
		}
	}

	protected static List<java.awt.Point> linePoints(double x1, double y1, double x2, double y2)
	{
		List<java.awt.Point> points = new ArrayList<java.awt.Point>();

		boolean steep = Math.abs(x2 - x1) < Math.abs(y2 - y1);
		if (steep)
		{
			//swap x/y variables
			double temp = x1;
			x1 = y1;
			y1 = temp;
			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		boolean swap = x1 > x2;
		if (swap)
		{
			//swap p1/p2 points
			double temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		int y1i = (int) Math.round(y1);
		int y2i = (int) Math.round(y2);
		if (y1i == y2i)
		{
			//handle special horizontal/vertical line case

			int x1i = (int) Math.round(x1);
			int x2i = (int) Math.round(x2);
			for (int x = x1i; x <= x2i; x++)
			{
				points.add(steep ? new java.awt.Point(y1i, x) : new java.awt.Point(x, y1i));
			}
		}
		else
		{
			double dx = x2 - x1;
			double dy = y2 - y1;
			double gradient = dy / dx;
			double intery = y1;
			int startX = (int) Math.round(x1);
			int endX = (int) Math.round(x2);
			double centerDiff = (startX - x1) * gradient;

			for (int x = startX; x <= endX; x++)
			{
				int y = (int) Math.round(intery);
				int add = (intery + centerDiff - y < 0) ? -1 : 1;
				boolean first = add > 0 ^ gradient < 0;

				java.awt.Point p1 = steep ? new java.awt.Point(y, x) : new java.awt.Point(x, y);
				java.awt.Point p2 = steep ? new java.awt.Point(y + add, x) : new java.awt.Point(x, y + add);

				if (first)
				{
					points.add(p1);
					points.add(p2);
				}
				else
				{
					points.add(p2);
					points.add(p1);
				}

				intery += gradient;
			}
		}

		//if points were swapped, reverse the list
		if (swap)
			Collections.reverse(points);

		return points;
	}

	/**
	 * Returns a point on the edge of the tile (if exists) closest to p2
	 */
	private static Coordinate edgePoint(Coordinate p1, Coordinate p2, ShapefileTile tile)
	{
		Coordinate minmin = tile.minmin;
		Coordinate minmax = tile.minmax;
		Coordinate maxmax = tile.maxmax;
		Coordinate maxmin = tile.maxmin;

		Coordinate minXintersect = lineSegmentIntersection(p1, p2, minmin, maxmin);
		Coordinate minYintersect = lineSegmentIntersection(p1, p2, minmin, minmax);
		Coordinate maxXintersect = lineSegmentIntersection(p1, p2, minmax, maxmax);
		Coordinate maxYintersect = lineSegmentIntersection(p1, p2, maxmin, maxmax);
		Coordinate[] intersects = new Coordinate[] { minXintersect, minYintersect, maxXintersect, maxYintersect };
		double minDistance = Double.MAX_VALUE;
		Coordinate minDistanceIntersect = null;

		for (Coordinate intersect : intersects)
		{
			if (intersect != null)
			{
				double distance = distanceSquared(p1, intersect);
				if (distance < minDistance)
				{
					minDistance = distance;
					minDistanceIntersect = intersect;
				}
			}
		}

		return minDistanceIntersect;
	}

	private static Coordinate lineSegmentIntersection(Coordinate p1, Coordinate p2, Coordinate p3, Coordinate p4)
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
	}

	private static double distanceSquared(Coordinate p1, Coordinate p2)
	{
		double delta;
		double result = 0d;
		delta = p1.x - p2.x;
		result += delta * delta;
		delta = p1.y - p2.y;
		result += delta * delta;
		return result;
	}
}
