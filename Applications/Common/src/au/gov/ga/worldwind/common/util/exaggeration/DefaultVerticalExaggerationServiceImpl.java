package au.gov.ga.worldwind.common.util.exaggeration;

import gov.nasa.worldwind.render.DrawContext;

/**
 * The default implementation of the {@link VerticalExaggerationService} that simply uses the global
 * vertical exaggeration as provided by the supplied {@link DrawContext}.
 */
public class DefaultVerticalExaggerationServiceImpl implements VerticalExaggerationService
{

	@Override
	public double applyVerticalExaggeration(DrawContext dc, double elevation)
	{
		return dc.getVerticalExaggeration() * elevation;
	}

	@Override
	public double getGlobalVerticalExaggeration(DrawContext dc)
	{
		return dc.getVerticalExaggeration();
	}

}
