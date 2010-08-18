/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;


/**
 * A factory class that maintains the current 'default' value type, 
 * and knows how to convert between value types.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterValueFactory
{
	/** The current 'default' parameter value type */
	private static ParameterValueType defaultValueType = ParameterValueType.BEZIER;
	
	/** Static factory class. No need to instantiate. */
	private ParameterValueFactory(){};
	
	/**
	 * Set the default value type to use when one is not provided
	 */
	public static void setDefaultValueType(ParameterValueType defaultValueType)
	{
		ParameterValueFactory.defaultValueType = defaultValueType;
	}
	
	/**
	 * @return the default value type used when one is not provided
	 */
	public static ParameterValueType getDefaultValueType()
	{
		return defaultValueType;
	}
	
	/**
	 * Create a new parameter value for the given parameter, using the current
	 * default value type.
	 *  
	 * @param parameter The parameter to create the value for
	 * @param value The value of the parameter
	 * @param value The frame the value is associated with
	 * 
	 * @return a new parameter value for the given parameter
	 */
	public static ParameterValue createParameterValue(Parameter parameter, double value, int frame)
	{
		return createParameterValue(defaultValueType, parameter, value, frame);
	}
	
	/**
	 * Create a new parameter value for the given parameter
	 * 
	 * @param type the type of the value to create
	 * @param parameter The parameter to create the value for
	 * @param value The value of the parameter
	 * @param frame The frame the value is associated with
	 * 
	 * @return a new parameter value for the given parameter
	 */
	public static ParameterValue createParameterValue(ParameterValueType type, Parameter parameter, double value, int frame)
	{
		switch (type)
		{
			case LINEAR:
				return new BasicParameterValue(value, frame, parameter);
			
			// Default case is BEZIER
			default:
				return new BasicBezierParameterValue(value, frame, parameter);
		}
	}
	
	/**
	 * Convert the provided {@link ParameterValue} to a {@link BezierParameterValue}. 
	 * <p/>
	 * If the value already is a bezier value, it will be returned unchanged. Otherwise, a new {@link BezierParameterValue} will be created
	 * with control points set to provide a smooth transition between <code>previous -> this -> next</code> values.
	 * <p/>
	 * If either of the <code>previousValue</code> or <code>nextValue</code> are <code>null</code>, it will be considered that <code>value</code> is a start-
	 * or end-point.
	 * 
	 * @param valueToConvert The parameter value to convert to a bezier value
	 * @param previousValue The previous value. Used to set initial values for the result control points.
	 * @param nextValue The next value. Used to set initial values for the result control points.
	 * 
	 * @return The converted bezier parameter
	 */
	public static BezierParameterValue convertToBezierParameterValue(ParameterValue valueToConvert, ParameterValue previousValue, ParameterValue nextValue)
	{
		if (valueToConvert.getType() == ParameterValueType.BEZIER)
		{
			return (BezierParameterValue)valueToConvert;
		}
		
		// Create the bezier with the same value and owner as the provided value
		BezierParameterValue result = new BasicBezierParameterValue(valueToConvert.getValue(), valueToConvert.getFrame(), valueToConvert.getOwner());
		result.setLocked(true);
		
		// Default 'in value' to the same as the 'value'. This will result in a horizontal 'in-value-out' control line
		double inValue = valueToConvert.getValue();
		
		// If previous and next points exist, and they exist on different sides of 'value' vertically,
		// use them to choose a better value for 'in' based on the line joining 'previous' and 'next'
		if (previousValue != null && nextValue != null && Math.signum(valueToConvert.getValue() - previousValue.getValue()) != Math.signum(valueToConvert.getValue() - nextValue.getValue()))
		{
			// Compute the gradient of the line joining the previous and next points
			double m = (nextValue.getValue() - previousValue.getValue()) / (nextValue.getFrame() - previousValue.getFrame());
			double x = (nextValue.getFrame() - previousValue.getFrame()) * result.getInPercent();
			
			// Compute the value to use for the in control point such that it lies on the line joining the previous and next lines
			inValue = valueToConvert.getValue() - m * x;
		}
		
		result.setInValue(inValue);
		
		return null;
	}
	
}
