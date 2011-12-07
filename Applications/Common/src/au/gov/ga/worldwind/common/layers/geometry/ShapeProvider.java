package au.gov.ga.worldwind.common.layers.geometry;

import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Represents a shape source for the {@link GeometryLayer}. Handles loading shapes
 * from the data source and adding them to the layer.
 */
public interface ShapeProvider extends Bounded
{
	/**
	 * Request (download if not cached) the shapes from the source data for the provided layer.
	 * 
	 * @param layer Layer for which to retrieve shapes
	 */
	public void requestShapes(GeometryLayer layer);
	
}
