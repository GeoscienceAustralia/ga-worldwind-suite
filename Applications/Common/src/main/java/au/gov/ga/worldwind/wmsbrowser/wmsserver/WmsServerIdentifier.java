package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import java.net.URL;

/**
 * Holds the identification details of a WMS server, including its name
 * and capabilities URL.
 */
public interface WmsServerIdentifier
{
	String getName();
	
	URL getCapabilitiesUrl();
}
