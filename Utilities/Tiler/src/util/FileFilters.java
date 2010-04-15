package util;

import java.io.File;
import java.io.FileFilter;

public class FileFilters
{
	public static class DirectoryFileFilter implements FileFilter
	{
		public boolean accept(File f)
		{
			return f.isDirectory();
		}
	}

	public static class ExtensionFileFilter implements FileFilter
	{
		private String extension;

		public ExtensionFileFilter(String extension)
		{
			this.extension = extension;
		}

		public boolean accept(File pathname)
		{
			return !pathname.isDirectory()
					&& pathname.getName().toLowerCase().endsWith(
							extension.toLowerCase());
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
