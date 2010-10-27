package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.io.File;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.AnimatorSceneController;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationRenderer} that renders each frame of the animation to an offscreen texture, then
 * writes that texture to disk.
 *
 */
public class OffscreenRenderer extends AnimationRendererBase
{
	private WorldWindow worldWindow;
	private Animator targetApplication;
	private AnimatorSceneController animatorSceneController;
	
	private int frameBuffer;
	private int texture;
	private int depthBuffer;
	
	private boolean detectCollisions;
	private double detailHintBackup;
	private boolean wasImmediate;
	
	public OffscreenRenderer(WorldWindow wwd, Animator targetApplication)
	{
		Validate.notNull(wwd, "A world window is required");
		Validate.notNull(targetApplication, "An Animator application is required");
		
		this.worldWindow = wwd;
		this.targetApplication = targetApplication;
		this.animatorSceneController = (AnimatorSceneController)wwd.getSceneController();
	}
	
	@Override
	protected void doPreRender(final Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		setupForRendering(detailHint);
		
		animatorSceneController.addPrePaintTask(new Runnable()
		{
			@Override
			public void run()
			{
				setupFrameBuffer(animation);
			}
		});
		worldWindow.redrawNow();
	}


	@Override
	protected void doRender(Animation animation, int frame, File targetFile, double detailHint, boolean alpha)
	{
		targetApplication.setSlider(frame);
		animation.applyFrame(frame);
		
		animatorSceneController.takeScreenshot(targetFile, alpha);
		worldWindow.redraw();
		animatorSceneController.waitForScreenshot();
	}

	@Override
	protected void doPostRender(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha)
	{
		animatorSceneController.addPostPaintTask(new Runnable()
		{
			@Override
			public void run()
			{
				teardownFrameBuffer();
			}
		});
		worldWindow.redrawNow();
		
		resetViewingParameters();
	}

	private void setupForRendering(double detailHint)
	{
		wasImmediate = ImmediateMode.isImmediate();
		ImmediateMode.setImmediate(true);

		targetApplication.disableUtilityLayers();

		detailHintBackup = targetApplication.getDetailedElevationModel().getDetailHint();
		targetApplication.getDetailedElevationModel().setDetailHint(detailHint);

		OrbitView orbitView = (OrbitView) worldWindow.getView();
		detectCollisions = orbitView.isDetectCollisions();
		orbitView.setDetectCollisions(false);
	}

	private void setupFrameBuffer(Animation animation)
	{
		GL gl = animatorSceneController.getDrawContext().getGL();
		Dimension renderDimensions = animation.getRenderParameters().getImageDimension();
		
		frameBuffer = generateFrameBuffer(gl);
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBuffer);
		
		texture = generateTexture(gl, renderDimensions);
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, texture, 0);
		
		depthBuffer = generateDepthBuffer(gl, renderDimensions);
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, depthBuffer);
		
	}
	
	/**
	 * @return The ID of the generated frame buffer object
	 */
	private int generateFrameBuffer(GL gl)
	{
		int[] frameBuffers = new int[1];
		gl.glGenFramebuffersEXT(1, frameBuffers , 0);
		return frameBuffers[0];
	}
	
	/**
	 * @return The ID of the generated texture
	 */
	private int generateTexture(GL gl, Dimension renderDimensions)
	{
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT24_ARB, renderDimensions.width, renderDimensions.height, 0, GL.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_INT, null);
		return textures[0];
	}
	
	/**
	 * @return The ID of the generated depth buffer
	 */
	private int generateDepthBuffer(GL gl, Dimension renderDimensions)
	{
		int[] renderBuffers = new int[1];
		gl.glGenRenderbuffersEXT(1, renderBuffers, 0);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderBuffers[0]);
		gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT24, renderDimensions.width, renderDimensions.height);
		return renderBuffers[0];
	}
	
	/**
	 * Performs necessary cleanup to remove the framebuffer
	 */
	private void teardownFrameBuffer()
	{
		GL gl = animatorSceneController.getDrawContext().getGL();
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
	}
	
	private void resetViewingParameters()
	{
		targetApplication.reenableUtilityLayers();
		
		targetApplication.getDetailedElevationModel().setDetailHint(detailHintBackup);
		((OrbitView)worldWindow.getView()).setDetectCollisions(detectCollisions);
		ImmediateMode.setImmediate(wasImmediate);
	}

}
