package au.gov.ga.worldwind.layers.shapefile.indextessellator;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Point;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;

import au.gov.ga.worldwind.layers.shapefile.FastShape;
import au.gov.ga.worldwind.util.Util;

import com.sun.opengl.util.BufferUtil;

public class PolygonTessellator
{
	public static List<FastShape> tessellateShapefile(Shapefile shapefile, Sector sector,
			int subdivisions)
	{
		//subdivisions = 3;

		List<FastShape> shapes = new ArrayList<FastShape>();
		List<ShapefileRecord> records = shapefile.getRecords();
		for (ShapefileRecord record : records)
		{
			FastShape shape = tessellateRecord(record, sector, subdivisions);
			shapes.add(shape);
		}
		return shapes;
	}

	public static FastShape tessellateRecord(ShapefileRecord record, Sector sector, int subdivisions)
	{
		if (subdivisions <= 0)
			throw new IllegalArgumentException("Number of subdivisions must be greater than zero");

		List<IndexedLatLon> vertices = new ArrayList<IndexedLatLon>();

		double deltaLat = sector.getDeltaLatDegrees() / subdivisions;
		double deltaLon = sector.getDeltaLonDegrees() / subdivisions;
		SubTile[] tiles = new SubTile[subdivisions * subdivisions];
		IndexedLatLon[] tileCorners = new IndexedLatLon[(subdivisions + 1) * (subdivisions + 1)];

		for (int y = 0; y <= subdivisions; y++)
		{
			for (int x = 0; x <= subdivisions; x++)
			{
				int index = y * (subdivisions + 1) + x;
				double lat = sector.getMinLatitude().degrees + deltaLat * y;
				double lon = sector.getMinLongitude().degrees + deltaLon * x;
				IndexedLatLon latlon = IndexedLatLon.fromDegrees(lat, lon);
				tileCorners[index] = latlon;
			}
		}

		for (int y = 0; y < subdivisions; y++)
		{
			for (int x = 0; x < subdivisions; x++)
			{
				int index = y * subdivisions + x;
				IndexedLatLon minmin = tileCorners[index + y];
				IndexedLatLon minmax = tileCorners[index + y + 1];
				IndexedLatLon maxmax = tileCorners[index + y + subdivisions + 2];
				IndexedLatLon maxmin = tileCorners[index + y + subdivisions + 1];
				tiles[index] = new SubTile(x, y, minmin, minmax, maxmax, maxmin);
			}
		}

		int shapeId = 0;
		for (int part = 0; part < record.getNumberOfParts(); part++)
		{
			shapeId++;

			VecBuffer buffer = record.getBuffer(part);
			int size = buffer.getSize();
			int start = vertices.size();
			for (int i = 0; i < size; i++)
			{
				LatLon latlon = buffer.getLocation(i);
				IndexedLatLon ill = new IndexedLatLon(latlon);
				ill.index(vertices);
			}

			IndexedLatLon lastLatlon = null;
			SubTile lastTile = null;
			int lastTileIndex = -1;
			List<SubTile> tilesAffected = new ArrayList<SubTile>();

			//sample first point twice (complete the polygon)
			for (int i = 0; i <= size; i++)
			{
				int index = (i % size) + start;
				IndexedLatLon latlon = vertices.get(index);

				int x =
						(int) ((latlon.longitude.degrees - sector.getMinLongitude().degrees) / deltaLon);
				int y =
						(int) ((latlon.latitude.degrees - sector.getMinLatitude().degrees) / deltaLat);
				x = Util.limitRange(x, 0, subdivisions - 1);
				y = Util.limitRange(y, 0, subdivisions - 1);

				int tileIndex = y * subdivisions + x;
				SubTile tile = tiles[tileIndex];

				if (!tile.contains(latlon))
				{
					String message = latlon + " outside bounds";
					Logging.logger().warning(message);

					//replace current vertex with a new one that is within the tile's bounds
					LatLon newLL = tile.limitLatLonWithinTile(latlon);
					IndexedLatLon newILL = new IndexedLatLon(newLL);
					latlon.replace(vertices, newILL);
					latlon = newILL;
				}

				if (tile != lastTile)
				{
					if (lastTile != null)
					{
						//get the fractional position of the points within their corresponding tile's
						//sector, in the range -0.5 to 0.5
						float x1 = (float) (lastTile.lonFract(lastLatlon) - 0.5);
						float y1 = (float) (lastTile.latFract(lastLatlon) - 0.5);
						float x2 = (float) (tile.lonFract(latlon) - 0.5);
						float y2 = (float) (tile.latFract(latlon) - 0.5);

						//calculate a line between the lastTile and this tile
						List<Point> line =
								linePoints(lastTile.x + x1, lastTile.y + y1, tile.x + x2, tile.y
										+ y2);

						SubTile lastCrossTile = lastTile;
						for (int j = 1; j < line.size() - 1; j++)
						{
							Point p = line.get(j);
							p.x = Util.limitRange(p.x, 0, subdivisions - 1);
							p.y = Util.limitRange(p.y, 0, subdivisions - 1);

							int crossTileIndex = p.y * subdivisions + p.x;
							//ignore first and last
							if (crossTileIndex == tileIndex || crossTileIndex == lastTileIndex)
								continue;

							SubTile crossTile = tiles[crossTileIndex];
							if (crossTile == lastCrossTile) //not required?
								continue;

							LatLon edge = edgePoint(lastLatlon, latlon, crossTile);
							if (edge != null)
							{
								if (lastCrossTile.x != crossTile.x
										&& lastCrossTile.y != crossTile.y)
								{
									String message = "Crossed tiles are not adjacent";
									Logging.logger().warning(message);
								}

								IndexedLatLon edgeIndexed = new IndexedLatLon(edge);
								edgeIndexed.index(vertices);
								int vertexIndex = edgeIndexed.getIndex();

								lastCrossTile.addVertex(shapeId, vertexIndex, false, true);
								crossTile.addVertex(shapeId, vertexIndex, true, false);

								tilesAffected.add(crossTile);
								lastCrossTile = crossTile;
							}
						}

						LatLon edge = edgePoint(lastLatlon, latlon, tile);
						if (edge == null)
						{
							String message = "Did not find an edge point in the sector";
							Logging.logger().warning(message);

							//edge = latlon; TODO ???
						}

						if (edge != null)
						{
							IndexedLatLon edgeIndexed = new IndexedLatLon(edge);
							edgeIndexed.index(vertices);
							int vertexIndex = edgeIndexed.getIndex();

							lastCrossTile.addVertex(shapeId, vertexIndex, false, true);
							tile.addVertex(shapeId, vertexIndex, true, false);
						}
					}

					tilesAffected.add(tile);
				}

				tile.addVertex(shapeId, index, false, false);

				lastTile = tile;
				lastLatlon = latlon;
				lastTileIndex = tileIndex;
			}

			for (SubTile tile : tilesAffected)
				tile.joinOrphanPolygons(shapeId);

			markFilledTilesInside(tiles, tilesAffected, subdivisions);
		}

		for (SubTile tile : tiles)
		{
			tile.completePolygons(vertices);
		}

		List<Position> positions = new ArrayList<Position>(vertices.size());
		double[] dVertices = new double[3 * vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
		{
			LatLon v = vertices.get(i);
			positions.add(new Position(v, 0));
			dVertices[i * 3] = v.longitude.degrees;
			dVertices[i * 3 + 1] = v.latitude.degrees;
			dVertices[i * 3 + 2] = 0;
		}

		GLU glu = new GLU();
		GLUtessellator tess = glu.gluNewTess();
		TessellatorCallback callback = new TessellatorCallback(glu);

		glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
		//glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);

		glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_END, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, callback);
		glu.gluTessNormal(tess, 0.0, 0.0, 1.0);

		for (SubTile tile : tiles)
		{
			List<IndexedContour> contours = tile.getContours();
			if (contours.isEmpty())
				continue;

			int pointCount = 0;
			for (IndexedContour contour : contours)
				pointCount += contour.indices.size();

			if (pointCount == 0)
				continue;

			glu.gluTessBeginPolygon(tess, null);

			for (IndexedContour contour : contours)
			{
				if (contour.indices.isEmpty())
					continue;

				glu.gluTessBeginContour(tess);

				for (int i = contour.indices.size(); i >= 0; i--)
				{
					Integer index = contour.indices.get(i % contour.indices.size());
					glu.gluTessVertex(tess, dVertices, index * 3, index);
				}

				glu.gluTessEndContour(tess);
			}

			glu.gluTessEndPolygon(tess);
		}

		glu.gluDeleteTess(tess);

		List<Integer> indices = callback.indices;
		IntBuffer ib = BufferUtil.newIntBuffer(indices.size());
		for (Integer i : indices)
			ib.put(i);

		FastShape shape = new FastShape(positions, new IntBuffer[] { ib }, GL.GL_TRIANGLES);
		return shape;

		/*List<IntBuffer> indices = new ArrayList<IntBuffer>();
		for (SubTile tile : tiles)
		{
			List<IndexedContour> contours = tile.getContours();
			for (IndexedContour contour : contours)
			{
				IntBuffer ib = BufferUtil.newIntBuffer((contour.indices.size() - 1) * 2);
				indices.add(ib);
				Integer lastI = null;
				for (Integer i : contour.indices)
				{
					if (lastI != null)
					{
						ib.put(lastI);
						ib.put(i);
					}
					lastI = i;
				}
			}
		}

		IntBuffer[] ibs = indices.toArray(new IntBuffer[indices.size()]);
		FastShape shape = new FastShape(positions, ibs, GL.GL_LINES);
		return shape;*/
	}

	private static List<Point> linePoints(float x1, float y1, float x2, float y2)
	{
		List<Point> points = new ArrayList<Point>();

		boolean steep = Math.abs(x2 - x1) < Math.abs(y2 - y1);
		if (steep)
		{
			//swap x/y variables
			float temp = x1;
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
			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		int y1i = Math.round(y1);
		int y2i = Math.round(y2);
		if (y1i == y2i)
		{
			//handle special horizontal/vertical line case

			int x1i = Math.round(x1);
			int x2i = Math.round(x2);
			for (int x = x1i; x <= x2i; x++)
			{
				points.add(steep ? new Point(y1i, x) : new Point(x, y1i));
			}
		}
		else
		{
			float dx = x2 - x1;
			float dy = y2 - y1;
			float gradient = dy / dx;
			float intery = y1;
			int startX = Math.round(x1);
			int endX = Math.round(x2);
			float centerDiff = (startX - x1) * gradient;

			for (int x = startX; x <= endX; x++)
			{
				int y = Math.round(intery);
				int add = (intery + centerDiff - y < 0) ? -1 : 1;
				boolean first = add > 0 ^ gradient < 0;

				Point p1 = steep ? new Point(y, x) : new Point(x, y);
				Point p2 = steep ? new Point(y + add, x) : new Point(x, y + add);

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
	private static LatLon edgePoint(LatLon p1, LatLon p2, SubTile tile)
	{
		LatLon minmin = tile.minmin;
		LatLon minmax = tile.minmax;
		LatLon maxmax = tile.maxmax;
		LatLon maxmin = tile.maxmin;

		LatLon minXintersect = lineSegmentIntersection(p1, p2, minmin, maxmin);
		LatLon minYintersect = lineSegmentIntersection(p1, p2, minmin, minmax);
		LatLon maxXintersect = lineSegmentIntersection(p1, p2, minmax, maxmax);
		LatLon maxYintersect = lineSegmentIntersection(p1, p2, maxmin, maxmax);
		LatLon[] intersects =
				new LatLon[] { minXintersect, minYintersect, maxXintersect, maxYintersect };
		double minDistance = Double.MAX_VALUE;
		LatLon minDistanceIntersect = null;

		for (LatLon intersect : intersects)
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

	private static void markFilledTilesInside(SubTile[] tiles, List<SubTile> tilesAffected,
			int subdivisions)
	{
		if (tilesAffected.get(0) != tilesAffected.get(tilesAffected.size() - 1))
		{
			throw new IllegalStateException(
					"First tile doesn't equal last tile; cannot fill inside polygon");
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

				SubTile curr = tilesAffected.get(i);
				int index = curr.y * subdivisions + curr.x;

				//update col/row bounds of tiles affected (to make loop below faster)
				minx = Math.min(minx, curr.x);
				maxx = Math.max(maxx, curr.x);
				miny = Math.min(miny, curr.y);
				maxy = Math.max(maxy, curr.y);

				//has this tile appeared in the list before? retrieve the last value
				int crossings = 0;
				if (crossingsMap.containsKey(index))
					crossings = crossingsMap.get(index);

				//find out the spatial relationship between the prev/next tiles
				SubTile prev = tilesAffected.get((i - 1 + count) % count);
				SubTile next = tilesAffected.get((i + 1) % count);
				int prevYDelta = curr.y - prev.y;
				int nextYDelta = curr.y - next.y;

				//increment crossings if either the previous tile or next tile is above
				//the current tile, but not both
				if (prevYDelta >= 1 ^ nextYDelta >= 1)
					crossings++;

				//update the crossings map
				crossingsMap.put(index, crossings);
			}

			//fill in the tiles within the polygon but not touched by the polygon
			for (int y = 0; y < subdivisions; y++)
			{
				int crossings = 0;

				//move along each scanline, filling in tiles for which the sum of crossings before the tile is odd
				for (int x = 0; x < subdivisions; x++)
				{
					int index = y * subdivisions + x;
					if (crossingsMap.containsKey(index))
					{
						//tile was entered by polygon, so don't need to fill
						crossings += crossingsMap.get(index);
					}
					else if (crossings % 2 == 1)
					{
						//crossings is odd, so fill in tile
						SubTile tile = tiles[index];
						tile.markFilled();
					}
				}
			}
		}
	}
}
