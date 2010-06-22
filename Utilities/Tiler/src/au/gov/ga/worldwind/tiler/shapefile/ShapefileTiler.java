package au.gov.ga.worldwind.tiler.shapefile;

import gistoolkit.datasources.shapefile.ShapeFileReader;
import gistoolkit.datasources.shapefile.ShapeFileRecord;
import gistoolkit.datasources.shapefile.ShapeFileWriter;
import gistoolkit.features.Envelope;
import gistoolkit.features.LineString;
import gistoolkit.features.LinearRing;
import gistoolkit.features.MultiLineString;
import gistoolkit.features.MultiPolygon;
import gistoolkit.features.Point;
import gistoolkit.features.Polygon;
import gistoolkit.features.Record;
import gistoolkit.features.Shape;

import java.awt.Dimension;
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

import au.gov.ga.worldwind.tiler.util.ProgressReporter;
import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;


public class ShapefileTiler
{
	public static void tile(File input, File output, int level, double lzts,
			ProgressReporter progress)
	{
		try
		{
			progress.getLogger().info("Parsing " + input);

			ShapeFileReader reader = new ShapeFileReader(input.getAbsolutePath());
			Envelope envelope = reader.getHeader().getFileEnvelope();
			Sector sector =
					new Sector(envelope.getMinY(), envelope.getMinX(), envelope.getMaxY(), envelope
							.getMaxX());

			double tilesizedegrees = Math.pow(0.5, level) * lzts;
			int minX = Util.getTileX(sector.getMinLongitude() + 1e-10, level, lzts);
			int maxX = Util.getTileX(sector.getMaxLongitude() - 1e-10, level, lzts);
			int minY = Util.getTileY(sector.getMinLatitude() + 1e-10, level, lzts);
			int maxY = Util.getTileY(sector.getMaxLatitude() - 1e-10, level, lzts);

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

					ShapefileTile tile = new ShapefileTile(ts, level, x, y);
					tiles[y0 * size.width + x0] = tile;
				}
			}

			boolean anyPolygons = false;
			Boolean lastPolygon = null;

			progress.getLogger().info("Reading records");
			ShapeFileRecord record;
			int shapeId = 0;
			while ((record = reader.read()) != null)
			{
				if (progress.isCancelled())
					return;

				Shape shape = record.getShape();
				boolean polygon =
						shape instanceof MultiPolygon || shape instanceof Polygon
								|| shape instanceof LinearRing;

				anyPolygons |= polygon;
				if (lastPolygon != null && polygon != lastPolygon.booleanValue())
				{
					progress.getLogger().warning("Polygons mixed with non-polygons");
				}
				lastPolygon = polygon;

				Attributes attributes = new Attributes(record);

				if (shape instanceof MultiPolygon)
				{
					MultiPolygon mp = (MultiPolygon) shape;
					shapeId =
							addMultiPolygon(shapeId, mp, attributes, tiles, level, lzts, min, size,
									progress);
				}
				else if (shape instanceof Polygon)
				{
					Polygon p = (Polygon) shape;
					shapeId =
							addPolygon(shapeId, p, attributes, tiles, level, lzts, min, size,
									progress);
				}
				else if (shape instanceof LinearRing)
				{
					LinearRing lr = (LinearRing) shape;
					shapeId =
							addLinearRing(shapeId, lr, attributes, tiles, level, lzts, min, size,
									true, progress);
				}
				else if (shape instanceof MultiLineString)
				{
					MultiLineString mls = (MultiLineString) shape;
					shapeId =
							addMultiLineString(shapeId, mls, attributes, tiles, level, lzts, min,
									size, progress);
				}
				else if (shape instanceof LineString)
				{
					LineString ls = (LineString) shape;
					shapeId =
							addLineString(shapeId, ls, attributes, tiles, level, lzts, min, size,
									progress);
				}
				else
				{
					progress.getLogger().severe("Unsupported shape type: " + shape);
				}
			}

			progress.getLogger().info("Saving tiles");

			int amount = 0;
			for (ShapefileTile tile : tiles)
			{
				if (progress.isCancelled())
					return;

				amount++;
				progress.progress(amount / (double) tiles.length);

				if (anyPolygons)
					tile.completePolygons();

				File rowDir = new File(output, String.valueOf(tile.level));
				rowDir = new File(rowDir, Util.paddedInt(tile.row, 4));
				if (!rowDir.exists())
					rowDir.mkdirs();

				File dst =
						new File(rowDir, Util.paddedInt(tile.row, 4) + "_"
								+ Util.paddedInt(tile.col, 4) + ".zip");

				saveShapefileZip(tile, dst, anyPolygons, progress);
			}

			progress.done();
		}
		catch (Exception e)
		{
			progress.getLogger().log(Level.SEVERE, "Error tiling shapefile", e);
		}
	}

	protected static void saveShapefileZip(ShapefileTile tile, File file, boolean polygon,
			ProgressReporter progress) throws IOException
	{
		if (file.exists())
		{
			progress.getLogger().warning(file + " already exists");
			return;
		}

		int indexOfDot = file.getName().lastIndexOf('.');
		if (indexOfDot < 0)
			throw new IllegalArgumentException("Filename does not have an extension");

		final String filenameNoExt = file.getName().substring(0, indexOfDot);
		File shapefile = new File(file.getParentFile(), filenameNoExt + ".shp");

		List<Record> records = tile.createRecords(polygon);
		if (!records.isEmpty())
		{
			boolean deleteZippedFiles = false;
			if (shapefile.exists())
			{
				progress.getLogger().warning(shapefile + " already exists");
			}
			else
			{
				ShapeFileWriter writer = new ShapeFileWriter(shapefile.getAbsolutePath());
				for (Record rec : records)
				{
					if (rec != null)
						writer.write(rec);
				}
				writer.close();
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
	}

	protected static int addMultiPolygon(int shapeId, MultiPolygon polygon, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		for (Polygon p : polygon.getPolygons())
		{
			shapeId = addPolygon(shapeId, p, attributes, tiles, level, lzts, min, size, progress);
		}
		return shapeId;
	}

	protected static int addPolygon(int shapeId, Polygon polygon, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		LinearRing ring = polygon.getPosativeRing();
		shapeId =
				addLinearRing(shapeId, ring, attributes, tiles, level, lzts, min, size, true,
						progress);

		for (LinearRing hole : polygon.getHoles())
		{
			ShapefileTile containing =
					allPointsWithin(hole.getXCoordinates(), hole.getYCoordinates(), tiles, level,
							lzts, min, size);
			if (containing != null)
			{
				addHole(hole.getXCoordinates(), hole.getYCoordinates(), containing, attributes);
			}
			else
			{
				shapeId =
						addLinearRing(shapeId, hole, attributes, tiles, level, lzts, min, size,
								false, progress);
			}
		}
		return shapeId;
	}

	protected static int addLinearRing(int shapeId, LinearRing ring, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size,
			boolean fillInside, ProgressReporter progress)
	{
		return addPoints(shapeId, ring.getXCoordinates(), ring.getYCoordinates(), attributes, true,
				fillInside, tiles, level, lzts, min, size, progress);
	}

	protected static int addMultiLineString(int shapeId, MultiLineString multiLineString,
			Attributes attributes, ShapefileTile[] tiles, int level, double lzts,
			java.awt.Point min, Dimension size, ProgressReporter progress)
	{
		for (LineString lineString : multiLineString.getLines())
		{
			shapeId =
					addLineString(shapeId, lineString, attributes, tiles, level, lzts, min, size,
							progress);
		}
		return shapeId;
	}

	protected static int addLineString(int shapeId, LineString lineString, Attributes attributes,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size,
			ProgressReporter progress)
	{
		return addPoints(shapeId, lineString.getXCoordinates(), lineString.getYCoordinates(),
				attributes, false, false, tiles, level, lzts, min, size, progress);
	}

	protected static int addPoints(int shapeId, double[] xpoints, double[] ypoints,
			Attributes attributes, boolean polygon, boolean fillInside, ShapefileTile[] tiles,
			int level, double lzts, java.awt.Point min, Dimension size, ProgressReporter progress)
	{
		if (xpoints.length != ypoints.length)
			throw new IllegalArgumentException("arrays don't have equal length");

		Point lastPoint = null;
		ShapefileTile lastTile = null;

		List<ShapefileTile> tilesAffected = new ArrayList<ShapefileTile>();

		for (int i = 0; i < xpoints.length; i++)
		{
			Point point = new Point(xpoints[i], ypoints[i]);

			int x = Util.getTileX(point.x, level, lzts);
			int y = Util.getTileY(point.y, level, lzts);

			//limit tile x/y on the edges
			//(eg lon=180 will resolve to x=11 at level 0 lzts 36, but should be x=10)
			x = Util.limitRange(x, min.x, min.x + size.width - 1);
			y = Util.limitRange(y, min.y, min.y + size.height - 1);

			int x0 = x - min.x;
			int y0 = y - min.y;
			int tileIndex = y0 * size.width + x0;
			ShapefileTile tile = tiles[tileIndex];

			if (!tile.sector.containsPoint(point.y, point.x))
			{
				progress.getLogger().warning(point + " outside bounds");
			}

			if (tile != lastTile)
			{
				if (lastTile != null)
				{
					lastTile.exitShape(shapeId, lastPoint, point);

					int diffRow = Math.abs(lastTile.row - tile.row);
					int diffCol = Math.abs(lastTile.col - tile.col);
					if (diffRow + diffCol > 1)
					{
						//If tile is not next (vertically/horizontally, not diagonally) to lastTile,
						//it means that the points have moved diagonally from one tile, across other
						//tile(s) (with no points inside), into another tile. We must add two edge
						//points to the in-between tile(s) (an entry and an exit).

						//get the fractional position of the points within their corresponding tile's
						//sector, in the range -0.5 to 0.5
						float x1 =
								(float) ((lastPoint.x - lastTile.sector.getMinLongitude())
										/ lastTile.sector.getDeltaLongitude() - 0.5);
						float y1 =
								(float) ((lastPoint.y - lastTile.sector.getMinLatitude())
										/ lastTile.sector.getDeltaLatitude() - 0.5);
						float x2 =
								(float) ((point.x - tile.sector.getMinLongitude())
										/ tile.sector.getDeltaLongitude() - 0.5);
						float y2 =
								(float) ((point.y - tile.sector.getMinLatitude())
										/ tile.sector.getDeltaLatitude() - 0.5);

						//create a list of possible tiles that may be affected by the line between the two tiles
						List<java.awt.Point> line =
								linePoints(lastTile.col + x1, lastTile.row + y1, tile.col + x2,
										tile.row + y2);
						//loop through each tile, calling crossShape() (ignoring the first/last tiles)
						for (int j = 1; j < line.size() - 1; j++)
						{
							java.awt.Point p = line.get(j);
							//check that p lies within tile array's bounds
							if (p.x < min.x || p.x > min.x + size.width || p.y < min.y
									|| p.y > min.y + size.height)
							{
								progress.getLogger().warning(
										"Crossed tile " + p + " outside bounds");
								continue;
							}

							int ptileIndex = (p.y - min.y) * size.width + (p.x - min.x);
							ShapefileTile crossTile = tiles[ptileIndex];
							boolean crossed =
									crossTile.crossShape(shapeId, lastPoint, point, attributes);
							if (crossed && polygon)
								tilesAffected.add(crossTile);
						}
					}
				}

				if (polygon)
					tilesAffected.add(tile);

				if (lastPoint != null)
				{
					tile.enterShape(shapeId, lastPoint, point, attributes);
				}
				else
				{
					tile.continueShape(shapeId, point, attributes);
				}
			}
			else
			{
				tile.continueShape(shapeId, point, attributes);
			}

			lastTile = tile;
			lastPoint = point;
		}

		if (polygon)
		{
			for (ShapefileTile tile : tilesAffected)
				tile.joinOrphanPolygons(shapeId);

			if (fillInside)
			{
				markFilledTilesInside(tiles, tilesAffected, min, size, attributes);
			}
		}

		return shapeId + 1;
	}

	protected static ShapefileTile allPointsWithin(double[] xpoints, double[] ypoints,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size)
	{
		if (xpoints == null || xpoints.length == 0)
			return null;

		if (xpoints.length != ypoints.length)
			throw new IllegalArgumentException("arrays don't have equal length");

		Integer X = null, Y = null;
		for (int i = 0; i < xpoints.length; i++)
		{
			int x = Util.getTileX(xpoints[i], level, lzts);
			int y = Util.getTileY(ypoints[i], level, lzts);

			x = Util.limitRange(x, min.x, min.x + size.width - 1);
			y = Util.limitRange(y, min.y, min.y + size.height - 1);

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

	protected static void addHole(double[] xpoints, double[] ypoints, ShapefileTile tile,
			Attributes attributes)
	{
		if (xpoints.length != ypoints.length)
			throw new IllegalArgumentException("arrays don't have equal length");

		List<Point> points = new ArrayList<Point>(xpoints.length);
		for (int i = 0; i < xpoints.length; i++)
		{
			Point point = new Point(xpoints[i], ypoints[i]);
			points.add(point);
		}

		tile.addHole(points, attributes);
	}

	protected static void markFilledTilesInside(ShapefileTile[] tiles,
			List<ShapefileTile> tilesAffected, java.awt.Point min, Dimension size,
			Attributes attributes)
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

	protected static List<java.awt.Point> linePoints(float x1, float y1, float x2, float y2)
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
}
