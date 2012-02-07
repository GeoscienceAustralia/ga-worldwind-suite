package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultFormatSuffixElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "FormatSuffix";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<FormatSuffix>." + context.getFormat().toLowerCase() + "</FormatSuffix>");
	}

}
