package au.gov.ga.worldwind.wmsbrowser.search;

import java.net.URL;

import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * An interface for search results yeilded by a {@link WmsServerSearchService}
 */
public interface WmsServerSearchResult
{
	
	/**
	 * @return The WMS server this result relates to. May or may not be returned in a 'loaded' state.
	 */
	WmsServer getWmsServer();
	
	/**
	 * @return The title of the server this result is for
	 */
	String getTitle();
	
	/**
	 * @return The abstract of the server this result is for
	 */
	String getAbstract();
	
	/**
	 * @return The capabilities URL for this result 
	 */
	URL getCapabilitiesUrl();
	
	/**
	 * @return The publisher for this result
	 */
	String getPublisher();
}
