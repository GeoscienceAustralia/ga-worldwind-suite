package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;
import au.gov.ga.worldwind.tiler.ribbon.definition.LayerDefinitionElementCreatorBase;
import au.gov.ga.worldwind.tiler.util.Util;

/**
 * A Path element creator that looks for a file called <code>source_file_name.txt</code> containing
 * whitespace-separated lines of [longitude latitude elevation_top elevation_bottom]
 */
public class AemPathElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Path";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		addLatLonsFromDataFile(result, level+1, context);
		appendLine(result, level, "</Path>");
		return result.toString();
	}

	private void addLatLonsFromDataFile(StringBuffer result, int level, RibbonTilingContext context)
	{
		FileReader dataFileReader = getDataFileReader(context);
		if (dataFileReader == null)
		{
			return;
		}
		
		CSVReader csvReader = new CSVReader(dataFileReader, '\t', '\\', 1);
		try
		{
			String[] nextLine;
			while ((nextLine = csvReader.readNext()) != null)
			{
				appendLine(result, level, "<LatLon units=\"degrees\" latitude=\"" + nextLine[1] + "\" longitude=\"" + nextLine[0] + "\"/>");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	private FileReader getDataFileReader(RibbonTilingContext context)
	{
		try
		{
			return new FileReader(new File(context.getSourceLocation(), Util.stripExtension(context.getSourceFile().getName()) + ".txt"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
