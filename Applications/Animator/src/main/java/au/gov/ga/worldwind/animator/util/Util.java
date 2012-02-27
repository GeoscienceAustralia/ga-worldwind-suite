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
package au.gov.ga.worldwind.animator.util;

import java.util.Random;

/**
 * Commonly used utility methods.
 * <p/>
 * Some are based on the Apache Commons util classes (StringUtil etc.)
 */
public class Util extends au.gov.ga.worldwind.common.util.Util
{
	/**
	 * Generate a random string of alpha characters (mixed case), of the given length.
	 * 
	 * @param length The length of the string to generate
	 */
	public static String randomAlphaString(int length)
	{
		String chars = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}
	
	/**
	 * Attempts to look up the application version in the following order:
	 * <ol>
	 * 	<li>From a manifest file <code>Implementation-Version</code> number
	 * 	<li>From a <code>Implementation-Version</code> environment variable
	 * </ol>
	 */
	public static String getVersion()
	{
		String result = null;
		
		Package p = Util.class.getPackage();
		if (p != null)
		{
			result = p.getImplementationVersion();
		}
		if (isBlank(result))
		{
			result = System.getenv("Implementation-Version");
		}
		return result;
	}
}
