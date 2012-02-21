package au.gov.ga.worldwind.dataprep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zipper
{
	public static void main(String[] args)
	{
		File dir = new File("V:/projects/presentations/11-5902 - Broken Hill 3D model data visualisation/Worldwind/layers/dem/mos_dem_10box_ers_wgs84");
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

				zip(file, zip);

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

				unzip(file, file.getParentFile());

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
	
	public static void zip(File input, File output) throws IOException, InterruptedException
	{
		String command =
				"7za a -tzip \"" + output.getAbsolutePath() + "\" \"" + input.getAbsolutePath()
						+ "\"";
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
	}
	
	public static void unzip(File input, File outputDir) throws IOException
	{
		byte[] buffer = new byte[10240];

		ZipInputStream zis = null;
		FileOutputStream fos = null;

		try
		{
			zis = new ZipInputStream(new FileInputStream(input));
			ZipEntry entry = zis.getNextEntry();
			File output = new File(outputDir, entry.getName());
			fos = new FileOutputStream(output);

			int len;
			while ((len = zis.read(buffer)) >= 0)
			{
				fos.write(buffer, 0, len);
			}
		}
		finally
		{
			if (zis != null)
				zis.close();
			if (fos != null)
				fos.close();
		}
	}
}
