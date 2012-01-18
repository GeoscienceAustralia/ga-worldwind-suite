package au.gov.ga.worldwind.animator.terrain.exaggeration;

import gov.nasa.worldwind.render.DrawContext;
import au.gov.ga.worldwind.common.util.exaggeration.VerticalExaggerationService;

/**
 * An implementation of the {@link VerticalExaggerationService} that uses the {@link VerticalExaggerationElevationModel} to
 * calculate the exaggeration to be applied to a given elevation value
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorVerticalExaggerationServiceImpl implements VerticalExaggerationService
{

	@Override
	public double applyVerticalExaggeration(DrawContext dc, double elevation)
	{
		return ((VerticalExaggerationElevationModel)dc.getGlobe().getElevationModel()).exaggerateElevation(elevation);
	}

	@Override
	public double getGlobalVerticalExaggeration(DrawContext dc)
	{
		return 1.0;
	}

}
