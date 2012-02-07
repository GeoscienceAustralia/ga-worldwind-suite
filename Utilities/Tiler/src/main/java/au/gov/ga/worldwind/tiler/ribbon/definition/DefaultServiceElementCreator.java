package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultServiceElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Service";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer buffer = new StringBuffer();
		
		appendLine(buffer, level, "<Service serviceName=\"DelegatorTileService\">");
		appendLine(buffer, level+1, "<URL>.\\</URL>");
		appendLine(buffer, level, "</Service>");
		
		return buffer.toString();
	}

}
