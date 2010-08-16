package au.gov.ga.worldwind.tiler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zipper
{
	public static void zip(File input, File output) throws IOException, InterruptedException
	{
		String command =
				"7za a -tzip \"" + output.getAbsolutePath() + "\" \"" + input.getAbsolutePath()
						+ "\"";
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();


		//The following Java method of zipping doesn't set the correct ZipEntry.getSize(), so
		//use a command line version of 7-Zip instead (above).


		/*byte[] buffer = new byte[10240];

		ZipOutputStream zos = null;
		FileInputStream fis = null;

		try
		{
			zos = new ZipOutputStream(new FileOutputStream(output));
			ZipEntry ze = new ZipEntry(input.getName());
			zos.putNextEntry(ze);

			fis = new FileInputStream(input);
			int len;
			while ((len = fis.read(buffer)) >= 0)
			{
				zos.write(buffer, 0, len);
			}

			zos.closeEntry();
		}
		finally
		{
			if (fis != null)
				fis.close();
			if (zos != null)
				zos.close();
		}*/
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
