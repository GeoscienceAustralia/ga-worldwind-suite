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
package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * A Path element creator that looks for a file called <code>source_file_name.txt</code> containing
 * whitespace-separated lines of [longitude latitude elevation_top elevation_bottom]
 */
public class AemPathElementCreator extends AemElementCreatorBase
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
		addLatLonsFromDataFile(result, level+1, context);
		appendLine(result, level, "</Path>");
		return result.toString();
	}

	private void addLatLonsFromDataFile(StringBuffer result, int level, RibbonTilingContext context)
	{
		FileReader dataFileReader = getDataFileReader(context);
		if (dataFileReader == null)
		{
			return;
		}
		
		CSVReader csvReader = new CSVReader(dataFileReader, '\t', '\\', 1);
		try
		{
			String[] nextLine;
			while ((nextLine = csvReader.readNext()) != null)
			{
				appendLine(result, level, "<LatLon units=\"degrees\" latitude=\"" + nextLine[1] + "\" longitude=\"" + nextLine[0] + "\"/>");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

}
