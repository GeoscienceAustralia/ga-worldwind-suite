package au.gov.ga.worldwind.viewer.stereo;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.stereo.StereoView;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.settings.Settings.StereoMode;

import com.sun.opengl.util.BufferUtil;

public class StereoSceneController extends AbstractSceneController
{
	private double lastVerticalExaggeration = -1;
	private double lastFieldOfView = -1;
	private boolean stereoTested = false;

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
				gl.glDisable(GL.GL_FOG);

				stereo.setup(true, swap ? Eye.LEFT : Eye.RIGHT);
				setupBuffer(gl, mode, Eye.RIGHT);
				this.applyView(dc);
				this.draw(dc);

				stereo.setup(false, Eye.LEFT);
				restoreBuffer(gl, mode);
				view.apply(dc);
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
