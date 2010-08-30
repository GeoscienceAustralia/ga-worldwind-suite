/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;


/**
 * A factory class that maintains the current 'default' value type 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ParameterValueFactory
{
	/** The current 'default' parameter value type */
	private static ParameterValueType defaultValueType = ParameterValueType.BEZIER;
	
	/** An instance of the {@link BasicParameterValue} class to act as a factory for that class when de-serialising */
	private static BasicParameterValue BASIC_VALUE_FACTORY = new BasicParameterValue();
	
	/** An instance of the {@link BasicBezierParameterValue} class to act as a factory for that class when de-serialising */
	private static BasicBezierParameterValue BEZIER_VALUE_FACTORY = new BasicBezierParameterValue();
	
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
	
	/**
	 * Create a parameter value from the provided XML element.
	 * 
	 * @param element The element to de-serialise from
	 * @param version The ID of the version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return The parameter value, created from the provided XML.
	 */
	public static ParameterValue fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		ParameterValueType type = ParameterValueType.valueOf(WWXML.getText(element, XmlSerializable.ATTRIBUTE_PATH_PREFIX + version.getConstants().getParameterValueAttributeType()));
		switch (type)
		{
			case LINEAR:
				return BASIC_VALUE_FACTORY.fromXml(element, version, context);
			
			default:
				return BEZIER_VALUE_FACTORY.fromXml(element, version, context);
		}
	}
}
