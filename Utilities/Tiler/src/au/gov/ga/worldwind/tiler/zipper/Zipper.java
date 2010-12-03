package au.gov.ga.worldwind.tiler.zipper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Zipper
{
	public static void main(String[] args)
	{
		File dir = new File("D:/Bathymetry/ausbath09_r3");
		String ext = "bil";
		zipAndDelete(dir, ext);
		//unzipAndDelete(dir);
	}

	public static void zipAndDelete(File dir, String ext)
	{
		System.out.println("Searching for files");

		List<File> files = new ArrayList<File>();
		addMatchingFiles(files, dir, ext.toLowerCase());

		System.out.println("Found " + files.size() + " files");

		try
		{
			for (int i = 0; i < files.size(); i++)
			{
				File file = files.get(i);

				System.out.println("Zipping " + file + " (" + (i + 1) + "/" + files.size() + " - "
						+ ((i + 1) * 100 / files.size()) + "%)");

				String filename = file.getAbsolutePath();
				filename = filename.substring(0, filename.length() - ext.length()) + "zip";
				File zip = new File(filename);

				au.gov.ga.worldwind.tiler.util.Zipper.zip(file, zip);

				file.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void unzipAndDelete(File dir)
	{
		System.out.println("Searching for files");

		List<File> files = new ArrayList<File>();
		addMatchingFiles(files, dir, "zip");

		System.out.println("Found " + files.size() + " files");

		try
		{
			for (int i = 0; i < files.size(); i++)
			{
				File file = files.get(i);

				System.out.println("Unzipping " + file + " (" + (i + 1) + "/" + files.size() + " - "
						+ ((i + 1) * 100 / files.size()) + "%)");

				au.gov.ga.worldwind.tiler.util.Zipper.unzip(file, file.getParentFile());

				file.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected static void addMatchingFiles(List<File> list, File dir, String ext)
	{
		File[] files = dir.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				addMatchingFiles(list, file, ext);
			}
			else
			{
				if (file.getName().toLowerCase().endsWith("." + ext))
				{
					list.add(file);
				}
			}
		}
	}
}
