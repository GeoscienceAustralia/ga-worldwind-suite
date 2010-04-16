package tiler;

import java.io.File;
import java.io.FileNotFoundException;

public class NativeGDALLibs
{
	private static String[] libraries = new String[]
	{ "xerces-c_2_8", "libexpat", "libpq", "proj", "iconv", "geos_c",
			"spatialite", "libcurl", "gdal17", "gdaljni" };

	public static void loadLibraries(File baseDir) throws FileNotFoundException
	{
		String suffix = ".dll";

		for (String lib : libraries)
		{
			String filename = lib + suffix;
			File file = new File(baseDir, filename);
			if (file.exists())
			{
				try
				{
					System.load(file.getAbsolutePath());
					System.out.println("Loaded " + file.getAbsolutePath());
				}
				catch (Error e)
				{
				}
				catch (Exception e)
				{
				}
			}
			else
			{
				throw new FileNotFoundException("Could not find library: "
						+ filename);
			}
		}
	}
}
