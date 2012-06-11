package au.gov.ga.worldwind.viewer.layers.shapefile;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
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

import au.gov.ga.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.viewer.layers.shapefile.indextessellator.PolygonTessellator;

public class ShapefileTileData implements Renderable, Cacheable
{
	private Sector sector;
	private List<FastShape> shapes = new ArrayList<FastShape>();
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
		String shapeType = shapefile.getShapeType();

		if (Shapefile.isPolygonType(shapeType))
		{
			addPolygons(shapefile);
		}
		else if (Shapefile.isPolygonType(shapeType))
		{
			addLines(shapefile);
		}
		else
		{
			addPoints(shapefile);
		}

		sizeDirty = true;
	}

	protected void addPolygons(Shapefile shapefile)
	{
		List<FastShape> shapes = PolygonTessellator.tessellateShapefile(shapefile, sector, 40);
		for (FastShape shape : shapes)
		{
			shape.setColor(randomColor());
			//shape.addPointsInsideSector(sector, 10);
			shape.setFollowTerrain(true);
			this.shapes.add(shape);
		}
	}

	protected void addLines(Shapefile shapefile)
	{
		List<Position> mergedLines = new ArrayList<Position>();
		List<Integer> mergedIndices = new ArrayList<Integer>();
		List<List<Position>> lines = new ArrayList<List<Position>>();

		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();

			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				List<Position> line = null;
				if (!mergeLines)
				{
					line = new ArrayList<Position>();
					lines.add(line);
				}

				VecBuffer buffer = record.getPointBuffer(part);
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

		if (!mergedLines.isEmpty())
		{
			IntBuffer indices = IntBuffer.allocate(mergedIndices.size());
			for (Integer i : mergedIndices)
				indices.put(i);

			FastShape shape = new FastShape(mergedLines, indices.array(), GL.GL_LINES);
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

	protected void addPoints(Shapefile shapefile)
	{
		List<Position> points = new ArrayList<Position>();

		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();

			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				VecBuffer buffer = record.getPointBuffer(part);
				int size = buffer.getSize();
				for (int i = 0; i < size; i++)
				{
					points.add(buffer.getPosition(i));
				}
			}
		}

		if (!points.isEmpty())
		{
			FastShape shape = new FastShape(points, GL.GL_POINTS);
			shape.setColor(randomColor());
			shapes.add(shape);
		}
	}

	protected Color randomColor()
	{
		HSLColor color = new HSLColor(new Random().nextFloat() * 360f, 100f, 50f);
		return color.getRGB();
	}

	@Override
	public void render(DrawContext dc)
	{
		for (Renderable renderable : shapes)
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
