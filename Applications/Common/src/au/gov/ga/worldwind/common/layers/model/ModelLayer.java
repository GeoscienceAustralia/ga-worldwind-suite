package au.gov.ga.worldwind.common.layers.model;

import au.gov.ga.worldwind.common.layers.Hierarchical;
import au.gov.ga.worldwind.common.layers.Wireframeable;
import au.gov.ga.worldwind.common.layers.data.DataLayer;
import au.gov.ga.worldwind.common.util.FastShape;

/**
 * Layer that renders a model. Uses {@link FastShape}s for storing and rendering
 * geometry.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ModelLayer extends DataLayer, Hierarchical, Wireframeable
{
	/**
	 * Add a shape to this layer
	 * 
	 * @param shape
	 *            Shape to add
	 */
	void addShape(FastShape shape);

	/**
	 * Remove a shape from this layer
	 * 
	 * @param shape
	 *            Shape to remove
	 */
	void removeShape(FastShape shape);
}