/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
