package au.gov.ga.worldwind.common.util;

/**
 * Utilities for working with files
 */
public class FileUtil
{
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
		if (Util.isBlank(filename))
		{
			return null;
		}
		
		if (filename.lastIndexOf('.') == -1)
		{
			return filename;
		}
		
		return filename.substring(0, filename.lastIndexOf('.'));
	}
	
	/**
	 * Return the file extension of the provided filename, or <code>null</code> if
	 * one does not exist.
	 * <p/>
	 * A file extension if defined as anything after the last period '.'
	 * <p/>
	 * The returned extension does <b>not</b> include the '.' character. 
	 * 
	 * @return The file extension of the provided filename, or <code>null</code> if none exists
	 */
	public static String getExtension(String filename)
	{
		if (Util.isBlank(filename))
		{
			return null;
		}
		
		if (filename.lastIndexOf('.') == -1)
		{
			return null;
		}
		
		return filename.substring(filename.lastIndexOf('.')+1);
	}
	
	/**
	 * Returns whether or not the provided filename has the provided extension.
	 * <p/>
	 * Comparisons are case-insensitive, and '.' characters are ignored.
	 * 
	 * @return <code>true</code> if the provided file has the provided extension. <code>false</code> otherwise.
	 */
	public static boolean hasExtension(String filename, String extension)
	{
		if (Util.isBlank(extension))
		{
			return false;
		}
		
		String fileExtension = getExtension(filename);
		if (Util.isBlank(fileExtension))
		{
			return false;
		}
		
		return fileExtension.equalsIgnoreCase(extension.replace(".", ""));
	}
}
