package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnimatorSceneController extends QualitySceneController
{
	private Queue<PaintTask> prePaintTasks = new LinkedList<PaintTask>();
	private Lock prePaintTasksLock = new ReentrantLock(true);

	private Queue<PaintTask> postPaintTasks = new LinkedList<PaintTask>();
	private Lock postPaintTasksLock = new ReentrantLock(true);

	@Override
	public void doRepaint(DrawContext dc)
	{
		doPrePaintTasks(dc);
		super.doRepaint(dc);
		doPostPaintTasks(dc);
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
}
