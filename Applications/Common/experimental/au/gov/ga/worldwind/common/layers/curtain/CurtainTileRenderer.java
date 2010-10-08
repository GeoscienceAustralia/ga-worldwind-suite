package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.logging.Level;

import javax.media.opengl.GL;

public class CurtainTileRenderer
{
	public void renderTile(DrawContext dc, CurtainTextureTile tile, Path path, double top,
			double bottom, int subsegments, boolean followTerrain)
	{
		if (tile == null)
		{
			String message = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		ArrayList<CurtainTextureTile> al = new ArrayList<CurtainTextureTile>(1);
		al.add(tile);
		this.renderTiles(dc, al, path, top, bottom, subsegments, followTerrain);
		al.clear();
	}

	public void renderTiles(DrawContext dc, Iterable<? extends CurtainTextureTile> tiles,
			Path path, double top, double bottom, int subsegments, boolean followTerrain)
	{
		if (tiles == null)
		{
			String message = Logging.getMessage("nullValue.TileIterableIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		GL gl = dc.getGL();

		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT // for alpha func
				| GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT // for depth func
				| GL.GL_TEXTURE_BIT // for texture env
				| GL.GL_TRANSFORM_BIT);

		try
		{
			gl.glDisable(GL.GL_CULL_FACE);

			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDepthFunc(GL.GL_LEQUAL);

			gl.glEnable(GL.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL.GL_GREATER, 0.01f);

			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPushMatrix();
			if (!dc.isPickingMode())
			{
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
			}
			else
			{
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);
			}

			for (CurtainTextureTile tile : tiles)
			{
				if (tile.bind(dc))
				{
					gl.glMatrixMode(GL.GL_TEXTURE);
					gl.glLoadIdentity();
					tile.applyInternalTransform(dc);

					SegmentGeometry geometry =
							path.getGeometry(dc, tile.getSegment(), top, bottom, subsegments,
									followTerrain);
					geometry.render(dc, 1);
				}
			}

			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPopMatrix();
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		catch (Exception e)
		{
			Logging.logger().log(
					Level.SEVERE,
					Logging.getMessage("generic.ExceptionWhileRenderingLayer", this.getClass()
							.getName()), e);
		}
		finally
		{
			// TODO: pop matrix stack too, for all texture units
			gl.glPopAttrib();
		}
	}
}
