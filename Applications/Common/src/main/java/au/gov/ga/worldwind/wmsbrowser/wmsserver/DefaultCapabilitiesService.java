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
package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.wms.CapabilitiesRequest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The default {@link WmsCapabilitiesService} implementation.
 * <p/>
 * Uses the {@link WMSCapabilities#retrieve(java.net.URI)} method to 
 * perform capabilities retrieval.
 */
public final class DefaultCapabilitiesService implements WmsCapabilitiesService
{
	@Override
	public WMSCapabilities retrieveCapabilities(URL url) throws Exception
	{
		if (url == null)
		{
			return null;
		}
		
		// TODO: Implement own capabilities loading mechanism to avoid hard-coding the version
		WMSCapabilities result = null;
		try
		{
			CapabilitiesRequest request = new CapabilitiesRequest(url.toURI());
			
			// Try v1.3.0 first, then v1.1.1
			request.setVersion("1.3.0");
			result = new WMSCapabilities(request).parse();
			
			if (result == null)
			{
				request.setVersion("1.1.1");
				result = new WMSCapabilities(request).parse();
			}
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		return result;
	}
}
