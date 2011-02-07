package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultDataCacheElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "DataCacheName";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return addIndent(level, "<DataCacheName>" + context.getDataCacheName() + "</DataCacheName>\n");
	}

}
