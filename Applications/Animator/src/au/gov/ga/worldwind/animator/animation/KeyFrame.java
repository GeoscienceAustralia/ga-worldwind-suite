/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.Restorable;

import java.io.Serializable;
import java.util.Collection;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;

/**
 * Represents a key frame in an animation.
 * <p/>
 * A {@link KeyFrame} contains all {@link ParameterValue}s that have been recorded at that
 * frame, along with the frame of the {@link Animation} it corresponds to.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface KeyFrame extends Serializable, Restorable
{
	
	/**
	 * Return the set of parameter values stored on this key frame.
	 *
	 * @return The set of parameter values stored on this key frame.
	 */
	Collection<ParameterValue> getParameterValues();

	/**
	 * Return the {@link ParameterValue} stored on this key frame for the provided 
	 * {@link Parameter}, if one exists.
	 * 
	 * @param p The parameter whose value is required
	 * 
	 * @return the {@link ParameterValue} stored on this key frame for the provided 
	 * {@link Parameter}, or <code>null</code> if one does not exist
	 */
	ParameterValue getValueForParameter(Parameter p); 
	
	/**
	 * Return whether or not this {@link KeyFrame} has a value recorded for the provided parameter.
	 * 
	 * @param p The parameter to check for
	 * 
	 * @return <code>true</code> if a value is recorded for the provided parameter, <code>false</code> otherwise.
	 */
	boolean hasValueForParameter(Parameter p);
	
	/**
	 * Add the provided parameter value to this key frame
	 * 
	 * @param value The value to add
	 */
	void addParameterValue(ParameterValue value);
	
	/**
	 * Get the animation frame this key frame corresponds to
	 * 
	 * @return The animation frame this Key Frame corresponds to
	 */
	int getFrame();
}
