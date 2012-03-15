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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * {@link BufferedWriter} subclass that adds functionality for writing lines
 * within one function call.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BufferedLineWriter extends BufferedWriter
{
	public BufferedLineWriter(Writer out)
	{
		super(out);
	}

	/**
	 * Write the given string, and then a newline.
	 * 
	 * @param str
	 *            String to write
	 * @throws IOException
	 */
	public void writeLine(String str) throws IOException
	{
		write(str);
		newLine();
	}
}
