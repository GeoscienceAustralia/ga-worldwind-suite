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

/**
 * Helper class containing a few static {@link FileFilter} implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileFilters
{
	/**
	 * {@link FileFilter} implementation that only accepts directories.
	 */
	public static class DirectoryFileFilter implements FileFilter
	{
		public boolean accept(File f)
		{
			return f.isDirectory();
		}
	}

	/**
	 * {@link FileFilter} implementation that accepts files that end with a
	 * given extension suffix.
	 */
	public static class ExtensionFileFilter implements FileFilter
	{
		private String extension;

		public ExtensionFileFilter(String extension)
		{
			this.extension = extension;
		}

		public boolean accept(File pathname)
		{
			return !pathname.isDirectory() && pathname.getName().toLowerCase().endsWith(extension.toLowerCase());
		}

		public String getExtension()
		{
			return extension;
		}

		public void setExtension(String extension)
		{
			this.extension = extension;
		}
	}
}
