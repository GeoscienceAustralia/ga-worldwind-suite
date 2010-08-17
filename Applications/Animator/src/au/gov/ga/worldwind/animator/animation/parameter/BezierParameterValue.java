/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.math.vector.Vector;

/**
 * An extension of the {@link ParameterValue} interface that includes an additional
 * '<code>in</code>' and '<code>out</code>' value to use in calculating Bezier curves
 * for interpolation between values.
 * <p/>
 * The three values of a {@link BezierParameterValue} are used to create a control line
 * at a vertice of the Bezier line, as below:
 * <pre>
 * in  val  out
 * o----o----o
 * </pre>
 * A {@link BezierParameterValue} can also be <code>locked</code>, which ensures that
 * the three values '<code>in</code>', '<code>value</code>' and '<code>out</code>' remain
 * colinear, with the '<code>in</code>' and '<code>out</code>' values maintaining an equal distance
 * from '<code>value</code>'.
 * <p/>
 * The <code>in</code> and <code>out</code> control points also have a percentage weighting associated
 * with them. This is a time-related dimension that is used during interpolation to control the rate
 * of change of the parameter value. 
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface BezierParameterValue<V extends Vector<V>> extends ParameterValue<V>
{
	/**
	 * Set the '<code>in</code>' value.
	 * 
	 * @param value The value to set for '<code>in</code>'
	 */
	void setInValue(V value);
	
	/**
	 * Return the '<code>in</code>' value
	 * 
	 * @return the '<code>in</code>' value
	 */
	V getInValue();
	
	/**
	 * Set the '<code>in</code>' time percent weighting
	 * 
	 * @param percent The percent to set
	 */
	void setInPercent(double percent);
	
	/**
	 * @return The '<code>in</code>' time percent weighting
	 */
	double getInPercent();
	
	/**
	 * Set the '<code>in</code>' value.
	 * 
	 * @param value The value to set for '<code>out</code>'
	 */
	void setOutValue(V value);
	
	/**
	 * Return the '<code>out</code>' value
	 * 
	 * @return the '<code>out</code>' value
	 */
	V getOutValue();
	
	/**
	 * Set the '<code>out</code>' time percent weighting
	 * 
	 * @param percent The percent to set
	 */
	void setOutPercent(double percent);
	
	/**
	 * @return The '<code>out</code>' time percent weighting
	 */
	double getOutPercent();
	
	/**
	 * @return Whether this bezier value is <code>locked</code>.
	 * <p/>
	 * When <code>locked</code>, the three values '<code>in</code>', '<code>value</code>' and '<code>out</code>' will remain
	 * colinear, with the '<code>in</code>' and '<code>out</code>' values adjusted on setting to maintain an equal distance
	 * from '<code>value</code>'
	 */
	boolean isLocked();
	
	/**
	 * Set whether this bezier value is <code>locked</code>
	 * 
	 * @param locked whether this bezier value is <code>locked</code>
	 * 
	 * @see #isLocked()
	 */
	void setLocked(boolean locked);
}
