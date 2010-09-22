/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.math.interpolation.BezierInterpolator;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.math.interpolation.LinearInterpolator;
import au.gov.ga.worldwind.animator.math.vector.TimeVector;
import au.gov.ga.worldwind.animator.math.vector.Vector2;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A factory class for creating instances of {@link Interpolator}s suitable for a
 * given pair of {@link ParameterValue}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class InterpolatorFactory
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
	 * the two provided parameter values. The interpolator will be configured to interpolate
	 * between 2 dimensional vectors <code>[frame, value]</code>
	 */
	public static Interpolator<Vector2> getInterpolator(ParameterValue startValue, ParameterValue endValue)
	{
		Validate.notNull(startValue, "A start value is required");
		Validate.notNull(endValue, "An end value is required");
		
		// If either values are bezier values, use a bezier interpolator
		if (startValue.getType() == ParameterValueType.BEZIER || endValue.getType() == ParameterValueType.BEZIER)
		{
			return createBezierInterpolator(startValue, endValue);
		}
		
		return createLinearInterpolator(startValue, endValue);
	}

	/**
	 * Create a new {@link LinearInterpolator} using the provided start and end values
	 * 
	 * @param startValue The start value to use
	 * @param endValue The end value to use
	 * 
	 * @return a {@link LinearInterpolator} using the provided start and end values.
	 */
	private static Interpolator<Vector2> createLinearInterpolator(ParameterValue startValue, ParameterValue endValue)
	{
		Vector2 start = new TimeVector(startValue.getFrame(), startValue.getValue());
		Vector2 end = new TimeVector(endValue.getFrame(), endValue.getValue());
		
		return new LinearInterpolator<Vector2>(start, end);
	}
	
	/**
	 * Create a {@link BezierInterpolator} using the provided start and end values
	 * 
	 * @param startValue The start value to use
	 * @param endValue The end value to use
	 * 
	 * @return a {@link BezierInterpolator} using the provided start and end values.
	 */
	private static Interpolator<Vector2> createBezierInterpolator(ParameterValue startValue, ParameterValue endValue)
	{
		BezierParameterValue startBezier = asBezierValue(startValue, true, endValue);
		BezierParameterValue endBezier = asBezierValue(endValue, false, startValue);
		
		// Interpolation is on a scaled interval [0,1] in the time dimension
		Vector2 begin = new TimeVector(0, startBezier.getValue());
		Vector2 out = new TimeVector(startBezier.getOutPercent(), startBezier.getOutValue());
		Vector2 in = new TimeVector(1 - endBezier.getInPercent(), endBezier.getInValue());
		Vector2 end = new TimeVector(1, endBezier.getValue());
		
		BezierInterpolator<Vector2> result = new BezierInterpolator<Vector2>(begin, out, in, end);
		return result;
		
	}

	/**
	 * Convert the provided parameter value to a Bezier value.
	 * <p/>
	 * If is already a bezier value, won't change anything. Otherwise, will create a 'linear' bezier value
	 * (i.e. a bezier value whose control points are setup to mimic a linear point).
	 * 
	 * @param valueToConvert The value to convert
	 * @param isStart Whether or not the value to convert is the start or end point on the interval. Used to determine whether to set 'in' or 'out' values.
	 * @param otherValue The other value in the interval
	 * @return
	 */
	private static BezierParameterValue asBezierValue(ParameterValue valueToConvert, boolean isStart, ParameterValue otherValue)
	{
		// If it already is a bezier value, don't need to do anything
		if (valueToConvert.getType() == ParameterValueType.BEZIER)
		{
			return (BezierParameterValue)valueToConvert;
		}
		
		// Otherwise, set the appropriate control point to point at the other value (mimic a linear point)
		BezierParameterValue result = new BasicBezierParameterValue(valueToConvert.getValue(), valueToConvert.getFrame(), valueToConvert.getOwner());
		result.removeChangeListener(valueToConvert.getOwner());
		
		Vector2 thisPoint = new Vector2(valueToConvert.getFrame(), valueToConvert.getValue());
		Vector2 otherPoint = new Vector2(otherValue.getFrame(), otherValue.getValue());
		Vector2 controlPoint = thisPoint.add((otherPoint.subtract(thisPoint)).mult(DEFAULT_BEZIER_CONTROL_SCALE));
		if (isStart)
		{
			result.setOutValue(controlPoint.y);
		} 
		else
		{
			result.setInValue(controlPoint.y);
		}
		return result;
	}
	
	
}
