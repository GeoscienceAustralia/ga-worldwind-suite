package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import gov.nasa.worldwind.terrain.RectangularTessellatorAccessible;

/**
 * An extension of the {@link RectangularTessellator} that ignores the cache and
 * regenerates vertices every repaint, instead of cacheing and regenerating
 * every second.
 */
public class ImmediateRectangularTesselator extends RectangularTessellatorAccessible
{
	@Override
	protected void makeVerts(DrawContext dc, RectTile tile)
	{
		RenderInfo ri = this.buildVerts(dc, tile, isMakeTileSkirts());
		setRenderInfo(tile, ri);
	}
}
