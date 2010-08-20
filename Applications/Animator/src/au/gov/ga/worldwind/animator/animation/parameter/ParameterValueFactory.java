/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;


/**
 * A factory class that maintains the current 'default' value type 
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
	 * @param frame The frame the value is associated with
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
}
