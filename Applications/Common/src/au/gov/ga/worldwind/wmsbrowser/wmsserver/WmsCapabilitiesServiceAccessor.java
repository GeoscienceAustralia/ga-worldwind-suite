package au.gov.ga.worldwind.wmsbrowser.wmsserver;

/**
 * A static accessor class for the current WMS capabilities service
 * <p/>
 * For use where injecting a service is not appropriate.
 */
public class WmsCapabilitiesServiceAccessor
{
	private static WmsCapabilitiesService service = new DefaultCapabilitiesService();
	
	public static WmsCapabilitiesService getService()
	{
		return service;
	}
	
	public static void setService(WmsCapabilitiesService service)
	{
		if (service != null)
		{
			WmsCapabilitiesServiceAccessor.service = service;
		}
	}
}
