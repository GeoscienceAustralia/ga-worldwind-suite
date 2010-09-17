/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import java.io.Serializable;

import au.gov.ga.worldwind.animator.animation.AnimationObject;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;

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
public interface ParameterValue extends AnimationObject, Serializable, XmlSerializable<ParameterValue>, Changeable
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
	
	/**
	 * Set the frame this parameter value is associated with
	 * 
	 * @param frame The frame this parameter value is associated with
	 */
	void setFrame(int frame);

	/**
	 * Apply a smoothing algorithm to this point to provide a smooth transition into and out-of this value
	 * during animation.
	 * <p/>
	 * The algorithm used to achieve smoothing is up to the implementing class. 
	 */
	void smooth();
}
