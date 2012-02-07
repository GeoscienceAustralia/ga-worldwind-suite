package au.gov.ga.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.common.layers.data.DataLayer;

/**
 * Interface for all Point layers. Point classes can extend the specific class
 * for the type of points to display (such as Markers or Icons), and simply need
 * to implement this interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface PointLayer extends DataLayer
{
	/**
	 * Add a point to this layer. Called by the {@link PointProvider}.
	 * 
	 * @param position
	 *            Point to add
	 * @param attributeValues
	 *            Attribute values for this point
	 */
	void addPoint(Position position, AVList attributeValues);

	/**
	 * Called by the {@link PointProvider} after all points have been loaded.
	 */
	void loadComplete();
}
