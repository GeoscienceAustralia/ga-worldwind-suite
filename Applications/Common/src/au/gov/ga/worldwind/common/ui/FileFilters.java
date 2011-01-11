package au.gov.ga.worldwind.common.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	
	private static final FileNameExtensionFilter TGA_FILE_FILTER_INSTANCE = new FileNameExtensionFilter("TGA Image Sequence", "tga");
	public static final FileNameExtensionFilter getTgaFilter()
	{
		return TGA_FILE_FILTER_INSTANCE;
	}
	
	private static final LayerDefinitionFileFilter LAYER_DEFINITION_FILTER_INSTANCE = new LayerDefinitionFileFilter();
	public static final LayerDefinitionFileFilter getLayerDefinitionFilter()
	{
		return LAYER_DEFINITION_FILTER_INSTANCE;
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
	
	/**
	 * Matches layer definition files with the extension <code>.xml</code>
	 */
	public static class LayerDefinitionFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return "Layer definition file (*.xml)";
		}

		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
			{
				return true;
			}
			return f.getName().toLowerCase().endsWith(".xml");
		}
	}
}
