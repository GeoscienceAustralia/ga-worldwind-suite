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
