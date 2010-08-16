package au.gov.ga.worldwind.animator.util;

/**
 * Util class that provides some commonly used validation methods.
 * <p/>
 * Based on the Apache Commons Validate class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class Validate
{

	/**
	 * Validate that the provided object is not <code>null</code>.
	 * 
	 * @param object The object to validate
	 * @param msg The message to include in any generated exceptions
	 * 
	 * @throws IllegalArgumentException if the provided object is <code>null</code>
	 */
	public static void notNull(Object object, String msg) throws IllegalArgumentException
	{
		if (object == null) {
			throw new IllegalArgumentException(msg);
		}
	}
}
