package au.gov.ga.worldwind.tiler.shapefile;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Container class which stores a single record for the {@link ShapefileTile}.
 * This is used to store geometry when building the tiles, and are converted to
 * shapefile features at the end of the process.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TileRecord
{
	/**
	 * Shapefile id for the shapefile feature which this record contains geomtry
	 * from.
	 */
	public final int shapeId;
	/**
	 * Shapefile feature attributes associated with this record.
	 */
	public final Attributes attributes;
	/**
	 * List of coordinates associated with this record (contains the actual
	 * geometry).
	 */
	public final List<Coordinate> coordinates;
	/**
	 * Any sub-holes within this record.
	 */
	public final List<TileRecord> holes = new ArrayList<TileRecord>();

	/**
	 * Has this record exited the {@link ShapefileTile} it is associated with?
	 */
	public boolean exited = false;
	/**
	 * Has this record entered the {@link ShapefileTile} it is associated with?
	 */
	public boolean entered = false;

	public TileRecord(int shapeId, boolean entered, Attributes attributes)
	{
		this(shapeId, entered, attributes, new ArrayList<Coordinate>());
	}

	public TileRecord(int shapeId, boolean entered, Attributes attributes, List<Coordinate> coordinates)
	{
		this.shapeId = shapeId;
		this.entered = entered;
		this.attributes = attributes;
		this.coordinates = coordinates;
	}
}
