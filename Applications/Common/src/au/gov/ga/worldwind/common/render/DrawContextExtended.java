package au.gov.ga.worldwind.common.render;

import javax.media.opengl.GL;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.DrawContextImpl;

public class DrawContextExtended extends DrawContextImpl
{
	protected boolean wireframe = false;

	public DrawContextExtended()
	{
		geographicSurfaceTileRenderer.dispose();
		geographicSurfaceTileRenderer = new OffsetSurfaceTileRenderer();
	}

	@Override
	public OffsetSurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{
		return (OffsetSurfaceTileRenderer) super.getGeographicSurfaceTileRenderer();
	}

	public boolean isWireframe()
	{
		return wireframe;
	}

	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
	}

	public static boolean isWireframe(DrawContext dc)
	{
		return dc instanceof DrawContextExtended && ((DrawContextExtended) dc).isWireframe();
	}

	public static void applyWireframePolygonMode(DrawContext dc)
	{
		dc.getGL().glPolygonMode(GL.GL_FRONT_AND_BACK, isWireframe(dc) ? GL.GL_LINE : GL.GL_FILL);
	}
}
