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
package au.gov.ga.worldwind.viewer.stereo;

import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.render.ExtendedDrawContext;
import au.gov.ga.worldwind.common.render.ExtendedSceneController;
import au.gov.ga.worldwind.common.util.SectorClipPlanes;
import au.gov.ga.worldwind.common.view.stereo.StereoView;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.settings.Settings.StereoMode;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link SceneController} implementation that supports stereo rendering. When
 * stereo is enabled, the scene is drawn twice, once for the left eye and once
 * for the right eye.
 * <p>
 * Supports hardware quad-buffered stereo, and also a number of anaglyph modes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoSceneController extends ExtendedSceneController
{
	private double lastVerticalExaggeration = -1;
	private double lastFieldOfView = -1;
	private boolean stereoTested = false;
	private SectorClipPlanes sectorClipping = new SectorClipPlanes();

	public void clipSector(Sector sector)
	{
		sectorClipping.clipSector(sector);
	}

	public void clearClipping()
	{
		sectorClipping.clear();
	}

	@Override
	protected void doRepaint(DrawContext dc)
	{
		Settings settings = Settings.get();
		double verticalExaggeration = settings.getVerticalExaggeration();
		if (lastVerticalExaggeration != verticalExaggeration)
		{
			setVerticalExaggeration(verticalExaggeration);
			lastVerticalExaggeration = verticalExaggeration;
		}

		double fieldOfView = settings.getFieldOfView();
		if (lastFieldOfView != fieldOfView)
		{
			dc.getView().setFieldOfView(Angle.fromDegrees(fieldOfView));
			lastFieldOfView = fieldOfView;
		}

		GL gl = dc.getGL();
		if (!stereoTested)
		{
			ByteBuffer buffer16 = BufferUtil.newByteBuffer(16);
			gl.glGetBooleanv(GL.GL_STEREO, buffer16);
			Settings.setStereoSupported(buffer16.get() == 1);
			stereoTested = true;
		}

		View view = dc.getView();
		StereoView stereo = null;
		if (view instanceof StereoView)
		{
			stereo = (StereoView) view;
			stereo.setParameters(settings);
		}

		this.initializeFrame(dc);
		try
		{
			this.applyView(dc);
			dc.addPickPointFrustum();
			this.createTerrain(dc);
			this.preRender(dc);
			this.clearFrame(dc);
			this.pick(dc);
			this.clearFrame(dc);
			ExtendedDrawContext.applyWireframePolygonMode(dc);

			try
			{
				sectorClipping.enableClipping(dc);

				if (stereo == null || !settings.isStereoEnabled())
				{
					this.draw(dc);
				}
				else
				{
					StereoMode mode = settings.getStereoMode();
					boolean swap = settings.isStereoSwap();

					stereo.setup(true, swap ? Eye.RIGHT : Eye.LEFT);
					setupBuffer(gl, mode, Eye.LEFT);
					this.applyView(dc);
					this.draw(dc);

					gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

					stereo.setup(true, swap ? Eye.LEFT : Eye.RIGHT);
					setupBuffer(gl, mode, Eye.RIGHT);
					this.applyView(dc);
					this.draw(dc);

					stereo.setup(false, Eye.LEFT);
					restoreBuffer(gl, mode);
					this.applyView(dc);
				}
			}
			finally
			{
				sectorClipping.disableClipping(dc);
			}
		}
		finally
		{
			this.finalizeFrame(dc);
		}
	}

	private void setupBuffer(GL gl, StereoMode mode, Eye eye)
	{
		boolean left = eye == Eye.LEFT;
		switch (mode)
		{
		case RC_ANAGLYPH:
			gl.glColorMask(left, !left, !left, true);
			break;
		case GM_ANAGLYPH:
			gl.glColorMask(!left, left, !left, true);
			break;
		case BY_ANAGLYPH:
			gl.glColorMask(!left, !left, left, true);
			break;
		case STEREO_BUFFER:
			gl.glDrawBuffer(left ? GL.GL_BACK_LEFT : GL.GL_BACK_RIGHT);
			break;
		}
	}

	private void restoreBuffer(GL gl, StereoMode mode)
	{
		switch (mode)
		{
		case BY_ANAGLYPH:
		case GM_ANAGLYPH:
		case RC_ANAGLYPH:
			gl.glColorMask(true, true, true, true);
			break;
		case STEREO_BUFFER:
			gl.glDrawBuffer(GL.GL_BACK);
			break;
		}
	}
}
