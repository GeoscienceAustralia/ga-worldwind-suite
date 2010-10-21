package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

import java.util.List;

/**
 * A {@link Shape} is a collection of {@link Position}s that has an ID unique for a given {@link GeometryLayer}.
 */
public interface Shape
{
	/**
	 * @return The ID of this shape. Shape IDs are unique within a single {@link GeometryLayer}.
	 */
	String getId();
	
	/**
	 * @return The ordered list of points associated with this shape
	 */
	List<? extends Position> getPoints();
	
	/**
	 * Add the provided position to the end of this shape's list of points.
	 */
	void addPoint(Position p, AVList attributeValues);
	
	/**
	 * @return The type of shape this is
	 */
	Type getType();
	
	/**
	 * The type of shape this is
	 */
	enum Type
	{
		POINT, LINE, POLYGON
	}
}
