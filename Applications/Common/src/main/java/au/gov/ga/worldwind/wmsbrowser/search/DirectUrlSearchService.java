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

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.DefaultCapabilitiesService;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsCapabilitiesService;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifierImpl;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerImpl;

/**
 * A search service that attempts to use the provided search string directly as a
 * WMS server capabilities URL.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DirectUrlSearchService implements WmsServerSearchService
{
	private WmsCapabilitiesService capabilitiesService = new DefaultCapabilitiesService();

	/**
	 * Set the {@link WmsCapabilitiesService} to use for retrieving capabilities from a URL
	 */
	public void setCapabilitiesService(WmsCapabilitiesService capabilitiesService)
	{
		if (capabilitiesService == null)
		{
			capabilitiesService = new DefaultCapabilitiesService();
		}
		this.capabilitiesService = capabilitiesService;
	}
	
	@Override
	public List<WmsServerSearchResult> searchForServers(String searchString)
	{
		if (Util.isBlank(searchString))
		{
			return Collections.emptyList();
		}
		
		// Check the search string is a URL
		URL searchUrl = URLUtil.fromString(searchString, "http");
		if (searchUrl == null)
		{
			return Collections.emptyList();
		}
		
		// Attempt to connect to the URL as a WMS capabilities URL
		WMSCapabilities serverCaps;
		try
		{
			serverCaps = capabilitiesService.retrieveCapabilities(searchUrl);
		}
		catch (Exception e)
		{
			// Not a capabilities URL...
			return Collections.emptyList();
		}
		if (serverCaps == null)
		{
			// Not a capabilities URL...
			return Collections.emptyList();
		}
		
		WmsServerIdentifier identifier = new WmsServerIdentifierImpl(serverCaps.getServiceInformation().getServiceTitle(), searchUrl);
		WmsServer server = new WmsServerImpl(identifier, serverCaps);
		
		List<WmsServerSearchResult> result = new ArrayList<WmsServerSearchResult>(1);
		result.add(new WmsServerSearchResultImpl(server));
		return result;
	}

}
