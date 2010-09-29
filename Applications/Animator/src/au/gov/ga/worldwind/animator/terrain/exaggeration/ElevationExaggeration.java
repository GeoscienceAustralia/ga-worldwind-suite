package au.gov.ga.worldwind.animator.terrain.exaggeration;

/**
 * An interface for classes that can provide configurable vertical exaggeration information for elevation data.
 * <p/>
 * The elevation boundary is immutable, while the exaggeration can be changed once an instance is created.
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
	 * @return the lower bound for the set of elevations that this exaggeration should be applied to.
	 */
	double getElevationBoundary();
	
}
