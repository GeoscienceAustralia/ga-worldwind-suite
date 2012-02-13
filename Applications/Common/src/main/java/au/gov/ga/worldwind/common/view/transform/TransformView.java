package au.gov.ga.worldwind.common.view.transform;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.view.ViewUtil;

/**
 * View interface extension that provides the ability to customize the model
 * view and projection matrices before they are applied.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface TransformView extends View
{
	/**
	 * Called by doApply function, before the transform matrices are computed.
	 * Override this function in subclasses to perform necessary calculations.
	 */
	void beforeComputeMatrices();

	/**
	 * Calculate a MODELVIEW transform for this view. Default implementation
	 * uses {@link ViewUtil#computeTransformMatrix}.
	 */
	Matrix computeModelView();

	/**
	 * Calculates a PROJECTION transform for this view. Default implementation
	 * uses {@link Matrix#fromPerspective}.
	 */
	Matrix computeProjection();
}
