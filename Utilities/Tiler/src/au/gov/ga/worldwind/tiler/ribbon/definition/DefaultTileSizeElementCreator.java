package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

public class DefaultTileSizeElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "TileSize";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<TileSize>");
		appendLine(result, level+1, "<Dimension width=\"" + context.getTilesize() + "\" height=\"" + context.getTilesize() + "\"/>");
		appendLine(result, level, "</TileSize>");
		return result.toString();
	}

}
