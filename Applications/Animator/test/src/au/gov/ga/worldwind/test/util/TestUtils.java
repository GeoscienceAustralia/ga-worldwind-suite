package au.gov.ga.worldwind.test.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Utility methods to help when unit testing.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class TestUtils
{
	/**
	 * Read the provided stream into a String
	 */
	public static String readStreamToString(InputStream stream) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer result = new StringBuffer();
		String readLine = null;
		while((readLine = reader.readLine()) != null)
		{
			if (result.length() > 0)
			{
				result.append('\n');
			}
			result.append(readLine);
		}
		reader.close();
		return result.toString();
	}

	/**
	 * @return the field with the given name and type from the provided target object
	 */
	public static <T> T getField(Object target, String name, Class<T> type)
	{
		try
		{
			Field targetField = findField(target.getClass(), name);
			if (targetField == null)
			{
				throw new IllegalArgumentException("Field '" + name + "' does not exist on '" + target.getClass().getSimpleName() + "'.");
			}
			
			targetField.setAccessible(true);
			Object result = targetField.get(target);
			return type.cast(result);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalArgumentException("Cannot access field '" + name + "' on '" + target.getClass().getSimpleName() + "'.", e);
		}
	}
	
	private static Field findField(Class<?> clazz, String fieldName)
	{
		if (clazz == null)
		{
			return null;
		}
		try
		{
			Field result = clazz.getDeclaredField(fieldName);
			return result;
		}
		catch (NoSuchFieldException e)
		{
			return findField(clazz.getSuperclass(), fieldName);
		}
	}
}
