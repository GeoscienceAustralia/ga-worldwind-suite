package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultFullSizeElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "FullSize";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<FullSize>");
		appendLine(result, level+1, "<Dimension width=\"" + context.getSourceImageSize().width + "\" height=\"" + context.getSourceImageSize().height + "\"/>");
		appendLine(result, level, "</FullSize>");
		return result.toString();
	}

}
