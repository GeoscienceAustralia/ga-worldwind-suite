/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.math.interpolation.BezierInterpolator;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.math.vector.Vector;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A factory class for creating instances of {@link Interpolator}s suitable for a
 * given pair of {@link ParameterValue}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class InterpolatorFactory<V extends Vector<V>>
{
	/** The default scaling to apply to generated bezier control points when generating from linear values */
	private static final double DEFAULT_BEZIER_CONTROL_SCALE = 0.4;
	
	/**
	 * Create and return an {@link Interpolator} configured for interpolation between 
	 * the two provided parameter values.
	 * 
	 * @param startValue The start value of the interpolation
	 * @param endValue The end value of the interpolation
	 * 
	 * @return an {@link Interpolator} configured for interpolation between 
	 * the two provided parameter values.
	 */
	public Interpolator<V> getInterpolator(ParameterValue<V> startValue, ParameterValue<V> endValue)
	{
		Validate.notNull(startValue, "A start value is required");
		Validate.notNull(endValue, "An end value is required");
		
		// If either values are bezier values, use a bezier interpolator
		if (startValue.getType() == ParameterValueType.BEZIER || endValue.getType() == ParameterValueType.BEZIER)
		{
			return createBezierInterpolator(startValue, endValue);
		}
		
		// TODO: Handle linear case
		return null;
	}

	/**
	 * Create a {@link BezierInterpolator} using the provided start and end values
	 * 
	 * @param startValue The start value to use
	 * @param endValue The end value to use
	 * 
	 * @return a {@link BezierInterpolator} using the provided start and end values
	 */
	private Interpolator<V> createBezierInterpolator(ParameterValue<V> startValue, ParameterValue<V> endValue)
	{
		// TODO: Implement me!
		return null;
		
	}

	private BezierParameterValue<V> asBezierValue(ParameterValue<V> valueToConvert, boolean isStart, ParameterValue<V> otherValue)
	{
		// If it already is a bezier value, don't need to do anything
		if (valueToConvert.getType() == ParameterValueType.BEZIER)
		{
			return (BezierParameterValue<V>)valueToConvert;
		}
		
		BezierParameterValue<V> result = new BasicBezierParameterValue<V>(valueToConvert.getValue(), valueToConvert.getOwner());
		
		// Otherwise, set the appropriate control point to point at the other value
		V controlPoint = valueToConvert.getValue().subtract(otherValue.getValue());
		controlPoint.mult(DEFAULT_BEZIER_CONTROL_SCALE);
		controlPoint = valueToConvert.getValue().add(controlPoint);
		if (isStart)
		{
			result.setOutValue(controlPoint);
		} 
		else
		{
			result.setInValue(controlPoint);
		}
		
		return result;
	}
	
	
}
