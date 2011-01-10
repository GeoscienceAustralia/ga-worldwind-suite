package au.gov.ga.worldwind.animator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utilities for working with files
 */
public class FileUtil extends au.gov.ga.worldwind.common.util.FileUtil
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
}
