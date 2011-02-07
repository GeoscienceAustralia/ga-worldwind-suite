package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultDatasetNameElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "DatasetName";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		return addIndent(level, "<DatasetName>" + context.getTilesetName() + "</DatasetName>\n");
	}

}
