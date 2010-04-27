package application;

import gov.nasa.worldwind.render.DrawContext;

import java.io.File;

import util.TGAScreenshot;

public class AnimatorSceneController extends QualitySceneController
{
	private boolean alpha = true;
	private File screenshotFile = null;
	private Object semaphore = new Object();
	private Object semaphore2 = new Object();

	@Override
	public void doRepaint(DrawContext dc)
	{
		synchronized (semaphore2)
		{
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
						/*com.sun.opengl.util.Screenshot.writeToTargaFile(screenshotFile,
								width, height, true);*/
						TGAScreenshot.writeToTargaFile(screenshotFile, width,
								height, alpha);
					}
					else
					{
						com.sun.opengl.util.Screenshot.writeToFile(
								screenshotFile, width, height, alpha);
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
}
