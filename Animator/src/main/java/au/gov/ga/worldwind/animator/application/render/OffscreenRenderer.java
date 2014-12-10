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
package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.io.File;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.animator.application.AnimatorSceneController;
import au.gov.ga.worldwind.animator.application.ScreenshotPaintTask;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.common.render.FrameBuffer;
import au.gov.ga.worldwind.common.render.PaintTask;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * An {@link AnimationRenderer} that renders each frame of the animation to an
 * offscreen texture, then writes that texture to disk.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
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
		validateNotNullAnimatorApplication(targetApplication);
		Validate.isTrue(wwd.getSceneController() instanceof AnimatorSceneController,
				"SceneController must be an AnimatorSceneController");

		this.wwd = wwd;
		this.targetApplication = targetApplication;
		this.animatorSceneController = (AnimatorSceneController) wwd.getSceneController();
	}
	
	protected void validateNotNullAnimatorApplication(Animator targetApplication)
	{
		Validate.notNull(targetApplication, "An Animator application is required");
	}

	@Override
	protected void doPreRender(final Animation animation, final RenderParameters renderParams)
	{
		setupForRendering(renderParams.getDetailLevel());

		final Dimension renderDimensions = renderParams.getRenderDimension();
		//final Dimension viewDimensions = renderParams.getImageDimension();

		animatorSceneController.setRenderDimensions(renderDimensions);
		animatorSceneController.addPrePaintTask(new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				frameBuffer.create(dc.getGL().getGL2(), renderDimensions);
			}
		});

		//create a pre PaintTask which will setup the viewport and FBO every frame
		preRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL2 gl = dc.getGL().getGL2();
				frameBuffer.bind(gl);
				gl.glViewport(0, 0, renderDimensions.width, renderDimensions.height);
			}
		};

		prePostRenderTask = new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				GL2 gl = dc.getGL().getGL2();
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
				GL2 gl = dc.getGL().getGL2();
				frameBuffer.unbind(gl);
				gl.glViewport(0, 0, dc.getDrawableWidth(), dc.getDrawableHeight());
				FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId());
			}
		};

		wwd.redrawNow();
	}

	@Override
	protected void doRender(int frame, File targetFile, Animation animation, RenderParameters renderParams)
	{
		// Ensure the directories exist
		if (!targetFile.getParentFile().exists())
		{
			targetFile.getParentFile().mkdirs();
		}

		updateSlider(frame);
		animation.applyFrame(frame);

		//add the pre render task
		animatorSceneController.addPrePaintTask(preRenderTask);

		//also add a viewport set just before the screenshot, to ensure the viewport is always correct
		animatorSceneController.addPostPaintTask(prePostRenderTask);

		//add the screenshot task
		ScreenshotPaintTask screenshotTask = new ScreenshotPaintTask(targetFile, renderParams.isRenderAlpha());
		animatorSceneController.addPostPaintTask(screenshotTask);

		//add the post render task AFTER the screenshot task, so that the screenshot is taken from the FBO
		animatorSceneController.addPostPaintTask(postRenderTask);

		//redraw, and then wait for the screenshot to complete 
		wwd.redrawNow();
		screenshotTask.waitForScreenshot();
	}

	@Override
	protected void doPostRender(Animation animation, RenderParameters renderParams)
	{
		animatorSceneController.setRenderDimensions(null);
		animatorSceneController.addPostPaintTask(new PaintTask()
		{
			@Override
			public void run(DrawContext dc)
			{
				frameBuffer.delete(dc.getGL().getGL2());
			}
		});

		wwd.redrawNow();
		resetViewingParameters();
	}
	
	protected void updateSlider(int frame)
	{
		targetApplication.setSlider(frame);
	}

	protected void setupForRendering(double detailHint)
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

	protected void resetViewingParameters()
	{
		targetApplication.reenableUtilityLayers();

		targetApplication.getDetailedElevationModel().setDetailHint(detailHintBackup);
		((OrbitView) wwd.getView()).setDetectCollisions(detectCollisions);
		ImmediateMode.setImmediate(wasImmediate);
	}
}
