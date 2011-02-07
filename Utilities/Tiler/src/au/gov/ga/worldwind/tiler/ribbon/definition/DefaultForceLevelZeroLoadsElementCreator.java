package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultForceLevelZeroLoadsElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "ForceLevelZeroLoads";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<ForceLevelZeroLoads>" + context.isForceLevelZeroLoads() + "</ForceLevelZeroLoads>");
	}

}
