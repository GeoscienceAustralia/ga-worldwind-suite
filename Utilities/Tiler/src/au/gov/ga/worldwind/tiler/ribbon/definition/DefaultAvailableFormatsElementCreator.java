package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultAvailableFormatsElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "AvailableImageFormats";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer buffer = new StringBuffer();
		appendLine(buffer, level, "<AvailableImageFormats>");
		appendLine(buffer, level+1, "<ImageFormat>image/" + context.getFormat() + "</ImageFormat>");
		appendLine(buffer, level, "</AvailableImageFormats>");
		return buffer.toString();
	}

}
