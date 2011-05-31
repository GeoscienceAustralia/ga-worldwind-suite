package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.ogc.OGCCapabilities;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Icons;

public class WmsServerNode extends FolderNode
{
	private String serverCapabilitiesUrl; 
	
	public WmsServerNode(OGCCapabilities caps)
	{
		super(caps.getServiceInformation().getServiceTitle(), null, Icons.folder.getURL(), true);
		this.serverCapabilitiesUrl = caps.getServiceInformation().getOnlineResource().getHref();
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
		return serverCapabilitiesUrl.equalsIgnoreCase(wmsLayerCaps.getServiceInformation().getOnlineResource().getHref());
	}

	public String getServerCapabilitiesUrl()
	{
		return serverCapabilitiesUrl;
	}
}
