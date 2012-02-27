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

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import au.gov.ga.worldwind.tiler.util.FileFilters.DirectoryFileFilter;

/**
 * Utility class containing functions to help with file handling.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileUtil
{
	public static void recursivelyAddFiles(Collection<File> files, File dir, FileFilter filter)
	{
		File[] list = dir.listFiles(filter);
		if (list != null)
		{
			for (File file : list)
			{
				files.add(file);
			}
		}
		File[] dirs = dir.listFiles(new DirectoryFileFilter());
		if (dirs != null)
		{
			for (File d : dirs)
			{
				recursivelyAddFiles(files, d, filter);
			}
		}
	}

	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
}
