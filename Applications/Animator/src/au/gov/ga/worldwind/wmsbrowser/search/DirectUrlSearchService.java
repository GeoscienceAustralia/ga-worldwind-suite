package au.gov.ga.worldwind.wmsbrowser.search;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public List<WmsServer> searchForServers(String searchString)
	{
		if (Util.isBlank(searchString))
		{
			return Collections.emptyList();
		}
		
		// Check the search string is a URL
		URL searchUrl = null;
		try
		{
			searchUrl = new URL(searchString.trim());
		}
		catch (MalformedURLException e)
		{
			// Not a URL..
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
		
		List<WmsServer> result = new ArrayList<WmsServer>(1);
		result.add(server);
		return result;
	}

}
