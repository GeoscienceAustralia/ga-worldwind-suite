package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * A Path element creator that looks for a file called <code>source_file_name.txt</code> containing
 * whitespace-separated lines of [longitude latitude elevation_top elevation_bottom]
 */
public class AemPathElementCreator extends AemElementCreatorBase
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

}
