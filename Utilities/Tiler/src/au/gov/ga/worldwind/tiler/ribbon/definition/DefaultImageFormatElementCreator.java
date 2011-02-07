package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultImageFormatElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "ImageFormat";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return addIndent(level, "<ImageFormat>image/" + context.getFormat() + "</ImageFormat>\n");
	}

}
