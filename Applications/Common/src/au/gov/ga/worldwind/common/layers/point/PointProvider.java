package au.gov.ga.worldwind.common.layers.point;

import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Represents a point source for the {@link PointLayer}. Handles loading points
 * from the data source, and adding them to the layer.
 * 
 * @author Michael de Hoog
 */
public interface PointProvider extends Bounded
{
	/**
	 * Request (download if not cached) the points from the source data for the
	 * provided layer.
	 * 
	 * @param layer
	 *            Layer for which to retrieve points
	 */
	public void requestPoints(PointLayer layer);
}
