package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.gov.ga.worldwind.animator.util.TGAScreenshot;

public class AnimatorSceneController extends QualitySceneController
{
	private boolean alpha = true;
	private File screenshotFile = null;
	private Object semaphore = new Object();
	private Object semaphore2 = new Object();
	
	private Queue<Runnable> prePaintTasks = new LinkedList<Runnable>();
	private Lock prePaintTasksLock = new ReentrantLock(true);
	
	private Queue<Runnable> postPaintTasks = new LinkedList<Runnable>();
	private Lock postPaintTasksLock = new ReentrantLock(true);
	
	@Override
	public void doRepaint(DrawContext dc)
	{
		synchronized (semaphore2)
		{
			doPrePaintTasks();
			
			super.doRepaint(dc);
			
			if (screenshotFile != null)
			{
				if (!screenshotFile.getParentFile().exists())
				{
					screenshotFile.getParentFile().mkdirs();
				}
				try
				{
					int width = dc.getGLDrawable().getWidth();
					int height = dc.getGLDrawable().getHeight();
					if (screenshotFile.getName().toLowerCase().endsWith(".tga"))
					{
						TGAScreenshot.writeToTargaFile(screenshotFile, width, height, alpha);
					}
					else
					{
						com.sun.opengl.util.Screenshot.writeToFile(screenshotFile, width, height, alpha);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				synchronized (semaphore)
				{
					screenshotFile = null;
					semaphore.notifyAll();
				}
			}
			
			doPostPaintTasks();
		}
	}

	public void takeScreenshot(File screenshotFile, boolean alpha)
	{
		synchronized (semaphore2)
		{
			this.screenshotFile = screenshotFile;
			this.alpha = alpha;
		}
	}

	public void waitForScreenshot()
	{
		synchronized (semaphore)
		{
			if (screenshotFile != null)
			{
				try
				{
					semaphore.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Add a task to be executed on the render thread prior to painting
	 */
	public void addPrePaintTask(Runnable r)
	{
		prePaintTasksLock.lock();
		prePaintTasks.add(r);
		prePaintTasksLock.unlock();
	}
	
	/**
	 * Add a task to be executed on the render thread immediately after
	 */
	public void addPostPaintTask(Runnable r)
	{
		postPaintTasksLock.lock();
		postPaintTasks.add(r);
		postPaintTasksLock.unlock();
	}
	
	private void doPrePaintTasks()
	{
		prePaintTasksLock.lock();
		try
		{
			while (!prePaintTasks.isEmpty())
			{
				prePaintTasks.remove().run();
			}
		}
		finally
		{
			prePaintTasksLock.unlock();
		}
	}
	
	private void doPostPaintTasks()
	{
		postPaintTasksLock.lock();
		try
		{
			while (!postPaintTasks.isEmpty())
			{
				postPaintTasks.remove().run();
			}
		}
		finally
		{
			postPaintTasksLock.unlock();
		}
	}
}
