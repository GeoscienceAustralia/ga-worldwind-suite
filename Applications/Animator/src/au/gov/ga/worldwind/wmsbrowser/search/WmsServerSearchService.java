package au.gov.ga.worldwind.wmsbrowser.search;

import java.util.List;

/**
 * An interface for services that can search for a WMS server using a provided search string
 */
public interface WmsServerSearchService
{
	
	/**
	 * Search for WMS servers using the provided search string
	 * 
	 * @return The list of WMS servers that match the search string. If no results, returns an empty
	 * string.
	 */
	List<WmsServerSearchResult> searchForServers(String searchString);
	
}
