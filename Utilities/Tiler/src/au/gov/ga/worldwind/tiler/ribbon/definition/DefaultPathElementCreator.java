package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * Default implementation that uses a list of pipe-separated lat-lons stored on the context
 */
public class DefaultPathElementCreator extends LayerDefinitionElementCreatorBase
{

	@Override
	public String getElementName()
	{
		return "Path";
	}

	@Override
	public String getElementString(int level, RibbonTilingContext context)
	{
		StringBuffer result = new StringBuffer();
		appendLine(result, level, "<Path>");
		for (String latLonPair : context.getPathLatLons())
		{
			String[] latLon = latLonPair.split("|");
			appendLine(result, level+1, "<LatLon units=\"degrees\" latitude=\"" + latLon[0] + "\" longitude=\"" + latLon[1] + "\"/>" );
		}
		appendLine(result, level, "</Path>");
		return result.toString();
	}

}
