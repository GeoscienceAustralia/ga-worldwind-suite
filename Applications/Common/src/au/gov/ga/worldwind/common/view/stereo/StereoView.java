package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;

/**
 * Interface which stereoscopic capable View subclasses must implement.
 * 
 * @author Michael de Hoog
 */
public interface StereoView extends View
{
	/**
	 * Represents an eye.
	 */
	public enum Eye
	{
		LEFT,
		RIGHT
	}

	/**
	 * Setup this view, ready for applying.
	 * 
	 * @param stereo
	 *            Enable stereo?
	 * @param eye
	 *            Eye to draw from if stereo enabled
	 */
	void setup(boolean stereo, Eye eye);

	/**
	 * @return Eye this view is drawing
	 */
	Eye getEye();

	/**
	 * @return Is this view currently drawing stereo?
	 */
	boolean isStereo();

	/**
	 * @return Parameters used to draw this view.
	 */
	StereoViewParameters getParameters();

	/**
	 * Set the parameters used to draw this view.
	 * 
	 * @param parameters
	 */
	void setParameters(StereoViewParameters parameters);
	
	/**
	 * @return Focal length used in the last call to doApply()
	 */
	double getCurrentFocalLength();
	
	/**
	 * @return Eye separation used in the last call to doApply()
	 */
	double getCurrentEyeSeparation();

	/**
	 * Calculate a projection matrix for the current state of this view.
	 * 
	 * @param nearDistance
	 *            Near frustum value
	 * @param farDistance
	 *            Far frustum value
	 * @return Projection matrix
	 */
	Matrix calculateProjectionMatrix(double nearDistance, double farDistance);
}
