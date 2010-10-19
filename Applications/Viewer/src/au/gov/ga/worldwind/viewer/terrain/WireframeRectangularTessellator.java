package au.gov.ga.worldwind.viewer.terrain;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellatorAccessible;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

public class WireframeRectangularTessellator extends RectangularTessellatorAccessible
{
	private boolean wireframeDepthTesting = true;
	private boolean backfaceCulling = false;

	public boolean isWireframeDepthTesting()
	{
		return wireframeDepthTesting;
	}

	public void setWireframeDepthTesting(boolean wireframeDepthTesting)
	{
		this.wireframeDepthTesting = wireframeDepthTesting;
	}

	public boolean isBackfaceCulling()
	{
		return backfaceCulling;
	}

	public void setBackfaceCulling(boolean backfaceCulling)
	{
		this.backfaceCulling = backfaceCulling;
	}

	@Override
	protected void renderWireframe(DrawContext dc, RectTile tile, boolean showTriangles,
			boolean showTileBoundary)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		RenderInfo ri = getRenderInfo(tile);

		if (ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		dc.getView().pushReferenceCenter(dc, getReferenceCenter(ri));

		javax.media.opengl.GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT
				| GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
		//gl.glEnable(GL.GL_BLEND);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		//gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		gl.glEnable(javax.media.opengl.GL.GL_CULL_FACE);
		gl.glCullFace(javax.media.opengl.GL.GL_BACK);
		gl.glDisable(javax.media.opengl.GL.GL_TEXTURE_2D);
		gl.glColor4d(0.6, 0.8, 0.8, 1.0);
		gl.glPolygonMode(javax.media.opengl.GL.GL_FRONT, javax.media.opengl.GL.GL_LINE);

		if (isWireframeDepthTesting())
		{
			gl.glEnable(GL.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(-1, 1);
		}
		else
		{
			gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		}

		if (showTriangles)
		{
			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);

				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

				DoubleBuffer vertices = getVertices(ri);
				IntBuffer indices = getIndices(ri);
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, vertices.rewind());
				gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, indices.limit(),
						javax.media.opengl.GL.GL_UNSIGNED_INT, indices.rewind());
			}
			finally
			{
				ogsh.pop(gl);
			}
		}

		dc.getView().popReferenceCenter(dc);

		gl.glPopAttrib();

		if (showTileBoundary)
			this.renderPatchBoundary(dc, tile);
	}

	@Override
	protected long render(DrawContext dc, RectTile tile, int numTextureUnits)
	{
		if (!backfaceCulling)
		{
			dc.getGL().glDisable(GL.GL_CULL_FACE);
		}
		return super.render(dc, tile, numTextureUnits);
	}
}
