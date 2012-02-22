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
