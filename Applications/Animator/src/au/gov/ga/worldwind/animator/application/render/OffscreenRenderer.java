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
 * @author James Navin
 * @author Michael de Hoog
 */
public class OffscreenRenderer extends AnimationRendererBase
{
	protected WorldWindow wwd;
	protected Animator targetApplication;
	protected AnimatorSceneController animatorSceneController;
	
	private FrameBuffer frameBuffer = new FrameBuffer();

	private boolean detectCollisions;
	private double detailHintBackup;
	private boolean wasImmediate;

	private PaintTask preRenderTask;
	private PaintTask prePostRenderTask;
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
				frameBuffer.create(dc.getGL(), renderDimensions);
			}
		});

		//create a pre PaintTask which will setup the viewport and FBO every frame
		preRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL gl = dc.getGL();
				frameBuffer.bind(gl);
				gl.glViewport(0, 0, renderDimensions.width, renderDimensions.height);
			}
		};
		
		prePostRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL gl = dc.getGL();
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
				frameBuffer.unbind(gl);
				gl.glViewport(0, 0, dc.getDrawableWidth(), dc.getDrawableHeight());
				FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTextureId());
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
		
		//also add a viewport set just before the screenshot, to ensure the viewport is always correct
		animatorSceneController.addPostPaintTask(prePostRenderTask);

		//add the screenshot task
		ScreenshotPaintTask screenshotTask = new ScreenshotPaintTask(targetFile, alpha);
		animatorSceneController.addPostPaintTask(screenshotTask);

		//add the post render task AFTER the screenshot task, so that the screenshot is taken from the FBO
		animatorSceneController.addPostPaintTask(postRenderTask);

		//redraw, and then wait for the screenshot to complete 
		wwd.redraw();
		screenshotTask.waitForScreenshot();
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
				frameBuffer.delete(dc.getGL());
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

	private void resetViewingParameters()
	{
		targetApplication.reenableUtilityLayers();

		targetApplication.getDetailedElevationModel().setDetailHint(detailHintBackup);
		((OrbitView) wwd.getView()).setDetectCollisions(detectCollisions);
		ImmediateMode.setImmediate(wasImmediate);
	}
}
