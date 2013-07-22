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
package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectTileBuilder;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.render.OffscreenRenderer;
import au.gov.ga.worldwind.animator.application.render.OffscreenSurfaceObjectRenderer;
import au.gov.ga.worldwind.common.render.ExtendedSceneController;

/**
 * A custom scene controller that supports {@link Effect}s, as well as pre/post
 * paint tasks (for taking screen captures when rendering the animation).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnimatorSceneController extends ExtendedSceneController
{
	private final Queue<PaintTask> prePaintTasks = new LinkedList<PaintTask>();
	private final Lock prePaintTasksLock = new ReentrantLock(true);

	private final Queue<PaintTask> postPaintTasks = new LinkedList<PaintTask>();
	private final Lock postPaintTasksLock = new ReentrantLock(true);

	private Dimension renderDimensions;
	private Animation animation;

	@Override
	public void doRepaint(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);

		doPrePaintTasks(dc);
		performRepaint(dc);
		doPostPaintTasks(dc);
	}

	private void performRepaint(DrawContext dc)
    {
        this.initializeFrame(dc);
        try
        {
            this.applyView(dc);
            this.createPickFrustum(dc);
            this.createTerrain(dc);
            this.preRender(dc);
            this.clearFrame(dc);
            this.pick(dc);
            this.clearFrame(dc);
            this.draw(dc);
        }
        finally
        {
            this.finalizeFrame(dc);
        }
    }
	
	@Override
	protected void draw(DrawContext dc)
	{
		//The draw call is the lowest level rendering call for the SceneController,
		//so we still have depth information at this level, which is needed for DoF.
		Dimension dimensions =
				renderDimensions != null ? renderDimensions : new Dimension(dc.getDrawableWidth(),
						dc.getDrawableHeight());

		//retrieve the list of effects, put them in a new list
		List<Effect> effects = new ArrayList<Effect>(animation.getEffects());

		//remove any disabled effects
		for (int i = effects.size() - 1; i >= 0; i--)
		{
			Effect effect = effects.get(i);
			if (!effect.isEnabled())
			{
				effect.releaseResources(dc);
				effects.remove(i);
			}
		}

		//if there's no enabled effects, draw normally
		if (effects.isEmpty())
		{
			super.draw(dc);
			return;
		}

		Effect firstEffect = effects.get(0);
		Effect lastEffect = effects.get(effects.size() - 1);

		try
		{
			firstEffect.bindFrameBuffer(dc, dimensions);
			this.clearFrame(dc);
			//draw the actual scene onto the first effect's frame buffer:
			super.draw(dc);
		}
		finally
		{
			firstEffect.unbindFrameBuffer(dc, dimensions);
		}

		for (int i = 1; i < effects.size(); i++)
		{
			try
			{
				effects.get(i).bindFrameBuffer(dc, dimensions);
				this.clearFrame(dc);
				//draw the previous effect's frame buffer onto the current frame buffer:
				effects.get(i - 1).drawFrameBufferWithEffect(dc, dimensions);
			}
			finally
			{
				effects.get(i).unbindFrameBuffer(dc, dimensions);
			}
		}

		lastEffect.drawFrameBufferWithEffect(dc, dimensions); //draw the final effect's frame buffer onto the final buffer
	}

	/**
	 * Add a task to be executed on the render thread prior to painting
	 */
	public void addPrePaintTask(PaintTask r)
	{
		prePaintTasksLock.lock();
		prePaintTasks.add(r);
		prePaintTasksLock.unlock();
	}

	/**
	 * Add a task to be executed on the render thread immediately after
	 */
	public void addPostPaintTask(PaintTask r)
	{
		postPaintTasksLock.lock();
		postPaintTasks.add(r);
		postPaintTasksLock.unlock();
	}

	private void doPrePaintTasks(DrawContext dc)
	{
		prePaintTasksLock.lock();
		try
		{
			while (!prePaintTasks.isEmpty())
			{
				prePaintTasks.remove().run(dc);
			}
		}
		finally
		{
			prePaintTasksLock.unlock();
		}
	}

	private void doPostPaintTasks(DrawContext dc)
	{
		postPaintTasksLock.lock();
		try
		{
			while (!postPaintTasks.isEmpty())
			{
				postPaintTasks.remove().run(dc);
			}
		}
		finally
		{
			postPaintTasksLock.unlock();
		}
	}

	@Override
	protected SurfaceObjectTileBuilder createSurfaceObjectTileBuilder()
	{
		return new OffscreenSurfaceObjectRenderer();
	}

	/**
	 * @return The current render dimensions (null if not rendering)
	 */
	public Dimension getRenderDimensions()
	{
		return renderDimensions;
	}

	/**
	 * Set the current render dimensions. This is called by the
	 * {@link OffscreenRenderer} when the rendering begins.
	 * 
	 * @param renderDimensions
	 */
	public void setRenderDimensions(Dimension renderDimensions)
	{
		this.renderDimensions = renderDimensions;
	}

	/**
	 * @return The current animation
	 */
	public Animation getAnimation()
	{
		return animation;
	}

	/**
	 * Set the current animation. This is called by the {@link Animator} when
	 * the animation changes.
	 * 
	 * @param animation
	 */
	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}
}
