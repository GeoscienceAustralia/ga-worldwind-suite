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
package au.gov.ga.worldwind.tiler.ribbon.definition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * A {@link LayerDefinitionCreator} that creates a <code>&lt;path&gt;</code> element by
 * copying the contents of a file with the name <code>[tileset name].path</code>
 */
public class PathFilePathElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Path";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(getPathFile(context)));
			String line;
			while ((line = reader.readLine()) != null)
			{
				appendLine(result, level+1, line);
			}
		}
		catch (Exception e)
		{
			// Do nothing. In the case of an exception we will just print the empty <path> element
		}
		appendLine(result, level, "</Path>");
		return result.toString();
	}
	
	private File getPathFile(RibbonTilingContext context)
	{
		File pathFile = new File(context.getSourceLocation(), context.getTilesetName() + ".path");
		return pathFile;
	}

}
