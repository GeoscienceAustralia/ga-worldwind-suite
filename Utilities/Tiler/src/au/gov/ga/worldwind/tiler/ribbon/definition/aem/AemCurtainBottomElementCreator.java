package au.gov.ga.worldwind.tiler.ribbon.definition.aem;

import java.io.BufferedReader;
import java.io.FileReader;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class AemCurtainBottomElementCreator extends AemElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "CurtainBottom";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<CurtainBottom>" + getCurtainTop(context) + "</CurtainBottom>");
	}

	private String getCurtainTop(RibbonTilingContext context)
	{
		FileReader dataFileReader = getDataFileReader(context);
		if (dataFileReader == null)
		{
			return "";
		}
		try
		{
			BufferedReader bufferedReader = new BufferedReader(dataFileReader);
			bufferedReader.readLine();
			
			String[] dataLine = bufferedReader.readLine().split("\t");
			return dataLine[3];
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
