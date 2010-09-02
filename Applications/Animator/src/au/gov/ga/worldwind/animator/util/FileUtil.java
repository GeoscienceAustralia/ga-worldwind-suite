package au.gov.ga.worldwind.animator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil
{
	public static String readFileAsString(File file) throws IOException
	{
		StringBuilder sb = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1)
		{
			sb.append(buf, 0, numRead);
		}
		reader.close();
		return sb.toString();
	}

	public static void writeStringToFile(String string, File file)
			throws IOException
	{
		FileWriter fw = new FileWriter(file);
		fw.append(string);
		fw.close();
	}
	
	/**
	 * @return a string representation of <code>value</code> padded to <code>charcount</code> with leading zeros
	 */
	public static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
	
	/**
	 * Strip file extensions from the provided filename.
	 * <p/>
	 * A file extension if defined as anything after the last period '.'
	 * 
	 * @param filename The name to strip extensions from
	 * 
	 * @return The provided file name, stripped of any file extensions
	 */
	public static String stripExtension(String filename)
	{
		if (filename == null || filename.isEmpty())
		{
			return null;
		}
		
		if (filename.lastIndexOf('.') == -1)
		{
			return filename;
		}
		
		return filename.substring(0, filename.lastIndexOf('.'));
	}
}
