package layers.depth;

import java.util.List;

import javax.media.opengl.GL;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;

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
		try
		{
			gl.glPushAttrib(GL.GL_POLYGON_BIT);

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
		public void applyInternalTransform(DrawContext dc)
		{
		}

		public boolean bind(DrawContext dc)
		{
			return true;
		}

		public Extent getExtent(DrawContext dc)
		{
			return null;
		}

		public Sector getSector()
		{
			return Sector.FULL_SPHERE;
		}

		public List<? extends LatLon> getCorners()
		{
			return null;
		}
	}
}
