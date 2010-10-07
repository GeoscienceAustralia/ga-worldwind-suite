package au.gov.ga.worldwind.animator.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Holds commonly used file filters
 */
public class FileFilters
{
	
	private static final XmlFilter XML_FILTER_INSTANCE = new XmlFilter();
	public static final XmlFilter getXmlFilter()
	{
		return XML_FILTER_INSTANCE;
	}
	
	/**
	 * A simple file filter that matches XML files with extension <code>.xml</code> 
	 */
	public static class XmlFilter extends FileFilter
	{
		/**
		 * @return The file extension associated with this filter
		 */
		public static String getFileExtension() { return ".xml";}
		
		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				return true;
			}
			return f.getName().toLowerCase().endsWith(getFileExtension());
		}

		@Override
		public String getDescription()
		{
			return "XML files (*.xml)";
		}
	}
}
