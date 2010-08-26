package au.gov.ga.worldwind.viewer.terrain;

import javax.media.opengl.GL;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import nasa.worldwind.terrain.RectangularTessellator;

public class WireframeRectangularTessellator extends RectangularTessellator
{
	private boolean wireframeDepthTesting = true;

	public boolean isWireframeDepthTesting()
	{
		return wireframeDepthTesting;
	}

	public void setWireframeDepthTesting(boolean wireframeDepthTesting)
	{
		this.wireframeDepthTesting = wireframeDepthTesting;
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

		if (tile.ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

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

				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, tile.ri.vertices.rewind());
				gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, tile.ri.indices.limit(),
						javax.media.opengl.GL.GL_UNSIGNED_INT, tile.ri.indices.rewind());
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
}
