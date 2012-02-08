package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import au.gov.ga.worldwind.common.terrain.WireframeRectangularTessellator;

/**
 * Custom {@link RectangularTessellator} that generates flat sector geometry (by
 * forcing vertical exaggeration to 0).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlatRectangularTessellator extends WireframeRectangularTessellator
{
	@Override
	public SectorGeometryList tessellate(DrawContext dc)
	{
		double oldExaggeration = dc.getVerticalExaggeration();
		Integer oldMaxLevel = Configuration.getIntegerValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL);
		try
		{
			dc.setVerticalExaggeration(0);
			Configuration.setValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, 4);
			return super.tessellate(dc);
		}
		finally
		{
			dc.setVerticalExaggeration(oldExaggeration);
			Configuration.setValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, oldMaxLevel);
		}
	}

	@Override
	protected CacheKey createCacheKey(DrawContext dc, RectTile tile)
	{
		//dodgy way to make key different from super-class': make the density negative
		return new CacheKey(dc, tile.getSector(), -tile.getDensity());
	}
}
