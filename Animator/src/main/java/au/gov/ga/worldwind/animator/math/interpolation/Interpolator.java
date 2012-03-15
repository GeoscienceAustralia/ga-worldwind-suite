/**
 * 
 */
package au.gov.ga.worldwind.animator.math.interpolation;

import au.gov.ga.worldwind.animator.math.vector.Vector;

/**
 * An interface for interpolators that are able to provide interpolated values at a given 
 * percentage between a start and end value.
 * <p/>
 * Subclasses might include linear or bezier interpolators and may 
 * require specific initialisation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Interpolator<V extends Vector<?>>
{
	
	/**
	 * Compute the interpolated value at the provided percentage 
	 * along the interpolation
	 * 
	 * @param percent The percentage along the interpolation the value is required for. 
	 * 				  In range <code>[0,1]</code>
	 * 
	 * @return The computed interpolated value at the provided percentage
	 */
	V computeValue(double percent);
	
}
