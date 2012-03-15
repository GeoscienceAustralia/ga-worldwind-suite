/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicSurfaceTileRendererAccessible;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLUtil;

import java.util.Iterator;
import java.util.logging.Level;

import javax.media.opengl.GL;

/**
 * {@link GeographicSurfaceTileRenderer} extension that supports multitexturing
 * per texture tile.
 * 
 * @see MultiTextureTile
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MultiTextureSurfaceTileRenderer extends GeographicSurfaceTileRendererAccessible
{
	private static final int DEFAULT_ALPHA_TEXTURE_SIZE = 1024; //private in super-class, so brought down

	@Override
	public void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> tiles)
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
		int totalNumTexUnits = dc.getGLRuntimeCapabilities().getNumTextureUnits();
		boolean showOutlines = this.isShowImageTileOutlines() && totalNumTexUnits > 2;

		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT // for alpha func
				| GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_DEPTH_BUFFER_BIT // for depth func
				| GL.GL_TRANSFORM_BIT);

		boolean texturesEnabled = false;
		int numTexUnitsUsed = showOutlines ? 3 : 2;
		int remainingTexUnits = totalNumTexUnits - numTexUnitsUsed;
		try
		{
			this.alphaTexture = dc.getTextureCache().getTexture(this);
			if (this.alphaTexture == null)
			{
				this.initAlphaTexture(DEFAULT_ALPHA_TEXTURE_SIZE); // TODO: choose size to match incoming tile sizes?
				dc.getTextureCache().put(this, this.alphaTexture);
			}

			if (showOutlines && this.outlineTexture == null)
				this.initOutlineTexture(128);

			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDepthFunc(GL.GL_LEQUAL);

			gl.glEnable(GL.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL.GL_GREATER, 0.01f);

			dc.getSurfaceGeometry().beginRendering(dc);

			// For each current geometry tile, find the intersecting image tiles and render the geometry
			// tile once for each intersecting image tile.
			Transform transform = new Transform();
			for (SectorGeometry sg : dc.getSurfaceGeometry())
			{
				Iterable<SurfaceTile> tilesToRender = this.getIntersectingTiles(dc, sg, tiles);
				if (tilesToRender == null)
					continue;

				Iterator<SurfaceTile> iterator = tilesToRender.iterator();
				if (!iterator.hasNext())
					continue;

				if (!texturesEnabled)
				{
					SurfaceTile first = iterator.next();
					int extraTextureCount =
							first instanceof MultiTextureTile ? ((MultiTextureTile) first).extraTextureCount() : 0;
					extraTextureCount = Math.min(extraTextureCount, remainingTexUnits);
					numTexUnitsUsed += extraTextureCount;

					for (int i = 0; i < numTexUnitsUsed; i++)
					{
						gl.glActiveTexture(GL.GL_TEXTURE0 + i);
						gl.glEnable(GL.GL_TEXTURE_2D);
						gl.glMatrixMode(GL.GL_TEXTURE);
						gl.glPushMatrix();
						if (showOutlines && i == numTexUnitsUsed - 2)
						{
							//outline texture
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_ADD);
						}
						else if (i == numTexUnitsUsed - 1 || !dc.isPickingMode())
						{
							//alpha texture or not picking mode
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
						}
						else
						{
							//picking mode (but not the alpha texture)
							gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
							gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
							gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);
						}
					}

					texturesEnabled = true;
				}

				try
				{
					sg.beginRendering(dc, numTexUnitsUsed);

					// Pre-load info to compute the texture transform
					this.preComputeTextureTransform(dc, sg, transform);

					// For each intersecting tile, establish the texture transform necessary to map the image tile
					// into the geometry tile's texture space. Use an alpha texture as a mask to prevent changing the
					// frame buffer where the image tile does not overlap the geometry tile. Render both the image and
					// alpha textures via multi-texture rendering.
					// TODO: Figure out how to apply multi-texture to more than one tile at a time. Use fragment shader?
					for (SurfaceTile tile : tilesToRender)
					{
						gl.glActiveTexture(GL.GL_TEXTURE0);

						boolean bound;
						if (tile instanceof MultiTextureTile)
							bound = ((MultiTextureTile) tile).bind(dc, GL.GL_TEXTURE1, remainingTexUnits);
						else
							bound = tile.bind(dc);

						if (bound)
						{
							// Determine and apply texture transform to map image tile into geometry tile's texture space
							this.computeTextureTransform(dc, tile, transform);

							for (int i = 0; i < numTexUnitsUsed; i++)
							{
								gl.glActiveTexture(GL.GL_TEXTURE0 + i);
								gl.glMatrixMode(GL.GL_TEXTURE);
								gl.glLoadIdentity();

								if (showOutlines && i == numTexUnitsUsed - 2)
								{
									this.outlineTexture.bind();
								}
								else if (i == numTexUnitsUsed - 1)
								{
									this.alphaTexture.bind();
								}
								else
								{
									tile.applyInternalTransform(dc, true);
								}

								gl.glScaled(transform.getHScale(), transform.getVScale(), 1d);
								gl.glTranslated(transform.getHShift(), transform.getVShift(), 0d);
							}

							// Render the geometry tile
							sg.renderMultiTexture(dc, numTexUnitsUsed);
						}
					}
				}
				finally
				{
					sg.endRendering(dc);
				}
			}
		}
		catch (Exception e)
		{
			Logging.logger().log(Level.SEVERE,
					Logging.getMessage("generic.ExceptionWhileRenderingLayer", this.getClass().getName()), e);
		}
		finally
		{
			dc.getSurfaceGeometry().endRendering(dc);

			for (int i = numTexUnitsUsed - 1; i >= 0; i--)
			{
				gl.glActiveTexture(GL.GL_TEXTURE0 + i);
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPopMatrix();
				gl.glDisable(GL.GL_TEXTURE_2D);
			}

			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
			if (dc.isPickingMode())
			{
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
			}

			gl.glPopAttrib();
		}
	}
}
