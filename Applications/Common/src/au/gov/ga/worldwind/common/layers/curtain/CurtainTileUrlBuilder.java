package au.gov.ga.worldwind.common.layers.curtain;

import java.net.URL;

public interface CurtainTileUrlBuilder
{
	public URL getURL(CurtainTile tile, String imageFormat) throws java.net.MalformedURLException;
}
