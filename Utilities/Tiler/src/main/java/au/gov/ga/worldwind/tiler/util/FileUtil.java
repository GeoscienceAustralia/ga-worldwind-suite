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
