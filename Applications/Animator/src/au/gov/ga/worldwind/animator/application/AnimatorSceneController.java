package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.BasicSceneController;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectTileBuilder;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.render.OffscreenSurfaceObjectRenderer;

public class AnimatorSceneController extends BasicSceneController
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
		GL gl = dc.getGL();
		gl.glHint(GL.GL_FOG_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);

		doPrePaintTasks(dc);
		super.doRepaint(dc);
		doPostPaintTasks(dc);
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

	public Dimension getRenderDimensions()
	{
		return renderDimensions;
	}

	public void setRenderDimensions(Dimension renderDimensions)
	{
		this.renderDimensions = renderDimensions;
	}

	public Animation getAnimation()
	{
		return animation;
	}

	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}
}
