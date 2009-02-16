package au.gov.ga.worldwind.stereo;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;


import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.settings.Settings.StereoMode;
import au.gov.ga.worldwind.stereo.StereoOrbitView.Eye;

import com.sun.opengl.util.BufferUtil;

public class StereoSceneController extends AbstractSceneController
{
	private double lastVerticalExaggeration = -1;
	private boolean stereoTested = false;

	@Override
	protected void doRepaint(DrawContext dc)
	{
		Settings.Properties settings = Settings.get();
		double verticalExaggeration = settings.getVerticalExaggeration();
		if (lastVerticalExaggeration != verticalExaggeration)
		{
			setVerticalExaggeration(verticalExaggeration);
			lastVerticalExaggeration = verticalExaggeration;
		}

		GL gl = dc.getGL();
		if (!stereoTested)
		{
			ByteBuffer buffer16 = BufferUtil.newByteBuffer(16);
			gl.glGetBooleanv(GL.GL_STEREO, buffer16);
			Settings.setStereoSupported(buffer16.get() == 1);
			stereoTested = true;
		}

		this.initializeFrame(dc);
		try
		{
			this.applyView(dc);
			this.createTerrain(dc);
			this.clearFrame(dc);
			this.pick(dc);
			this.clearFrame(dc);

			View view = dc.getView();
			StereoOrbitView stereo = null;
			if (view instanceof StereoOrbitView)
			{
				stereo = (StereoOrbitView) view;
			}
			if (stereo == null || !settings.isStereoEnabled())
			{
				this.draw(dc);
			}
			else
			{
				StereoMode mode = settings.getStereoMode();
				boolean swap = settings.isStereoSwap();

				stereo.setDrawingStereo(true);
				stereo.setEye(swap ? Eye.RIGHT : Eye.LEFT);
				setupBuffer(gl, mode, Eye.LEFT);
				this.applyView(dc);
				this.draw(dc);

				gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
				gl.glDisable(GL.GL_FOG);

				stereo.setEye(swap ? Eye.LEFT : Eye.RIGHT);
				setupBuffer(gl, mode, Eye.RIGHT);
				this.applyView(dc);
				this.draw(dc);

				stereo.setDrawingStereo(false);
				restoreBuffer(gl, mode);
				view.apply(dc);
			}
		}
		finally
		{
			this.finalizeFrame(dc);
		}
	}

	@Override
	protected void draw(DrawContext dc)
	{
		try
		{
			super.draw(dc);
		}
		catch (NumberFormatException e)
		{
			//scale bar layer sometimes throws this exception: ignore it
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

	private ArrayList<Point> pickPoints = new ArrayList<Point>();

	protected void pickTerrain(DrawContext dc)
	{
		try
		{
			if (dc.isPickingMode() && dc.getVisibleSector() != null
					&& dc.getSurfaceGeometry() != null
					&& dc.getSurfaceGeometry().size() > 0)
			{
				this.pickPoints.clear();
				if (dc.getPickPoint() != null)
					this.pickPoints.add(dc.getPickPoint());

				// Clear viewportCenterPosition.
				dc.setViewportCenterPosition(null);
				Point vpc = dc.getViewportCenterScreenPoint();
				if (vpc != null)
					this.pickPoints.add(vpc);

				if (this.pickPoints.size() == 0)
					return;

				List<PickedObject> pickedObjects = dc.getSurfaceGeometry()
						.pick(dc, this.pickPoints);
				if (pickedObjects == null || pickedObjects.size() == 0)
					return;

				for (PickedObject po : pickedObjects)
				{
					if (po == null)
						continue;
					if (po.getPickPoint().equals(dc.getPickPoint()))
						dc.addPickedObject(po);
					else if (po.getPickPoint().equals(vpc))
						dc.setViewportCenterPosition((Position) po.getObject());
				}
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			//ignore (bug in nasa code)
		}
	}
}
