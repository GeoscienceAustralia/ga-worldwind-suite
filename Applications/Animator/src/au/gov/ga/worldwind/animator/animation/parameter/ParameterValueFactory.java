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
	 * 
	 * @return a new parameter value for the given parameter
	 */
	public static ParameterValue createParameterValue(Parameter parameter, double value)
	{
		return createParameterValue(defaultValueType, parameter, value);
	}
	
	/**
	 * Create a new parameter value for the given parameter
	 * 
	 * @param type the type of the value to create
	 * @param parameter The parameter to create the value for
	 * @param value The value of the parameter
	 * 
	 * @return a new parameter value for the given parameter
	 */
	public static ParameterValue createParameterValue(ParameterValueType type, Parameter parameter, double value)
	{
		switch (type)
		{
			case LINEAR:
				return new BasicParameterValue(value, parameter);
			
			// Default case is BEZIER
			default:
				return new BasicBezierParameterValue(value, parameter);
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
		BezierParameterValue result = new BasicBezierParameterValue(valueToConvert.getValue(), valueToConvert.getOwner());
		
		// If there are no other points to use, we can't do much better than this
		if (previousValue == null && nextValue == null)
		{
			return result;
		}
		
		return null;
	}
	
}
