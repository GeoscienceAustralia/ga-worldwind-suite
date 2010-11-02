package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.io.File;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.AnimatorSceneController;
import au.gov.ga.worldwind.animator.application.PaintTask;
import au.gov.ga.worldwind.animator.application.ScreenshotPaintTask;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationRenderer} that renders each frame of the animation to an
 * offscreen texture, then writes that texture to disk.
 * 
 */
public class OffscreenRenderer extends AnimationRendererBase
{
	protected WorldWindow wwd;
	protected Animator targetApplication;
	protected AnimatorSceneController animatorSceneController;

	private int frameBufferId;
	private int textureId;
	private int depthBufferId;

	private boolean detectCollisions;
	private double detailHintBackup;
	private boolean wasImmediate;

	private PaintTask preRenderTask;
	private PaintTask postRenderTask;

	public OffscreenRenderer(WorldWindow wwd, Animator targetApplication)
	{
		Validate.notNull(wwd, "A world window is required");
		Validate.notNull(targetApplication, "An Animator application is required");
		Validate.isTrue(wwd.getSceneController() instanceof AnimatorSceneController, "SceneController must be an AnimatorSceneController");

		this.wwd = wwd;
		this.targetApplication = targetApplication;
		this.animatorSceneController = (AnimatorSceneController) wwd.getSceneController();
	}

	@Override
	protected void doPreRender(final Animation animation, int firstFrame, int lastFrame, File outputDir,
			String frameName, double detailHint, boolean alpha)
	{
		setupForRendering(detailHint);

		final Dimension renderDimensions = animation.getRenderParameters().getImageDimension();

		animatorSceneController.addPrePaintTask(new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				setupFrameBuffer(renderDimensions, dc);
			}
		});

		//create a pre PaintTask which will setup the viewport and FBO every frame
		preRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL gl = dc.getGL();
				gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
				gl.glViewport(0, 0, renderDimensions.width, renderDimensions.height);
			}
		};

		//create a post PaintTask which will reset the viewport, unbind the FBO, and draw the
		//offscreen texture to a quad in screen coordinates
		postRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL gl = dc.getGL();
				gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
				gl.glViewport(0, 0, dc.getDrawableWidth(), dc.getDrawableHeight());
				renderTexturedQuad(gl, textureId);
			}
		};

		wwd.redrawNow();
	}

	@Override
	protected void doRender(Animation animation, int frame, final File targetFile, double detailHint, boolean alpha)
	{
		targetApplication.setSlider(frame);
		animation.applyFrame(frame);

		//add the pre render task
		animatorSceneController.addPrePaintTask(preRenderTask);
		
		//also add it post render, just before the screenshot, to ensure the viewport is always correct
		animatorSceneController.addPostPaintTask(preRenderTask);

		//add the screenshot task
		ScreenshotPaintTask screenshotTask = new ScreenshotPaintTask(targetFile, alpha);
		animatorSceneController.addPostPaintTask(screenshotTask);

		//add the post render task AFTER the screenshot task, so that the screenshot is taken from the FBO
		animatorSceneController.addPostPaintTask(postRenderTask);

		//redraw, and then wait for the screenshot to complete 
		wwd.redraw();
		screenshotTask.waitForScreenshot();
	}

	private static void renderTexturedQuad(GL gl, int textureId)
	{
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glPushAttrib(GL.GL_ENABLE_BIT);

		try
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);

			gl.glBegin(GL.GL_QUADS);
			{
				gl.glTexCoord2f(0, 0);
				gl.glVertex3i(-1, -1, -1);
				gl.glTexCoord2f(1, 0);
				gl.glVertex3i(1, -1, -1);
				gl.glTexCoord2f(1, 1);
				gl.glVertex3i(1, 1, -1);
				gl.glTexCoord2f(0, 1);
				gl.glVertex3i(-1, 1, -1);
			}
			gl.glEnd();
		}
		finally
		{
			gl.glPopMatrix();
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPopMatrix();
			gl.glPopAttrib();
		}
	}

	@Override
	protected void doPostRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName,
			double detailHint, boolean alpha)
	{
		animatorSceneController.addPostPaintTask(new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				teardownFrameBuffer(dc);
			}
		});

		wwd.redrawNow();
		resetViewingParameters();
	}

	private void setupForRendering(double detailHint)
	{
		wasImmediate = ImmediateMode.isImmediate();
		ImmediateMode.setImmediate(true);

		targetApplication.disableUtilityLayers();

		detailHintBackup = targetApplication.getDetailedElevationModel().getDetailHint();
		targetApplication.getDetailedElevationModel().setDetailHint(detailHint);

		OrbitView orbitView = (OrbitView) wwd.getView();
		detectCollisions = orbitView.isDetectCollisions();
		orbitView.setDetectCollisions(false);
	}

	private void setupFrameBuffer(Dimension renderDimensions, DrawContext dc)
	{
		GL gl = dc.getGL();

		//generate a texture, depth buffer, and frame buffer
		textureId = generateTexture(gl, renderDimensions);
		depthBufferId = generateDepthBuffer(gl, renderDimensions);
		frameBufferId = generateFrameBuffer(gl);

		//bind the frame buffer
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
		//bind the color and depth attachments to the frame buffer
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, textureId, 0);
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, depthBufferId);

		//check to see if the frame buffer is supported and complete
		int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		if (status == GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT)
		{
			throw new IllegalStateException("Frame buffer unsupported, or parameters incorrect");
		}
		else if (status != GL.GL_FRAMEBUFFER_COMPLETE_EXT)
		{
			throw new IllegalStateException("Frame buffer incomplete");
		}

		//unbind the frame buffer (bound later in the prerender task)
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	}

	/**
	 * @return The ID of the generated frame buffer object
	 */
	private int generateFrameBuffer(GL gl)
	{
		int[] frameBuffers = new int[1];
		gl.glGenFramebuffersEXT(1, frameBuffers, 0);
		return frameBuffers[0];
	}

	/**
	 * @return The ID of the generated texture
	 */
	private int generateTexture(GL gl, Dimension renderDimensions)
	{
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		if (textures[0] <= 0)
		{
			throw new IllegalStateException("Error generating texture for offscreen rendering");
		}
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, renderDimensions.width, renderDimensions.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		return textures[0];
	}

	/**
	 * @return The ID of the generated depth buffer
	 */
	private int generateDepthBuffer(GL gl, Dimension renderDimensions)
	{
		int[] renderBuffers = new int[1];
		gl.glGenRenderbuffersEXT(1, renderBuffers, 0);
		if (renderBuffers[0] <= 0)
		{
			throw new IllegalStateException("Error generating depth buffer of offscreen rendering");
		}
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderBuffers[0]);
		gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT24, renderDimensions.width, renderDimensions.height);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
		return renderBuffers[0];
	}

	/**
	 * Performs necessary cleanup to remove the framebuffer
	 */
	private void teardownFrameBuffer(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glDeleteFramebuffersEXT(1, new int[] { frameBufferId }, 0);
		gl.glDeleteRenderbuffersEXT(1, new int[] { depthBufferId }, 0);
		gl.glDeleteTextures(1, new int[] { textureId }, 0);
	}

	private void resetViewingParameters()
	{
		targetApplication.reenableUtilityLayers();

		targetApplication.getDetailedElevationModel().setDetailHint(detailHintBackup);
		((OrbitView) wwd.getView()).setDetectCollisions(detectCollisions);
		ImmediateMode.setImmediate(wasImmediate);
	}
}
