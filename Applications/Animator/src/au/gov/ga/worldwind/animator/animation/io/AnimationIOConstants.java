package au.gov.ga.worldwind.animator.animation.io;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface defining accessors for constants used during animation IO.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationIOConstants
{
	/**
	 * @return The context key for the parameter value owner. Of type {@link Parameter}.
	 */
	String getParameterValueOwnerKey();
	
	/**
	 * @return The name of the 'parameter value' element
	 */
	String getParameterValueElementName();
	
	/**
	 * @return The name of the 'value' attribute on the parameter value element
	 */
	String getParameterValueAttributeValue();
	
	/**
	 * @return The name of the 'frame' attribute on the parameter value element
	 */
	String getParameterValueAttributeFrame();
	
	/**
	 * @return The name of the 'type' attribute on the parameter value element
	 */
	String getParameterValueAttributeType();
	
	/**
	 * @return The name of the 'in value' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeInValue();

	/**
	 * @return The name of the 'in percent' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeInPercent();

	/**
	 * @return The name of the 'out value' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeOutValue();

	/**
	 * @return The name of the 'out percent' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeOutPercent();

	/**
	 * @return The name of the 'locked' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeLocked();

	
	/**
	 * Common constants that don't change across versions
	 */
	public static abstract class BaseConstants implements AnimationIOConstants
	{
		@Override
		public String getParameterValueOwnerKey() {return "parameterValueOwner";}
		
	}
	
	/**
	 * Implementation of the {@link AnimationIOConstants} for V2 files
	 */
	public static class V2 extends BaseConstants implements AnimationIOConstants
	{
		@Override
		public String getParameterValueElementName() {return "parameterValue";}
		
		@Override
		public String getParameterValueAttributeValue() {return "value";}
		
		@Override
		public String getParameterValueAttributeFrame() {return "frame";}
		
		@Override
		public String getParameterValueAttributeType() {return "type";}

		@Override
		public String getBezierValueAttributeInValue() {return "inValue";}

		@Override
		public String getBezierValueAttributeInPercent() {return "inPercent";}

		@Override
		public String getBezierValueAttributeOutValue() {return "outValue";}

		@Override
		public String getBezierValueAttributeOutPercent() {return "outPercent";}
		@Override
		public String getBezierValueAttributeLocked() {return "locked";}
		
	}

}
