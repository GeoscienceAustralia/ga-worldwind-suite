package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultDisplayNameElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "DisplayName";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer buffer = new StringBuffer();
		appendLine(buffer, level, "<DisplayName>" + context.getTilesetName() + "</DisplayName>");
		return buffer.toString();
	}

}
