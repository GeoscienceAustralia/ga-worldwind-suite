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

import gov.nasa.worldwind.applications.gos.Record;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * Default implementation of the {@link WmsServerSearchResult} interface.
 * <p/>
 * Attempts to use the provided {@link WmsServer} to retrieve metadata, if the
 * capabilities have been loaded. Otherwise, uses provided values.
 */
public class WmsServerSearchResultImpl implements WmsServerSearchResult
{
	private WmsServer server;
	private String serviceAbstract;
	private String publisher; 
	
	public WmsServerSearchResultImpl(WmsServer server)
	{
		Validate.notNull(server, "A server is required");
		this.server = server;
	}
	
	public WmsServerSearchResultImpl(WmsServer server, Record cswRecord)
	{
		this(server);
		
		if (cswRecord != null)
		{
			serviceAbstract = cswRecord.getAbstract();
		}
	}
	
	@Override
	public WmsServer getWmsServer()
	{
		return server;
	}

	@Override
	public String getTitle()
	{
		return server.getName();
	}

	@Override
	public String getAbstract()
	{
		if (server.isCapabilitiesLoaded())
		{
			return server.getCapabilities().getServiceInformation().getServiceAbstract();
		}
		return serviceAbstract;
	}

	@Override
	public URL getCapabilitiesUrl()
	{
		return server.getCapabilitiesUrl();
	}

	@Override
	public String getPublisher()
	{
		if (server.isCapabilitiesLoaded())
		{
			return server.getCapabilities().getServiceInformation().getContactInformation().getOrganization();
		}
		return publisher;
	}

	public void setServiceAbstract(String serviceAbstract)
	{
		this.serviceAbstract = serviceAbstract;
	}
	
	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}
}
