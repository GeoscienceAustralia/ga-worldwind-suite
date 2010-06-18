package au.gov.ga.worldwind.layers.shapefile.tessellator;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.VecBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;

import au.gov.ga.worldwind.layers.shapefile.FastShape;
import au.gov.ga.worldwind.util.Util;

public class PolygonTessellator
{
	public static List<FastShape> tessellateShapefile(Shapefile shapefile, Sector sector,
			int subdivisions)
	{
		subdivisions = 4;

		if (subdivisions <= 0)
			throw new IllegalArgumentException("Number of subdivisions must be greater than zero");

		List<FastShape> shapes = new ArrayList<FastShape>();

		double deltaLat = sector.getDeltaLatDegrees() / subdivisions;
		double deltaLon = sector.getDeltaLonDegrees() / subdivisions;
		SubTile[] tiles = new SubTile[subdivisions * subdivisions];

		for (int y = 0; y < subdivisions; y++)
		{
			for (int x = 0; x < subdivisions; x++)
			{
				int index = y * subdivisions + x;
				double lon1 = sector.getMinLongitude().degrees + deltaLon * x;
				double lat1 = sector.getMinLatitude().degrees + deltaLat * y;
				double lon2 = lon1 + deltaLon;
				double lat2 = lat1 + deltaLat;
				Sector s = Sector.fromDegrees(lat1, lat2, lon1, lon2);
				tiles[index] = new SubTile(s, x, y);
			}
		}

		int shapeId = 0;
		List<ShapefileRecord> records = shapefile.getRecords();
		for (ShapefileRecord record : records)
		{
			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				shapeId++;
				Position lastPosition = null;
				SubTile lastTile = null;
				int lastTileIndex = -1;
				List<SubTile> tilesAffected = new ArrayList<SubTile>();

				VecBuffer buffer = record.getBuffer(part);
				int size = buffer.getSize();
				for (int i = 0; i <= size; i++)
				{
					Position position = buffer.getPosition(i % size);
					int x =
							(int) ((position.longitude.degrees - sector.getMinLongitude().degrees) / deltaLon);
					int y =
							(int) ((position.latitude.degrees - sector.getMinLatitude().degrees) / deltaLat);
					x = Util.limitRange(x, 0, subdivisions - 1);
					y = Util.limitRange(y, 0, subdivisions - 1);

					int tileIndex = y * subdivisions + x;
					SubTile tile = tiles[tileIndex];
					if (!tile.sector.contains(position))
					{
						//TODO log properly
						System.out.println("WARNING: " + position + " outside bounds");
						position = Util.limitPosition(position, tile.sector);
					}

					if (tile != lastTile)
					{
						if (lastTile != null)
						{
							lastTile.exitShape(shapeId, lastPosition, position);

							int diffX = Math.abs(lastTile.x - tile.x);
							int diffY = Math.abs(lastTile.y - tile.y);
							if (diffX + diffY > 1)
							{
								//If tile is not next (vertically/horizontally, not diagonally) to lastTile,
								//it means that the points have moved diagonally from one tile, across other
								//tile(s) (with no points inside), into another tile. We must add two edge
								//points to the in-between tile(s) (an entry and an exit).

								//get the fractional position of the points within their corresponding tile's
								//sector, in the range -0.5 to 0.5
								float x1 =
										(float) ((lastPosition.longitude.degrees - lastTile.sector
												.getMinLongitude().degrees)
												/ lastTile.sector.getDeltaLonDegrees() - 0.5);
								float y1 =
										(float) ((lastPosition.latitude.degrees - lastTile.sector
												.getMinLatitude().degrees)
												/ lastTile.sector.getDeltaLatDegrees() - 0.5);
								float x2 =
										(float) ((position.longitude.degrees - tile.sector
												.getMinLongitude().degrees)
												/ tile.sector.getDeltaLonDegrees() - 0.5);
								float y2 =
										(float) ((position.latitude.degrees - tile.sector
												.getMinLatitude().degrees)
												/ tile.sector.getDeltaLatDegrees() - 0.5);

								//create a list of possible tiles that may be affected by the line between the two tiles
								List<java.awt.Point> line =
										linePoints(lastTile.x + x1, lastTile.y + y1, tile.x + x2,
												tile.y + y2);

								//loop through each tile, calling crossShape() (ignoring the first/last tiles)
								for (int j = 1; j < line.size() - 1; j++)
								{
									java.awt.Point p = line.get(j);
									p.x = Util.limitRange(p.x, 0, subdivisions - 1);
									p.y = Util.limitRange(p.y, 0, subdivisions - 1);

									int crossTileIndex = p.y * subdivisions + p.x;

									//ignore tile if it the first or last (this can happen if line endpoints
									//are on the edges of a tile's sector
									if (crossTileIndex == tileIndex
											|| crossTileIndex == lastTileIndex)
										continue;

									SubTile crossTile = tiles[crossTileIndex];
									boolean crossed =
											crossTile.crossShape(shapeId, lastPosition, position);
									if (crossed)
									{
										tilesAffected.add(crossTile);
									}
								}
							}
						}

						tilesAffected.add(tile);

						if (lastPosition != null)
						{
							tile.enterShape(shapeId, lastPosition, position);
						}
						else
						{
							tile.continueShape(shapeId, position);
						}
					}
					else
					{
						tile.continueShape(shapeId, position);
					}

					lastTile = tile;
					lastPosition = position;
					lastTileIndex = tileIndex;
				}

				for (SubTile tile : tilesAffected)
					tile.joinOrphanPolygons(shapeId);

				markFilledTilesInside(tiles, tilesAffected, subdivisions);
			}
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

		for (SubTile tile : tiles)
		{
			tile.completePolygons();

			List<SubTileContour> contours = tile.getContours();
			if (contours.isEmpty())
				continue;

			int pointCount = 0;
			for (SubTileContour contour : contours)
				pointCount += contour.points.size();

			if (pointCount == 0)
				continue;

			glu.gluTessBeginPolygon(tess, null);

			for (SubTileContour contour : contours)
			{
				if (contour.points.isEmpty())
					continue;

				glu.gluTessBeginContour(tess);

				for (int i = contour.points.size(); i >= 0; i--)
				{
					LatLon ll = contour.points.get(i % contour.points.size());
					double[] v = new double[] { ll.longitude.degrees, ll.latitude.degrees, 0 };
					glu.gluTessVertex(tess, v, 0, v);
				}

				glu.gluTessEndContour(tess);
			}

			glu.gluTessEndPolygon(tess);
		}

		glu.gluDeleteTess(tess);

		FastShape shape = callback.getShape();
		shapes.add(shape);

		/*for (SubTile tile : tiles)
		{
			tile.completePolygons();

			List<SubTileContour> contours = tile.getContours();
			for (SubTileContour contour : contours)
			{
				List<Position> positions = new ArrayList<Position>();
				LatLon last = null;
				for (int i = 0; i < contour.points.size() + 1; i++)
				{
					LatLon ll = contour.points.get(i % contour.points.size());
					ll =
							LatLon
									.fromDegrees(
											ll.latitude.degrees
													- (ll.latitude.degrees
															- tile.sector.getMinLatitude().degrees - tile.sector
															.getDeltaLatDegrees() / 2d) / 20d,
											ll.longitude.degrees
													- (ll.longitude.degrees
															- tile.sector.getMinLongitude().degrees - tile.sector
															.getDeltaLonDegrees() / 2d) / 20d);

					if (last != null)
					{
						positions.add(new Position(last, 0));
						positions.add(new Position(ll, 0));
					}

					last = ll;
				}

				FastShape shape2 = new FastShape(positions, GL.GL_LINES);
				shapes.add(shape2);
			}
		}*/

		return shapes;
	}

	private static List<java.awt.Point> linePoints(float x1, float y1, float x2, float y2)
	{
		List<java.awt.Point> points = new ArrayList<java.awt.Point>();

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
				points.add(steep ? new java.awt.Point(y1i, x) : new java.awt.Point(x, y1i));
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

				java.awt.Point p1 = steep ? new java.awt.Point(y, x) : new java.awt.Point(x, y);
				java.awt.Point p2 =
						steep ? new java.awt.Point(y + add, x) : new java.awt.Point(x, y + add);

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
