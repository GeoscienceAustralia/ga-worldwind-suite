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
	 * Common constants that don't change across versions
	 */
	public static abstract class BaseConstants implements AnimationIOConstants
	{
		@Override
		public String getParameterValueOwnerKey() {return "parameterValueOwner";};
		
	}
	
	/**
	 * Implementation of the {@link AnimationIOConstants} for V2 files
	 */
	public static class V2 extends BaseConstants implements AnimationIOConstants
	{
	}
}
