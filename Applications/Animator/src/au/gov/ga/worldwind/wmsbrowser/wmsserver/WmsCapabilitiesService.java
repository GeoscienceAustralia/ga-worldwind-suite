package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.URL;

/**
 * A service interface to services that can retrieve WMS capabilities from a URL.
 */
public interface WmsCapabilitiesService
{
	/**
	 * @return the WMS Capabilites for the provided URL, or <code>null</code> if the URL
	 * is not a valid WMS capabilities URL 
	 */
	WMSCapabilities retrieveCapabilities(URL url) throws Exception;
}