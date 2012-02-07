package au.gov.ga.worldwind.common.layers.curtain;

import java.net.URL;

/**
 * Generates URLs for downloading {@link CurtainTile} data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CurtainTileUrlBuilder
{
	public URL getURL(CurtainTile tile, String imageFormat) throws java.net.MalformedURLException;
}
