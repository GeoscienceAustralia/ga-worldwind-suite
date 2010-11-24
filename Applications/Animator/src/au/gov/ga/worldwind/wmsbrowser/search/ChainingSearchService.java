package au.gov.ga.worldwind.wmsbrowser.search;

import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * An implementation of the {@link WmsServerSearchService} that queries
 * delegate search services in order until one returns a result, at which point
 * the result is returned.
 */
public class ChainingSearchService extends DelegatingSearchService implements WmsServerSearchService
{
	@Override
	public List<WmsServer> searchForServers(String searchString)
	{
		try
		{
			lockServices();
			
			for (WmsServerSearchService service : getServices())
			{
				List<WmsServer> servers = service.searchForServers(searchString);
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
