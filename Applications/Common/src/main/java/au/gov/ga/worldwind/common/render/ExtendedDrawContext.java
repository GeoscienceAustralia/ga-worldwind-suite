package au.gov.ga.worldwind.common.render;

import javax.media.opengl.GL;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.DrawContextImpl;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.terrain.SectorGeometryList;

public class ExtendedDrawContext extends DrawContextImpl
{
	protected boolean wireframe = false;
	protected SectorGeometryList flatSurfaceGeometry;
	protected SectorGeometryList oldSurfaceGeomtry;

	public ExtendedDrawContext()
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
		return dc instanceof ExtendedDrawContext && ((ExtendedDrawContext) dc).isWireframe();
	}

	public static void applyWireframePolygonMode(DrawContext dc)
	{
		dc.getGL().glPolygonMode(GL.GL_FRONT_AND_BACK, isWireframe(dc) ? GL.GL_LINE : GL.GL_FILL);
	}

	@Override
	public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
	{
		super.setGLRuntimeCapabilities(capabilities);
		
		//it would be better to override the initialize method, but unfortunately (for some unknown reason) it's final
		
		if (this.flatSurfaceGeometry != null)
			this.flatSurfaceGeometry.clear();
		this.flatSurfaceGeometry = null;
	}

	public SectorGeometryList getFlatSurfaceGeometry()
	{
		return flatSurfaceGeometry;
	}

	public void setFlatSurfaceGeometry(SectorGeometryList flatSectorGeometry)
	{
		this.flatSurfaceGeometry = flatSectorGeometry;
	}
	
	public void switchToFlatSurfaceGeometry()
	{
		oldSurfaceGeomtry = getSurfaceGeometry();
		setSurfaceGeometry(flatSurfaceGeometry);
	}
	
	public void switchToStandardSurfaceGeometry()
	{
		setSurfaceGeometry(oldSurfaceGeomtry);
	}
}
