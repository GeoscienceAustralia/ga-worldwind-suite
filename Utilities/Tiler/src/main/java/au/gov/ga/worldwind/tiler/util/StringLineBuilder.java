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
package au.gov.ga.worldwind.tiler.util;

/**
 * Helper class that uses an internal {@link StringBuilder} to build a string
 * from a number of lines.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StringLineBuilder
{
	private StringBuilder s;
	private String newLine;

	public StringLineBuilder()
	{
		s = new StringBuilder();
		newLine = System.getProperty("line.separator");
	}

	public void appendLine(String str)
	{
		s.append(str + newLine);
	}

	@Override
	public String toString()
	{
		return toString(false);
	}

	public String toString(boolean removeLastLine)
	{
		String str = s.toString();
		if (removeLastLine && str.endsWith(newLine))
			return str.substring(0, str.length() - newLine.length());
		return str;
	}
}
