package au.gov.ga.worldwind.common.util;

/**
 * Util class that provides some commonly used validation methods.
 * <p/>
 * Based on the Apache Commons Validate class
 */
public class Validate
{

	private static final String DEFAULT_NOT_NULL_MSG = "Value cannot be null";
	private static final String DEFAULT_IS_TRUE_MSG = "Value is false when expected true";
	private static final String DEFAULT_NOT_BLANK_MSG = "Value cannot be blank";
	

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
		if (object == null) 
		{
			throw new IllegalArgumentException(msg == null ? DEFAULT_NOT_NULL_MSG : msg);
		}
	}
	
	/**
	 * Validate that the provided boolean value is <code>true</code>.
	 * 
	 * @param value The value to validate
	 * @param msg The message to include in any generated exceptions
	 * 
	 * @throws IllegalArgumentException if the provided boolean value is <code>false</code>
	 */
	public static void isTrue(boolean value, String msg) throws IllegalArgumentException
	{
		if (value == false) 
		{
			throw new IllegalArgumentException(msg == null ? DEFAULT_IS_TRUE_MSG : msg);
		}
	}

	/**
	 * Validate that the provided string contains non-whitespace characters.
	 * 
	 * @param string The string to validate
	 * @param msg The message to include in any generated exceptions
	 * 
	 * @throws IllegalArgumentException if the provided string is <code>null</code> or <code>empty</code>
	 */
	public static void notBlank(String string, String msg)
	{
		if (string == null || string.trim() == "")
		{
			throw new IllegalArgumentException(msg == null ? DEFAULT_NOT_BLANK_MSG : msg);
		}
		
	}
}
