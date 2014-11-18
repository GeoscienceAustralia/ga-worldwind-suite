/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.animation.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.math.vector.Vector2;


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

	/** The default scaling to apply to generated bezier control points when generating from linear values */
	private static final double DEFAULT_BEZIER_CONTROL_SCALE = 0.4;
	
	
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
	 * Convert the provided parameter value to the given type, using sensible defaults if more information is required
	 */
	public static ParameterValue convertParameterValue(ParameterValue valueToConvert, ParameterValueType type)
	{
		if (valueToConvert == null || valueToConvert.getType() == type)
		{
			return valueToConvert;
		}
		
		ParameterValue result = null;
		
		if (valueToConvert.getType() == ParameterValueType.BEZIER && type == ParameterValueType.LINEAR)
		{
			// Bezier -> Linear is easy, just ignore the bezier in/out values
			result = new BasicParameterValue(valueToConvert.getValue(), valueToConvert.getFrame(), valueToConvert.getOwner());
		}
		else if (valueToConvert.getType() == ParameterValueType.LINEAR && type == ParameterValueType.BEZIER)
		{
			// Linear -> Bezier is a bit more tricky - use an unlocked bezier with handles pointing at adjacent values, as if it were linear
			result = toBezierValue(valueToConvert);
		}
		
		// Attach all of the listeners to the new value
		result.clearChangeListeners();
		valueToConvert.copyChangeListenersTo(result);
		
		// Don't know how to do any other conversion....
		return result == null ? valueToConvert : result;
	}
	
	/**
	 * Convert the provided parameter value to a Bezier value.
	 * <p/>
	 * If is already a bezier value, won't change anything. Otherwise, will create an unlocked 'linear' bezier value
	 * (i.e. a bezier value whose control points are setup to point to adjacent key frame values to mimic a linear point).
	 */
	private static ParameterValue toBezierValue(ParameterValue valueToConvert)
	{
		// If it already is a bezier value, don't need to do anything
		if (valueToConvert.getType() == ParameterValueType.BEZIER)
		{
			return (BezierParameterValue)valueToConvert;
		}
		
		// Otherwise, set the appropriate control point to point at the other value (mimic a linear point)
		Vector2 thisPoint = new Vector2(valueToConvert.getFrame(), valueToConvert.getValue());
		Double inValue = null;
		Double outValue = null;
		
		// Set the 'in' handle appropriately, if there is a previous value
		ParameterValue previousValue = valueToConvert.getOwner().getValueAtKeyFrameBeforeFrame(valueToConvert.getFrame());
		if (previousValue != null)
		{
			Vector2 previousPoint = new Vector2(previousValue.getFrame(), previousValue.getValue());
			Vector2 controlPoint = thisPoint.add((previousPoint.subtract(thisPoint)).mult(DEFAULT_BEZIER_CONTROL_SCALE));
			
			inValue = controlPoint.y;
		}

		// Set the 'out' handle appropriately, if there is a next value
		ParameterValue nextValue = valueToConvert.getOwner().getValueAtKeyFrameAfterFrame(valueToConvert.getFrame());
		if (nextValue != null)
		{
			Vector2 nextPoint = new Vector2(nextValue.getFrame(), nextValue.getValue());
			Vector2 controlPoint = thisPoint.add((nextPoint.subtract(thisPoint)).mult(DEFAULT_BEZIER_CONTROL_SCALE));
			
			outValue = controlPoint.y;
		}
		
		return new BasicBezierParameterValue(valueToConvert.getValue(), valueToConvert.getFrame(), valueToConvert.getOwner(), inValue, outValue);
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
		ParameterValueType type = ParameterValueType.valueOf(element.getAttribute(version.getConstants().getParameterValueAttributeType()));
		switch (type)
		{
			case LINEAR:
				return BASIC_VALUE_FACTORY.fromXml(element, version, context);
			
			default:
				return BEZIER_VALUE_FACTORY.fromXml(element, version, context);
		}
	}
}
