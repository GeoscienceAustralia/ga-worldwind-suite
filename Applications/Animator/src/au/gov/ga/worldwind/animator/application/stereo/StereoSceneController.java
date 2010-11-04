package au.gov.ga.worldwind.animator.application.stereo;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.io.Serializable;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.application.render.FrameBuffer;
import au.gov.ga.worldwind.common.util.EnumPersistenceDelegate;
import au.gov.ga.worldwind.common.view.stereo.StereoView;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;

public class StereoSceneController extends AbstractSceneController
{
	private boolean stereoEnabled = false;
	private StereoMode stereoMode = StereoMode.RC_ANAGLYPH;

	private FrameBuffer leftFrameBuffer = new FrameBuffer();
	private FrameBuffer rightFrameBuffer = new FrameBuffer();

	public boolean isStereoEnabled()
	{
		return stereoEnabled;
	}

	public void setStereoEnabled(boolean stereoEnabled)
	{
		this.stereoEnabled = stereoEnabled;
	}

	public StereoMode getStereoMode()
	{
		return stereoMode;
	}

	public void setStereoMode(StereoMode stereoMode)
	{
		this.stereoMode = stereoMode;
	}

	@Override
	protected void doRepaint(DrawContext dc)
	{
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

			GL gl = dc.getGL();

			StereoView view = dc.getView() instanceof StereoView ? (StereoView) dc.getView() : null;
			if (view == null || !stereoEnabled)
			{
				this.draw(dc);
			}
			else
			{
				//ensure the frame buffers match the canvas size
				Dimension size = new Dimension(dc.getDrawableWidth(), dc.getDrawableHeight());
				leftFrameBuffer.resize(gl, size);
				rightFrameBuffer.resize(gl, size);

				//draw the left eye to the left frame buffer
				leftFrameBuffer.bind(gl);
				view.setup(true, Eye.LEFT);
				this.applyView(dc);
				this.clearFrame(dc);
				this.draw(dc);
				leftFrameBuffer.unbind(gl);

				//draw the right eye to the right frame buffer
				rightFrameBuffer.bind(gl);
				view.setup(true, Eye.RIGHT);
				this.applyView(dc);
				this.clearFrame(dc);
				this.draw(dc);
				rightFrameBuffer.unbind(gl);

				//apply the view with stereo disabled, so that the matrices are correct for picking
				view.setup(false, Eye.LEFT);
				this.applyView(dc);

				try
				{
					gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
					
					gl.glDepthMask(false);
					gl.glEnable(GL.GL_BLEND);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

					//draw the left and right frame buffers as textured quads
					setupStereoMode(gl, getStereoMode(), Eye.LEFT);
					FrameBuffer.renderTexturedQuad(gl, leftFrameBuffer.getTextureId());
					setupStereoMode(gl, getStereoMode(), Eye.RIGHT);
					FrameBuffer.renderTexturedQuad(gl, rightFrameBuffer.getTextureId());

					//reset the depth mask and stereo drawing
					packDownStereo(gl, getStereoMode());
				}
				finally
				{
					gl.glPopAttrib();
				}
			}
		}
		finally
		{
			this.finalizeFrame(dc);
		}
	}

	private void setupStereoMode(GL gl, StereoMode mode, Eye eye)
	{
		boolean left = eye == Eye.LEFT;
		switch (mode)
		{
		case ALPHA_BLENDING:
			float alpha = left ? 1.0f : 0.5f;
			gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
			break;
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

	private void packDownStereo(GL gl, StereoMode mode)
	{
		switch (mode)
		{
		case ALPHA_BLENDING:
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			break;
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

	public enum StereoMode implements Serializable
	{
		ALPHA_BLENDING("Alpha blending"),
		STEREO_BUFFER("Hardware stereo buffer"),
		RC_ANAGLYPH("Red/cyan anaglyph"),
		GM_ANAGLYPH("Green/magenta anaglyph"),
		BY_ANAGLYPH("Blue/yellow anaglyph");

		private String pretty;

		StereoMode(String pretty)
		{
			this.pretty = pretty;
		}

		@Override
		public String toString()
		{
			return pretty;
		}

		static
		{
			EnumPersistenceDelegate.installFor(values());
		}
	}
}
