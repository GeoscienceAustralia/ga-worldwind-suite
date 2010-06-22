package au.gov.ga.worldwind.tiler.shapefile;

import gistoolkit.features.Point;

import java.util.ArrayList;
import java.util.List;

public class TileRecord
{
	public final int shapeId;
	public final Attributes attributes;
	public final List<Point> points;
	public final List<TileRecord> holes = new ArrayList<TileRecord>();

	public boolean exited = false;
	public boolean entered = false;

	public TileRecord(int shapeId, boolean entered, Attributes attributes)
	{
		this(shapeId, entered, attributes, new ArrayList<Point>());
	}

	public TileRecord(int shapeId, boolean entered, Attributes attributes, List<Point> points)
	{
		this.shapeId = shapeId;
		this.entered = entered;
		this.attributes = attributes;
		this.points = points;
	}
}
