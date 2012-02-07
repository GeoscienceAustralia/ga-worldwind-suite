package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultCurtainBottomElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "CurtainBottom";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return formatLine(level, "<CurtainBottom>" + context.getCurtainBottom() + "</CurtainBottom>");
	}

}
