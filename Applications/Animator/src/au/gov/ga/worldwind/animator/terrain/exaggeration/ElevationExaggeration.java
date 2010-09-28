package au.gov.ga.worldwind.animator.terrain.exaggeration;

/**
 * An interface for classes that can provide configurable vertical exaggeration information for elevation data.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ElevationExaggeration
{

	/**
	 * Set the exaggeration this instance applied to elevation data
	 */
	void setExaggeration(double exaggeration);
	
	/**
	 * @return The exaggeration this instance is applying to elevation data
	 */
	double getExaggeration();
	
	/**
	 * Set the elevation threshold.
	 * <p/>
	 * Elevations at and above the threshold will be exaggerated by the configured exaggeration amount.
	 * <p/>
	 * Elevations below the threshold will not be exaggerated
	 */
	void setElevationThreshold(double threshold);
	
	/**
	 * @return The elevation threshold.
	 * @see #setElevationThreshold(double)
	 */
	double getElevationThreshold();
	
}
