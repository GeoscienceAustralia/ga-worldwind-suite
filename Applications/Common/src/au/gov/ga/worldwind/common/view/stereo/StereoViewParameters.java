package au.gov.ga.worldwind.common.view.stereo;

/**
 * Parameters for {@link StereoView}s.
 * 
 * @author Michael de Hoog
 */
public interface StereoViewParameters
{
	/**
	 * @return Asymmetric frustum focal length for this view.
	 */
	double getFocalLength();

	/**
	 * Set the asymmetric frustum focal length for this view; ignored if dynamic
	 * stereo is enabled.
	 * 
	 * @param focalLength
	 */
	void setFocalLength(double focalLength);

	/**
	 * @return Eye separation for this view.
	 */
	double getEyeSeparation();

	/**
	 * Set the eye separation for this view; ignored if dynamic stereo is
	 * enabled.
	 * 
	 * @param eyeSeparation
	 */
	void setEyeSeparation(double eyeSeparation);

	/**
	 * @return Eye separation multiplier applied to the eye separation in the
	 *         dynamic stereo mode.
	 */
	double getEyeSeparationMultiplier();

	/**
	 * Set the eye separation multiplier applied to the eye separation in the
	 * dynamic stereo mode.
	 * 
	 * @param eyeSeparationMultiplier
	 */
	void setEyeSeparationMultiplier(double eyeSeparationMultiplier);

	/**
	 * @return Is this view calculating stereo parameters (focal length and eye
	 *         separation) dynamically according to zoom and pitch?
	 */
	boolean isDynamicStereo();

	/**
	 * Enable/disable dynamic calculation of stereo parameters (focal length and
	 * eye separation) according to zoom and pitch.
	 * 
	 * @param dynamicStereo
	 */
	void setDynamicStereo(boolean dynamicStereo);
}
