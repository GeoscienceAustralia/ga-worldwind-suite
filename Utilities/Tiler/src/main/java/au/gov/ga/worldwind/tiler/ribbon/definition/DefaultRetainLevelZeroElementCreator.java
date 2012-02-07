package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultRetainLevelZeroElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "RetainLevelZeroTiles";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<RetainLevelZeroTiles>" + context.isRetainLevelZeroTiles() + "</RetainLevelZeroTiles>");
	}

}
