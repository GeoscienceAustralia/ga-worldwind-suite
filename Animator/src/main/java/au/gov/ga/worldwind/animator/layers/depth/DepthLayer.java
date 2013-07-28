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
package au.gov.ga.worldwind.animator.layers.depth;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;

import java.util.List;

import javax.media.opengl.GL2;

/**
 * A layer that draws the elevation tiles with the OpenGL color mask disabled,
 * so that it only writes to the depth buffer. This is useful for ensuring that
 * that back of transparent layers (such as roads) aren't visible when no other
 * layers are enabled, because the depth testing is still performed.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthLayer extends AbstractLayer
{
	private SurfaceTile tile = new DepthTile();

	public DepthLayer()
	{
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glPushAttrib(GL2.GL_POLYGON_BIT);

		try
		{
			gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glCullFace(GL2.GL_BACK);
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
