package au.gov.ga.worldwind.common.layers.model;

import au.gov.ga.worldwind.common.layers.data.DataLayer;
import au.gov.ga.worldwind.common.util.FastShape;

/**
 * Layer that renders a model. Uses a {@link FastShape} for storing and
 * rendering geometry.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ModelLayer extends DataLayer
{
	/**
	 * @return The {@link FastShape} containing the model geometry for this
	 *         layer.
	 */
	FastShape getShape();

	/**
	 * Set the model geometry for this layer. Called by the
	 * {@link ModelProvider}.
	 * 
	 * @param shape
	 */
	void setShape(FastShape shape);
}
