package au.gov.ga.worldwind.common.util.exaggeration;

import gov.nasa.worldwind.render.DrawContext;

/**
 * A static accessor used to apply the correct vertical exaggeration values to
 * a given elevation value
 */
public class VerticalExaggerationAccessor
{
	private static VerticalExaggerationService service = new DefaultVerticalExaggerationServiceImpl();
	
	public static VerticalExaggerationService getService()
	{
		if (VerticalExaggerationAccessor.service == null)
		{
			service = new DefaultVerticalExaggerationServiceImpl();
		}
		return service;
	}
	
	public static void setService(VerticalExaggerationService service)
	{
		VerticalExaggerationAccessor.service = service;
	}
	
	/** @see VerticalExaggerationService#applyVerticalExaggeration(DrawContext, double) */
	public static double applyVerticalExaggeration(DrawContext dc, double elevation)
	{
		return getService().applyVerticalExaggeration(dc, elevation);
	}
	
	/** @see VerticalExaggerationService#getGlobalVerticalExaggeration(DrawContext) */
	public static double getGlobalVerticalExaggeration(DrawContext dc)
	{
		return getService().getGlobalVerticalExaggeration(dc);
	}
	
	private VerticalExaggerationAccessor(){}
}
