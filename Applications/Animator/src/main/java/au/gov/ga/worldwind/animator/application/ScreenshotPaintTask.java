package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;

import java.io.File;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.util.TGAScreenshot;

/**
 * A {@link PaintTask} which saves the current frame buffer to a file. Should be
 * added to the AnimatorSceneController as a postPaintTask. The
 * WorldWindow.redraw() should then be called, and then this.waitForScreenshot()
 * should be called (from the non-GL thread) to block until the screenshot has
 * completed.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ScreenshotPaintTask implements PaintTask
{
	private final File screenshotFile;
	private final boolean alpha;

	private final Object semaphore = new Object();
	private boolean complete = false;

	public ScreenshotPaintTask(File screenshotFile, boolean alpha)
	{
		this.screenshotFile = screenshotFile;
		this.alpha = alpha;
	}

	@Override
	public void run(DrawContext dc)
	{
		if (!screenshotFile.getParentFile().exists())
		{
			screenshotFile.getParentFile().mkdirs();
		}
		try
		{
			int[] viewportArray = new int[4];
			dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
			int width = viewportArray[2];
			int height = viewportArray[3];

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
			complete = true;
			semaphore.notifyAll();
		}
	}

	public void waitForScreenshot()
	{
		synchronized (semaphore)
		{
			if (!complete)
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
}
