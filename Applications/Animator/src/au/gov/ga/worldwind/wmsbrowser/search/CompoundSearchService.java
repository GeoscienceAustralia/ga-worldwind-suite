package au.gov.ga.worldwind.wmsbrowser.search;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link WmsServerSearchService} interface
 * that queries each search service in turn and accumulates the results
 * into a single returned list of servers.
 */
public class CompoundSearchService extends DelegatingSearchService implements WmsServerSearchService
{

	@Override
	public List<WmsServerSearchResult> searchForServers(String searchString)
	{
		try
		{
			lockServices();
			
			Set<WmsServerSearchResult> result = new LinkedHashSet<WmsServerSearchResult>(); // Maintain insertion order
			for (WmsServerSearchService service : getServices())
			{
				List<WmsServerSearchResult> servers = service.searchForServers(searchString);
				result.addAll(servers);
			}
			
			return new ArrayList<WmsServerSearchResult>(result);
		}
		finally
		{
			unlockServices();
		}
	}

}
