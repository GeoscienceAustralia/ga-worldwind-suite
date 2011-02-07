package au.gov.ga.worldwind.tiler.ribbon.definition;

import java.text.SimpleDateFormat;
import java.util.Date;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultLastUpdateElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "LastUpdate";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return addIndent(level, "<LastUpdate>" + getFormattedDate() + "</LastUpdate>\n");
	}

	private String getFormattedDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy HH:mm:ss Z");
		return formatter.format(new Date());
	}

}
