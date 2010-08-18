/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.Restorable;

import java.io.Serializable;

/**
 * A {@link ParameterValue} represents a snapshot of the value of a {@link Parameter}
 * at a given frame.
 * <p/>
 * {@link ParameterValue}s can be associated with key frames to record the state of a {@link Parameter}
 * at a given key frame, or can be calculated by interpolating between two key frames.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ParameterValue extends Restorable, Serializable
{
	/**
	 * @return The value of this parameter value
	 */
	double getValue();
	
	/**
	 * Set the value of this parameter value
	 * 
	 * @param value The value to set
	 */
	void setValue(double value);
	
	/**
	 * @return The {@link Parameter} that 'owns' this value
	 */
	Parameter getOwner();
	
	/**
	 * @return The type of this parameter value
	 */
	ParameterValueType getType();
	
	/**
	 * @return The frame this parameter value is associated with
	 */
	int getFrame();
}
