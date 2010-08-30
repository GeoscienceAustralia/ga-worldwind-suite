package au.gov.ga.worldwind.animator.animation.io;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface defining accessors for constants used during animation IO.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationIOConstants
{
	// -------------------------------------
	// Context keys
	// -------------------------------------
	
	/**
	 * @return The context key for the animation in context. Of type {@link Animation}.
	 */
	String getAnimationKey();
	
	/**
	 * @return The context key for the parameter value owner. Of type {@link Parameter}.
	 */
	String getParameterValueOwnerKey();
	
	// -------------------------------------
	// XML elements and attributes
	// -------------------------------------
	
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
	 * @return The name of the 'default value' attribute on the parameter element
	 */
	String getParameterAttributeDefaultValue();
	
	/**
	 * @return The name of the 'name' attribute on the parameter element
	 */
	String getParameterAttributeName();
	
	/**
	 * @return The name of the 'enabled' attribute on the parameter element
	 */
	String getParameterAttributeEnabled();
	
	/**
	 * @return The name of the 'parameter' element
	 */
	String getParameterElementName();
	
	/**
	 * @return The name of the 'eye lat' element of the camera
	 */
	String getCameraEyeLatElementName();
	
	/**
	 * @return The name of the 'eye lon' element of the camera
	 */
	String getCameraEyeLonElementName();
	
	/**
	 * @return The name of the 'eye elevation' element of the camera
	 */
	String getCameraEyeElevationElementName();
	
	/**
	 * @return The name of the 'Lookat lat' element of the camera
	 */
	String getCameraLookatLatElementName();
	
	/**
	 * @return The name of the 'Lookat lon' element of the camera
	 */
	String getCameraLookatLonElementName();
	
	/**
	 * @return The name of the 'Lookat elevation' element of the camera
	 */
	String getCameraLookatElevationElementName();
	
	/**
	 * @return The name of the 'name' attribute of the camera
	 */
	String getCameraAttributeName();
	
	/**
	 * @return The name of the 'camera' element
	 */
	String getCameraElementName();
	
	/**
	 * Common constants that don't change across versions
	 */
	public static abstract class BaseConstants implements AnimationIOConstants
	{
		@Override
		public String getParameterValueOwnerKey() {return "parameterValueOwner";}
		
		@Override
		public String getAnimationKey() {return "currentAnimation";}
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
		
		@Override
		public String getParameterAttributeDefaultValue() {return "defaultValue";}
		
		@Override
		public String getParameterAttributeName() {return "name";}
		
		@Override
		public String getParameterElementName() {return "parameter";}
		
		@Override
		public String getParameterAttributeEnabled() {return "enabled";}
		
		@Override
		public String getCameraEyeLatElementName() {return "eyeLat";}
		
		@Override
		public String getCameraEyeLonElementName() {return "eyeLon";}
		
		@Override
		public String getCameraEyeElevationElementName() {return "eyeElevation";}
		
		@Override
		public String getCameraLookatLatElementName() {return "lookAtLat";}
		
		@Override
		public String getCameraLookatLonElementName() {return "lookAtLon";}
		
		@Override
		public String getCameraLookatElevationElementName() {return "lookAtElevation";}
		
		@Override
		public String getCameraAttributeName() {return "name";}
		
		@Override
		public String getCameraElementName() {return "camera";}
	}
}
