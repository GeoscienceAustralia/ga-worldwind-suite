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

import java.io.File;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.animator.util.TGAScreenshot;
import au.gov.ga.worldwind.common.render.PaintTask;

import com.jogamp.opengl.util.awt.Screenshot;

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
			dc.getGL().getGL2().glGetIntegerv(GL2.GL_VIEWPORT, viewportArray, 0);
			int width = viewportArray[2];
			int height = viewportArray[3];

			if (screenshotFile.getName().toLowerCase().endsWith(".tga"))
			{
				TGAScreenshot.writeToTargaFile(screenshotFile, width, height, alpha);
			}
			else
			{
				Screenshot.writeToFile(screenshotFile, width, height, alpha);
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
