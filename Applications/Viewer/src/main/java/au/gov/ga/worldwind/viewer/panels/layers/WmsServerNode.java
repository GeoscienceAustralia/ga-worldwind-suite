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
package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.ogc.OGCCapabilities;
import gov.nasa.worldwind.ogc.OGCRequestDescription;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.net.URL;
import java.util.Set;

import au.gov.ga.worldwind.common.util.Icons;

/**
 * A {@link FolderNode} that represents a WMS Server.
 * <p/>
 * Contains helper methods for matching {@link WmsLayerNode}s to
 * their correct {@link WmsServerNode}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsServerNode extends FolderNode
{
	private String serverCapabilitiesUrl; 
	
	public WmsServerNode(OGCCapabilities caps)
	{
		super(caps.getServiceInformation().getServiceTitle(), null, Icons.folder.getURL(), true);
		
		this.serverCapabilitiesUrl = getCapabilitiesUrlForCaps(caps);
	}

	public WmsServerNode(String name, URL iconURL, boolean expanded, URL capabilitiesURL)
	{
		super(name, null, iconURL, expanded);
		this.serverCapabilitiesUrl = capabilitiesURL.toExternalForm();
	}
	
	/**
	 * @return Whether this server node represents the WMS server that is 
	 * the origin of the provided WMS layer
	 */
	public boolean isOriginOf(WMSLayerInfo wmsInfo)
	{
		if (wmsInfo == null)
		{
			return false;
		}
		
		OGCCapabilities wmsLayerCaps = wmsInfo.getCaps();
		return serverCapabilitiesUrl.equalsIgnoreCase(getCapabilitiesUrlForCaps(wmsLayerCaps));
	}

	public String getServerCapabilitiesUrl()
	{
		return serverCapabilitiesUrl;
	}
	
	private String getCapabilitiesUrlForCaps(OGCCapabilities caps)
	{
		Set<OGCRequestDescription> requestDescriptions = caps.getCapabilityInformation().getRequestDescriptions();
        for (OGCRequestDescription rd : requestDescriptions)
        {
            if (rd.getRequestName().equals("GetCapabilities"))
            {
            	return rd.getOnlineResouce("HTTP", "Get").getHref();
            }
        }
        return null;
	}
}
