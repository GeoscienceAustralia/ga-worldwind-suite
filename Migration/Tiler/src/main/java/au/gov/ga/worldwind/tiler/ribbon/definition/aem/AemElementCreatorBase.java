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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;
import au.gov.ga.worldwind.tiler.ribbon.definition.LayerDefinitionElementCreatorBase;
import au.gov.ga.worldwind.tiler.util.Util;

public abstract class AemElementCreatorBase extends LayerDefinitionElementCreatorBase
{

	protected FileReader getDataFileReader(RibbonTilingContext context)
	{
		try
		{
			return new FileReader(new File(context.getSourceLocation(), Util.stripExtension(context.getSourceFile().getName()) + ".txt"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	

}
