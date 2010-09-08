package au.gov.ga.worldwind.test.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	
}
