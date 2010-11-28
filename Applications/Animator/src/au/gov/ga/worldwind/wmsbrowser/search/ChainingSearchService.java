package au.gov.ga.worldwind.wmsbrowser.search;

import java.util.Collections;
import java.util.List;

/**
 * An implementation of the {@link WmsServerSearchService} that queries
 * delegate search services in order until one returns a result, at which point
 * the result is returned.
 */
public class ChainingSearchService extends DelegatingSearchService implements WmsServerSearchService
{
	@Override
	public List<WmsServerSearchResult> searchForServers(String searchString)
	{
		try
		{
			lockServices();
			
			for (WmsServerSearchService service : getServices())
			{
				List<WmsServerSearchResult> servers = service.searchForServers(searchString);
				if (!servers.isEmpty())
				{
					return servers;
				}
			}
			
			return Collections.emptyList();
		}
		finally
		{
			unlockServices();
		}
	}
}
