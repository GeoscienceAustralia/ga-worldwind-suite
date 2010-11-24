package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

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
		
		WMSCapabilities result = WMSCapabilities.retrieve(url.toURI());
		if (result != null)
		{
			// Ensure the result has been parsed
			result.parse();
		}
		return result;
	}
}