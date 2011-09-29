package au.gov.ga.worldwind.animator.layers.depth;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;

import java.util.List;

import javax.media.opengl.GL;

public class DepthLayer extends AbstractLayer
{
	private SurfaceTile tile = new DepthTile();

	public DepthLayer()
	{
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_POLYGON_BIT);
		
		try
		{
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);
			gl.glColorMask(false, false, false, false);
			dc.getGeographicSurfaceTileRenderer().renderTile(dc, tile);
			gl.glColorMask(true, true, true, true);
		}
		finally
		{
			gl.glPopAttrib();
		}
	}

	private class DepthTile implements SurfaceTile
	{
		@Override
		public void applyInternalTransform(DrawContext dc, boolean textureIdAvailable)
		{
		}

		@Override
		public boolean bind(DrawContext dc)
		{
			return true;
		}

		@Override
		public Extent getExtent(DrawContext dc)
		{
			return null;
		}

		@Override
		public Sector getSector()
		{
			return Sector.FULL_SPHERE;
		}

		@Override
		public List<? extends LatLon> getCorners()
		{
			return null;
		}
	}
}
