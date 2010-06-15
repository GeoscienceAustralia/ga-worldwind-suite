package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordMultiPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPoint;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolygon;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolyline;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Color;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.util.HSLColor;

import com.sun.opengl.util.BufferUtil;

public class ShapefileTileData implements Renderable, Cacheable
{
	private Sector sector;
	private List<FastShape> shapes = new ArrayList<FastShape>();
	private List<Renderable> renderables = new ArrayList<Renderable>();
	private long size;
	private boolean sizeDirty = true;
	private boolean mergeLines = true;

	public static synchronized MemoryCache getMemoryCache()
	{
		if (!WorldWind.getMemoryCacheSet().containsCache(ShapefileTileData.class.getName()))
		{
			long size = Configuration.getLongValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L) * 10;
			//long size = 300 * 1024 * 1024;
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName("Shapefile Tile Data");
			WorldWind.getMemoryCacheSet().addCache(ShapefileTileData.class.getName(), cache);
		}
		return WorldWind.getMemoryCacheSet().getCache(ShapefileTileData.class.getName());
	}

	public ShapefileTileData(Sector sector)
	{
		this(sector, null);
	}

	public ShapefileTileData(Sector sector, Shapefile shapefile)
	{
		this.sector = sector;

		if (shapefile != null)
			addShapefile(shapefile);
	}

	public void addShapefile(Shapefile shapefile)
	{
		List<ShapefileRecord> records = shapefile.getRecords();

		/*for (ShapefileRecord record : records)
		{
			if (record instanceof ShapefileRecordPolyline)
			{
				for (int part = 0; part < record.getNumberOfParts(); part++)
				{
					List<Position> positions = new ArrayList<Position>();

					VecBuffer buffer = record.getBuffer(part);
					int size = buffer.getSize();
					for (int i = 0; i < size; i++)
					{
						Position position = buffer.getPosition(i);
						positions.add(position);
					}

					BetterPolygon polygon = new BetterPolygon(positions);
					polygon.setEnableLevelOfDetail(false);
					polygon.setTerrainConforming(true);
					renderables.add(polygon);
				}
			}
		}*/

		boolean anyPolygons = false;
		for (ShapefileRecord record : records)
		{
			if (record instanceof ShapefileRecordPolygon)
			{
				anyPolygons = true;
				break;
			}
		}

		if (anyPolygons)
		{
			List<FastShape> shapes = PolygonTessellator.tessellateShapefile(shapefile, sector);
			for (FastShape shape : shapes)
			{
				shape.setColor(randomColor());
				//shape.addPointsInsideSector(sector, 10);
				shape.setFollowTerrain(true);
				this.shapes.add(shape);
			}
		}
		else
		{
			List<Position> mergedLines = new ArrayList<Position>();
			List<Integer> mergedIndices = new ArrayList<Integer>();
			List<List<Position>> lines = new ArrayList<List<Position>>();
			List<Position> points = new ArrayList<Position>();

			for (ShapefileRecord record : records)
			{
				if (record instanceof ShapefileRecordPolyline)
				{
					for (int part = 0; part < record.getNumberOfParts(); part++)
					{
						List<Position> line = null;
						if (!mergeLines)
						{
							line = new ArrayList<Position>();
							lines.add(line);
						}

						VecBuffer buffer = record.getBuffer(part);
						int size = buffer.getSize();
						for (int i = 0; i < size; i++)
						{
							Position position = buffer.getPosition(i);
							if (mergeLines)
							{
								if (i > 0)
								{
									int index = mergedLines.size();
									mergedIndices.add(index - 1);
									mergedIndices.add(index);
								}
								mergedLines.add(position);
							}
							else
							{
								line.add(position);
							}
						}
					}
				}
				else if (record instanceof ShapefileRecordPoint
						|| record instanceof ShapefileRecordMultiPoint)
				{
					for (int part = 0; part < record.getNumberOfParts(); part++)
					{
						VecBuffer buffer = record.getBuffer(part);
						int size = buffer.getSize();
						for (int i = 0; i < size; i++)
						{
							points.add(buffer.getPosition(i));
						}
					}
				}
			}

			if (!mergedLines.isEmpty())
			{
				IntBuffer indices = BufferUtil.newIntBuffer(mergedIndices.size());
				for (Integer i : mergedIndices)
					indices.put(i);

				FastShape shape =
						new FastShape(mergedLines, new IntBuffer[] { indices }, GL.GL_LINES);
				shape.setColor(randomColor());
				shapes.add(shape);
			}
			if (!points.isEmpty())
			{
				FastShape shape = new FastShape(points, GL.GL_POINTS);
				shape.setColor(randomColor());
				shapes.add(shape);
			}
			for (List<Position> line : lines)
			{
				if (line.size() > 1)
				{
					FastShape shape = new FastShape(line, GL.GL_LINE_STRIP);
					shape.setColor(randomColor());
					shapes.add(shape);
				}
			}
		}

		sizeDirty = true;
	}

	private Color randomColor()
	{
		HSLColor color = new HSLColor(new Random().nextFloat() * 360f, 100f, 50f);
		return color.getRGB();
	}

	@Override
	public void render(DrawContext dc)
	{
		GL gl = dc.getGL();

		//gl.glCullFace(GL.GL_NONE);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		//gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);

		for (Renderable renderable : shapes)
			renderable.render(dc);
		for (Renderable renderable : renderables)
			renderable.render(dc);
	}

	@Override
	public long getSizeInBytes()
	{
		if (sizeDirty)
		{
			size = 20l; //at least
			for (FastShape shape : shapes)
				size += shape.getSizeInBytes();
			sizeDirty = false;
		}
		return size;
	}
}
