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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

import javax.imageio.ImageIO;

import au.gov.ga.worldwind.tiler.shapefile.ShapefileTile.TilePoints;
import au.gov.ga.worldwind.tiler.util.MultiMap;
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

		//TODO need to work out a way of filling in all tiles that are surrounded by a polygon
		//but don't contain any polygon edges. This includes tiles that only contain negative
		//holes but no positive edges.

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

	public static void saveShapefileZip(ShapefileTile tile, File file, boolean polygon) throws IOException
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

	public static int addMultiPolygon(int shapeId, MultiPolygon polygon, ShapefileTile[] tiles, int level,
			double lzts, java.awt.Point min, Dimension size)
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

	public static int addLineString(int shapeId, LineString lineString, ShapefileTile[] tiles, int level,
			double lzts, java.awt.Point min, Dimension size)
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
			//(eg lon=180 will go to x=11 at level 0 lzts 36, but should be x=10)
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
						//if tile is not next (vertically/horizontally, not diagonally) to lastTile,
						//it means that the points have moved diagonally from one tile, across another
						//tile (with no points inside), into another tile. We must add two edge points
						//to the in-between tile, an entry and an exit.
						//(Also, don't forget to add it to tilesAffected!)

						float x1 =
								(float) (lastTile.col
										+ (lastPoint.x - lastTile.sector.getMinLongitude())
										/ lastTile.sector.getDeltaLongitude() - 0.5);
						float y1 =
								(float) (lastTile.row
										+ (lastPoint.y - lastTile.sector.getMinLatitude())
										/ lastTile.sector.getDeltaLatitude() - 0.5);
						float x2 =
								(float) (tile.col + (point.x - tile.sector.getMinLongitude())
										/ tile.sector.getDeltaLongitude() - 0.5);
						float y2 =
								(float) (tile.row + (point.y - tile.sector.getMinLatitude())
										/ tile.sector.getDeltaLatitude() - 0.5);

						List<java.awt.Point> line = Util.linePoints(x1, y1, x2, y2);
						for (int j = 1; j < line.size() - 1; j++)
						{
							java.awt.Point p = line.get(j);
							int px0 = p.x - min.x;
							int py0 = p.y - min.y;
							int ptileIndex = py0 * size.width + px0;
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
				if (tilesAffected.get(0) != tilesAffected.get(tilesAffected.size() - 1))
				{
					throw new IllegalStateException(
							"First tile doesn't equal last tile; cannot fill inside polygon");
				}

				//remove last tile (it is the same as the first)
				tilesAffected.remove(tilesAffected.size() - 1);
				int count = tilesAffected.size();


				//we may have just emptied the list; check
				if (count > 0)
				{
					//find the bounds of the tiles affected
					int minx, miny, maxx, maxy;
					minx = miny = Integer.MAX_VALUE;
					maxx = maxy = Integer.MIN_VALUE;
					for (ShapefileTile tile : tilesAffected)
					{
						minx = Math.min(minx, tile.col);
						maxx = Math.max(maxx, tile.col);
						miny = Math.min(miny, tile.row);
						maxy = Math.max(maxy, tile.row);
					}



					BufferedImage image = null;
					Graphics2D g = null;
					File output = new File("C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/test.bmp");
					int sz = 100;
					if (!output.exists())
					{
						image =
								new BufferedImage((maxx - minx + 1) * sz + 1, (maxy - miny + 1)
										* sz + 1, BufferedImage.TYPE_INT_RGB);
						g = image.createGraphics();
						g.setColor(Color.white);
						g.fillRect(0, 0, image.getWidth(), image.getHeight());
						ShapefileTile lTile = null;
						for (ShapefileTile tile : tilesAffected)
						{
							g.setColor(Color.black);
							g.drawRect((tile.col - minx) * sz + 2, (tile.row - miny) * sz + 2,
									sz - 4, sz - 4);

							for (TilePoints points : tile.points)
							{
								int lastx = -1, lasty = -1;
								for (Point p : points.points)
								{
									int x =
											(int) (((p.x - tile.sector.getMinLongitude()) / tile.sector
													.getDeltaLongitude()) * sz)
													+ (tile.col - minx) * sz;
									int y =
											(int) (((p.y - tile.sector.getMinLatitude()) / tile.sector
													.getDeltaLatitude()) * sz)
													+ (tile.row - miny) * sz;
									if (lastx > 0)
									{
										g.drawLine(lastx, lasty, x, y);
									}
									lastx = x;
									lasty = y;
								}
							}

							if (lTile != null)
							{
								g.setColor(Color.green);
								int minrow = Math.min(tile.row, lTile.row);
								int maxrow = Math.max(tile.row, lTile.row);
								int mincol = Math.min(tile.col, lTile.col);
								int maxcol = Math.max(tile.col, lTile.col);
								if (minrow != maxrow)
								{
									g.fillRect((mincol - minx) * sz + sz / 2 - sz / 20, (minrow
											- miny + 1)
											* sz - sz / 20, sz / 10, sz / 10);
								}
								if (mincol != maxcol)
								{
									g.fillRect((mincol - minx + 1) * sz - sz / 20, (minrow - miny)
											* sz + sz / 2 - sz / 20, sz / 10, sz / 10);
								}
							}

							lTile = tile;
						}
					}

					//create a map of tiles affected for efficient querying
					MultiMap<Integer, Integer> intersectionIndices =
							new MultiMap<Integer, Integer>();
					for (int i = 0; i < tilesAffected.size(); i++)
					{
						ShapefileTile tile = tilesAffected.get(i);
						int index = (tile.row - min.y) * size.width + (tile.col - min.x);
						intersectionIndices.put(index, i);
					}

					List<Map<Integer, Integer>> crossingsMaps =
							new ArrayList<Map<Integer, Integer>>(maxy - miny + 1);
					for (int y = miny; y <= maxy; y++)
					{
						int y0 = y - min.y;
						Map<Integer, Integer> crossingsMap = new HashMap<Integer, Integer>();
						crossingsMaps.add(crossingsMap);

						for (int x = minx; x <= maxx; x++)
						{
							int x0 = x - min.x;
							int index = y0 * size.width + x0;
							if (intersectionIndices.containsKey(index))
							{
								int crossings = 0;
								if (crossingsMap.containsKey(index))
									crossings = crossingsMap.get(index);

								List<Integer> indices = intersectionIndices.get(index);
								for (int i : indices)
								{
									int prevI = (i - 1 + count) % count;
									int nextI = (i + 1) % count;
									ShapefileTile curr = tilesAffected.get(i);
									ShapefileTile prev = tilesAffected.get(prevI);
									ShapefileTile next = tilesAffected.get(nextI);
									int prevRowDelta = curr.row - prev.row;
									int nextRowDelta = curr.row - next.row;
									int prevPos = prevRowDelta < 0 ? -1 : prevRowDelta == 0 ? 0 : 1;
									int nextPos = nextRowDelta < 0 ? -1 : nextRowDelta == 0 ? 0 : 1;

									//both above, below, or on line
									if (prevPos == nextPos)
									{
										//do nothing
									}
									//one above, one below
									else if (Math.abs(prevPos) == 1 && Math.abs(nextPos) == 1)
									{
										crossings++;
									}
									//one above, one on line
									else if (prevPos == 1 || nextPos == 1)
									{
										crossings++;
									}
								}

								crossingsMap.put(index, crossings);
							}
						}
					}

					//fill in the polygons
					for (int y = miny; y <= maxy; y++)
					{
						int crossings = 0;
						int y0 = y - min.y;
						Map<Integer, Integer> crossingsMap = crossingsMaps.get(y - miny);

						for (int x = minx; x <= maxx; x++)
						{
							int x0 = x - min.x;
							int index = y0 * size.width + x0;
							if (intersectionIndices.containsKey(index))
							{
								//tile was affected by polygon, so don't need to fill

								//increment crossings int
								if (crossingsMap.containsKey(index))
									crossings += crossingsMap.get(index);
							}
							else
							{
								if (crossings % 2 == 1)
								{
									//crossings is odd, so fill in tile
									ShapefileTile tile = tiles[index];
									tile.fillSector();
									//System.out.println(tile.row + "," + tile.col + " @ " + tile.level);

									if (g != null)
									{
										g.setColor(Color.black);
										g.fillRect((tile.col - minx) * sz + sz / 4,
												(tile.row - miny) * sz + sz / 4, sz / 2, sz / 2);
									}
								}
							}

							if (g != null)
							{
								/*HSLColor c = new HSLColor(crossings * 60, 100, 50);
								g.setColor(c.getRGB());*/
								g.drawRect((x - minx) * sz + sz / 4, (y - miny) * sz + sz / 4,
										sz / 2, sz / 2);
							}
						}
					}

					if (g != null)
					{
						g.dispose();
						try
						{
							ImageIO.write(image, "bmp", output);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		return shapeId + 1;
	}
}
