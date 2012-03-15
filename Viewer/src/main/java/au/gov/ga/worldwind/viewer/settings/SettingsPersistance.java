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
package au.gov.ga.worldwind.viewer.settings;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Used to save/restore a Settings object to/from an XML file. Currently uses
 * the {@link XMLEncoder} and {@link XMLDecoder} to serialize the object to XML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SettingsPersistance
{
	//TODO don't use XMLEncoder, but rather write a custom XML file

	public static Settings load(Object source)
	{
		/*Settings settings = new Settings();

		Element element = XMLUtil.getElementFromSource(source);
		if (element != null)
		{
		}
		
		return settings;*/



		Settings settings = null;
		File file = (File) source;
		if (file.exists())
		{
			XMLDecoder xmldec = null;
			try
			{
				FileInputStream fis = new FileInputStream(file);
				xmldec = new XMLDecoder(fis);
				settings = (Settings) xmldec.readObject();
			}
			catch (Exception e)
			{
				settings = null;
			}
			finally
			{
				if (xmldec != null)
					xmldec.close();
			}
		}
		return settings;
	}

	public static void save(Settings settings, File file)
	{
		XMLEncoder xmlenc = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			xmlenc = new XMLEncoder(fos);
			xmlenc.writeObject(settings);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (xmlenc != null)
				xmlenc.close();
		}
	}
}
