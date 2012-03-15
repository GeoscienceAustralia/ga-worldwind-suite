package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.URL;

/**
 * A {@link WmsCapabilitiesService} that is 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LocalResourceCapabilitiesService implements WmsCapabilitiesService
{

	@Override
	public WMSCapabilities retrieveCapabilities(URL url) throws Exception
	{
		if (url == null)
		{
			return null;
		}
		
		return new WMSCapabilities(url).parse();
	}

}
