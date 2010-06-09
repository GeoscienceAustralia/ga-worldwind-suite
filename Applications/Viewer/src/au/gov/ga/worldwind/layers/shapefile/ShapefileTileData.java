package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.WorldWind;
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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.util.HSLColor;

public class ShapefileTileData implements Renderable, Cacheable
{
	private List<Renderable> renderables = new ArrayList<Renderable>();
	private long size = 4l;
	private boolean mergeLines = true;

	public static synchronized MemoryCache getMemoryCache()
	{
		if (!WorldWind.getMemoryCacheSet().containsCache(ShapefileTileData.class.getName()))
		{
			//long size = Configuration.getLongValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
			long size = 300 * 1024 * 1024;
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName("Shapefile Tile Data");
			WorldWind.getMemoryCacheSet().addCache(ShapefileTileData.class.getName(), cache);
		}
		return WorldWind.getMemoryCacheSet().getCache(ShapefileTileData.class.getName());
	}

	public ShapefileTileData(Shapefile shapefile)
	{
		addShapefile(shapefile);
	}

	public void addShapefile(Shapefile shapefile)
	{
		List<ShapefileRecord> records = shapefile.getRecords();
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
			List<FastShape> shapes = PolygonTessellator.tessellateShapefile(shapefile);
			for (FastShape shape : shapes)
			{
				shape.setColor(randomColor());
				renderables.add(shape);
			}
		}
		else
		{
			List<Position> mergedLines = new ArrayList<Position>();
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

						Position last = null;
						VecBuffer buffer = record.getBuffer(part);
						int size = buffer.getSize();
						for (int i = 0; i < size; i++)
						{
							Position position = buffer.getPosition(i);
							if (mergeLines)
							{
								if (last != null)
								{
									mergedLines.add(last);
									mergedLines.add(position);
								}
								last = position;
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
				FastShape shape = new FastShape(mergedLines, GL.GL_LINES);
				shape.setColor(randomColor());
				renderables.add(shape);
			}
			if (!points.isEmpty())
			{
				FastShape shape = new FastShape(points, GL.GL_POINTS);
				shape.setColor(randomColor());
				renderables.add(shape);
			}
			for (List<Position> line : lines)
			{
				if (line.size() > 1)
				{
					FastShape shape = new FastShape(line, GL.GL_LINE_STRIP);
					shape.setColor(randomColor());
					renderables.add(shape);
				}
			}
		}
	}

	private Color randomColor()
	{
		HSLColor color = new HSLColor(new Random().nextFloat() * 360f, 100f, 50f);
		return color.getRGB();
	}

	@Override
	public void render(DrawContext dc)
	{
		for (Renderable renderable : renderables)
			renderable.render(dc);
	}

	@Override
	public long getSizeInBytes()
	{
		return size;
	}
}
