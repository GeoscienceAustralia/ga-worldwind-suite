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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import au.gov.ga.worldwind.tiler.util.Sector;
import au.gov.ga.worldwind.tiler.util.Util;


public class ShapefileTiler
{
	public static void main(String[] args)
	{
		File level0 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/c/GSHHS_c_L1.shp");
		File level1 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/l/GSHHS_l_L1.shp");
		File level2 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/i/GSHHS_i_L1.shp");
		File level3 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/h/GSHHS_h_L1.shp");
		File level4 = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/f/GSHHS_f_L1.shp");
		File[] levels = new File[] { level0, level1, level2, level3, level4 };
		File output = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/tiled");
		int levelCount = 1;
		int startLevel = 0;
		double lzts = 36;

		try
		{
			for (int i = 0; i < 5; i++)
			{
				File input = levels[i];
				startLevel = i;

				System.out.println("Parsing " + input);
				ShapeFileReader reader = new ShapeFileReader(input.getAbsolutePath());
				Envelope envelope = reader.getHeader().getFileEnvelope();
				Sector sector =
						new Sector(envelope.getMinY(), envelope.getMinX(), envelope.getMaxY(),
								envelope.getMaxX());

				ShapefileTile[][] tiles = new ShapefileTile[levelCount][];
				Dimension[] sizes = new Dimension[levelCount];
				java.awt.Point[] mins = new java.awt.Point[levelCount];

				for (int level = startLevel; level < startLevel + levelCount; level++)
				{
					int l = level - startLevel;
					double tilesizedegrees = Math.pow(0.5, level) * lzts;
					int minX = Util.getTileX(sector.getMinLongitude() + 1e-10, level, lzts);
					int maxX = Util.getTileX(sector.getMaxLongitude() - 1e-10, level, lzts);
					int minY = Util.getTileY(sector.getMinLatitude() + 1e-10, level, lzts);
					int maxY = Util.getTileY(sector.getMaxLatitude() - 1e-10, level, lzts);

					mins[l] = new java.awt.Point(minX, minY);
					sizes[l] = new Dimension(maxX - minX + 1, maxY - minY + 1);
					int size = sizes[l].width * sizes[l].height;
					tiles[l] = new ShapefileTile[size];

					for (int y = minY; y <= maxY; y++)
					{
						int y0 = y - minY;
						for (int x = minX; x <= maxX; x++)
						{
							int x0 = x - minX;
							final double lat1 = (y * tilesizedegrees) - 90;
							final double lon1 = (x * tilesizedegrees) - 180;
							final double lat2 = lat1 + tilesizedegrees;
							final double lon2 = lon1 + tilesizedegrees;
							Sector s = new Sector(lat1, lon1, lat2, lon2);

							ShapefileTile tile = new ShapefileTile(s, level, x, y);
							tiles[l][y0 * sizes[l].width + x0] = tile;
						}
					}
				}

				boolean anyPolygons = false;

				System.out.println("Reading records");
				ShapeFileRecord record;
				int shapeId = 0;
				while ((record = reader.read()) != null)
				{
					for (int level = startLevel; level < startLevel + levelCount; level++)
					{
						int l = level - startLevel;
						ShapefileTile[] ts = tiles[l];
						Dimension size = sizes[l];
						java.awt.Point min = mins[l];

						Shape shape = record.getShape();
						boolean polygon =
								shape instanceof MultiPolygon || shape instanceof Polygon
										|| shape instanceof LinearRing;

						anyPolygons |= polygon;
						if (anyPolygons && !polygon)
							System.out.println("ERROR: polygons mixed with non-polygons");

						if (shape instanceof MultiPolygon)
						{
							MultiPolygon mp = (MultiPolygon) shape;
							shapeId = addMultiPolygon(shapeId, mp, ts, level, lzts, min, size);
						}
						else if (shape instanceof Polygon)
						{
							Polygon p = (Polygon) shape;
							shapeId = addPolygon(shapeId, p, ts, level, lzts, min, size);
						}
						else if (shape instanceof LinearRing)
						{
							LinearRing lr = (LinearRing) shape;
							shapeId = addLinearRing(shapeId, lr, ts, level, lzts, min, size, true);
						}
						else if (shape instanceof MultiLineString)
						{
							MultiLineString mls = (MultiLineString) shape;
							shapeId = addMultiLineString(shapeId, mls, ts, level, lzts, min, size);
						}
						else if (shape instanceof LineString)
						{
							LineString ls = (LineString) shape;
							shapeId = addLineString(shapeId, ls, ts, level, lzts, min, size);
						}
						else
						{
							System.out.println("ERROR: unsupported shape type: " + shape);
						}
					}
				}

				int amount = 0;
				System.out.println("Saving tiles");
				for (int level = startLevel; level < startLevel + levelCount; level++)
				{
					System.out.println("Saving level: " + level);
					int l = level - startLevel;
					ShapefileTile[] ts = tiles[l];

					for (ShapefileTile tile : ts)
					{
						amount++;
						if (amount * 100 / ts.length > (amount - 1) * 100 / ts.length)
							System.out.println((amount * 100 / ts.length) + "%");

						if (anyPolygons)
							tile.completePolygons();

						File rowDir = new File(output, String.valueOf(tile.level));
						rowDir = new File(rowDir, Util.paddedInt(tile.row, 4));
						if (!rowDir.exists())
							rowDir.mkdirs();

						File dst =
								new File(rowDir, Util.paddedInt(tile.row, 4) + "_"
										+ Util.paddedInt(tile.col, 4) + ".zip");

						saveShapefileZip(tile, dst, anyPolygons);
					}
				}

				System.out.println("Done");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void saveShapefileZip(ShapefileTile tile, File file, boolean polygon)
			throws IOException
	{
		if (file.exists())
		{
			System.out.println(file + " already exists");
			return;
		}

		if (!file.getName().toLowerCase().endsWith(".zip"))
			throw new IllegalArgumentException("unknown zip file extension: " + file.getName());

		final String filenameNoExt = file.getName().substring(0, file.getName().length() - 4);
		File shapefile = new File(file.getParentFile(), filenameNoExt + ".shp");

		if (shapefile.exists())
		{
			System.out.println(shapefile + " already exists");
			return;
		}

		List<Record> records = tile.createRecords(polygon);
		if (!records.isEmpty())
		{
			ShapeFileWriter writer = new ShapeFileWriter(shapefile.getAbsolutePath());
			for (Record rec : records)
			{
				if (rec != null)
					writer.write(rec);
			}
			writer.close();

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

			//delete all the files that we just zipped
			for (File f : files)
			{
				f.delete();
			}
		}
	}

	public static int addMultiPolygon(int shapeId, MultiPolygon polygon, ShapefileTile[] tiles,
			int level, double lzts, java.awt.Point min, Dimension size)
	{
		for (Polygon p : polygon.getPolygons())
		{
			shapeId = addPolygon(shapeId, p, tiles, level, lzts, min, size);
		}
		return shapeId;
	}

	public static int addPolygon(int shapeId, Polygon polygon, ShapefileTile[] tiles, int level,
			double lzts, java.awt.Point min, Dimension size)
	{
		//TODO filling in the positive ring will not work if holes exist that cross the borders of a filled tile

		LinearRing ring = polygon.getPosativeRing();
		shapeId = addLinearRing(shapeId, ring, tiles, level, lzts, min, size, true);

		for (LinearRing hole : polygon.getHoles())
		{
			shapeId = addLinearRing(shapeId, hole, tiles, level, lzts, min, size, false);
		}
		return shapeId;
	}

	public static int addLinearRing(int shapeId, LinearRing ring, ShapefileTile[] tiles, int level,
			double lzts, java.awt.Point min, Dimension size, boolean fillInside)
	{
		return addPoints(shapeId, ring.getXCoordinates(), ring.getYCoordinates(), true, fillInside,
				tiles, level, lzts, min, size);
	}

	public static int addMultiLineString(int shapeId, MultiLineString multiLineString,
			ShapefileTile[] tiles, int level, double lzts, java.awt.Point min, Dimension size)
	{
		for (LineString lineString : multiLineString.getLines())
		{
			shapeId = addLineString(shapeId, lineString, tiles, level, lzts, min, size);
		}
		return shapeId;
	}

	public static int addLineString(int shapeId, LineString lineString, ShapefileTile[] tiles,
			int level, double lzts, java.awt.Point min, Dimension size)
	{
		return addPoints(shapeId, lineString.getXCoordinates(), lineString.getYCoordinates(),
				false, false, tiles, level, lzts, min, size);
	}

	public static int addPoints(int shapeId, double[] xpoints, double[] ypoints, boolean polygon,
			boolean fillInside, ShapefileTile[] tiles, int level, double lzts, java.awt.Point min,
			Dimension size)
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
				System.out.println("ERROR: " + point + " outside bounds");
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
								System.out
										.println("WARNING: crossed tile " + p + " outside bounds");
								continue;
							}

							int ptileIndex = (p.y - min.y) * size.width + (p.x - min.x);
							ShapefileTile crossTile = tiles[ptileIndex];
							boolean crossed = crossTile.crossShape(shapeId, lastPoint, point);
							if (crossed && polygon)
								tilesAffected.add(crossTile);
						}
					}
				}

				if (polygon)
					tilesAffected.add(tile);

				if (lastPoint != null)
				{
					tile.enterShape(shapeId, lastPoint, point);
				}
				else
				{
					tile.continueShape(shapeId, point);
				}
			}
			else
			{
				tile.continueShape(shapeId, point);
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
				fillInside(tiles, tilesAffected, min, size);
			}
		}

		return shapeId + 1;
	}

	private static void fillInside(ShapefileTile[] tiles, List<ShapefileTile> tilesAffected,
			java.awt.Point min, Dimension size)
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
						tile.fillSector();
					}
				}
			}
		}
	}

	private static List<java.awt.Point> linePoints(float x1, float y1, float x2, float y2)
	{
		List<java.awt.Point> points = new ArrayList<java.awt.Point>();

		int x1i = Math.round(x1);
		int y1i = Math.round(y1);
		int x2i = Math.round(x2);
		int y2i = Math.round(y2);

		//handle special vertical/horizontal line cases
		if (x1i == x2i || y1i == y2i)
		{
			if (x1i == x2i)
			{
				for (int y = y1i; y <= y2i; y++)
					points.add(new java.awt.Point(x1i, y));
			}
			else
			{
				for (int x = x1i; x <= x2i; x++)
					points.add(new java.awt.Point(x, y1i));
			}
			return points;
		}

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
		if (x1 > x2)
		{
			//swap p1/p2 points
			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

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

		return points;
	}
}
