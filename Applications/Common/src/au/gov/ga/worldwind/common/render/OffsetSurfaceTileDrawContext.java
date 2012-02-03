package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.render.DrawContextImpl;

public class OffsetSurfaceTileDrawContext extends DrawContextImpl
{
	public OffsetSurfaceTileDrawContext()
	{
		geographicSurfaceTileRenderer.dispose();
		geographicSurfaceTileRenderer = new OffsetSurfaceTileRenderer();
	}

	@Override
	public OffsetSurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{
		return (OffsetSurfaceTileRenderer) super.getGeographicSurfaceTileRenderer();
	}
}
