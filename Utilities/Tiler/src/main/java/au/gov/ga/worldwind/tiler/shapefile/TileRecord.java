package au.gov.ga.worldwind.tiler.shapefile;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class TileRecord
{
	public final int shapeId;
	public final Attributes attributes;
	public final List<Coordinate> coordinates;
	public final List<TileRecord> holes = new ArrayList<TileRecord>();

	public boolean exited = false;
	public boolean entered = false;

	public TileRecord(int shapeId, boolean entered, Attributes attributes)
	{
		this(shapeId, entered, attributes, new ArrayList<Coordinate>());
	}

	public TileRecord(int shapeId, boolean entered, Attributes attributes,
			List<Coordinate> coordinates)
	{
		this.shapeId = shapeId;
		this.entered = entered;
		this.attributes = attributes;
		this.coordinates = coordinates;
	}
}
