package au.gov.ga.worldwind.viewer.application;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;

import com.sun.opengl.util.Screenshot;

/**
 * Utility class for taking screenshots of WorldWind.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Screenshotter
{
	/**
	 * Take a simple screenshot of the WorldWindow canvas at the current
	 * dimensions.
	 * 
	 * @param wwd
	 *            WorldWindow to take a screenshot of.
	 * @param canvas
	 *            GLCanvas associated with the WorldWindow.
	 * @param file
	 *            File to save the screenshot to.
	 */
	public static void takeScreenshot(WorldWindow wwd, GLCanvas canvas, File file)
	{
		wwd.addRenderingListener(new SimpleScreenshotListener(wwd, file, canvas.getWidth(), canvas.getHeight()));
	}

	/**
	 * Take a screenshot at the provided dimensions. Temporarily resizes the
	 * WorldWind canvas to the dimensions. If the dimensions are larger than the
	 * current screen resolution, the canvas is moved around the screen and
	 * captured in parts. This is because any part of the canvas outside of the
	 * screen bounds cannot be captured.
	 * 
	 * @param wwd
	 *            WorldWindow to take a screenshot of.
	 * @param width
	 *            Width of the screenshot.
	 * @param height
	 *            Height of the screenshot.
	 * @param file
	 *            File to save the screenshot to.
	 */
	public static void takeScreenshot(final WorldWindow wwd, final int width, final int height, final File file)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				takeScreenshotThread(wwd, width, height, file);
			}
		});
		thread.start();
	}

	private static void takeScreenshotThread(WorldWindow wwd, int width, int height, File file)
	{
		if (!(wwd instanceof Component))
			throw new IllegalArgumentException();

		Component component = (Component) wwd;

		Container parent = component.getParent();
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.add(component, BorderLayout.CENTER);
		Dimension size = new Dimension(width, height);
		component.setMinimumSize(size);
		component.setMaximumSize(size);
		component.setPreferredSize(size);
		frame.setSize(size);
		frame.setVisible(true);

		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		DisplayMode displayMode = device.getDisplayMode();
		int displayWidth = displayMode.getWidth();
		int displayHeight = displayMode.getHeight();

		final int rows = (width - 1) / displayWidth + 1;
		final int columns = (height - 1) / displayHeight + 1;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = bi.createGraphics();

		for (int x = 0; x < rows; x++)
		{
			for (int y = 0; y < columns; y++)
			{
				final int xOffset = x * displayWidth;
				final int yOffset = y * displayHeight;
				final int screenshotWidth = Math.min(width - xOffset, displayWidth);
				final int screenshotHeight = Math.min(height - yOffset, displayHeight);
				frame.setLocation(-xOffset, -yOffset);

				BufferedImage shot = takeShot(wwd, 0, 0, width, height);
				g2d.drawImage(shot, xOffset, yOffset, xOffset + screenshotWidth, yOffset + screenshotHeight, xOffset,
						yOffset, xOffset + screenshotWidth, yOffset + screenshotHeight, null);
			}
		}
		g2d.dispose();

		String filename = file.getName();
		int dot = filename.lastIndexOf('.');
		String extension = filename.substring(dot + 1, filename.length());
		try
		{
			ImageIO.write(bi, extension, file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		parent.add(component, BorderLayout.CENTER);
		frame.dispose();
	}

	private static BufferedImage takeShot(final WorldWindow wwd, int x, int y, int width, int height)
	{
		ScreenshotListener screenshotListener = new ScreenshotListener(x, y, width, height);

		wwd.addRenderingListener(screenshotListener);
		wwd.redraw();

		while (!screenshotListener.done)
		{
			sleep();
		}

		wwd.removeRenderingListener(screenshotListener);
		return screenshotListener.image;
	}

	private static void sleep()
	{
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static class ScreenshotListener implements RenderingListener
	{
		public boolean done = false;
		public BufferedImage image;

		private int x;
		private int y;
		private int width;
		private int height;
		private boolean doing = false;

		public ScreenshotListener(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public void stageChanged(RenderingEvent event)
		{
			if (!doing && event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
			{
				doing = true;
				image = Screenshot.readToBufferedImage(x, y, width, height, false);
				done = true;
			}
		}
	}

	private static class SimpleScreenshotListener implements RenderingListener
	{
		private File file;
		private int width;
		private int height;
		private WorldWindow wwd;

		public SimpleScreenshotListener(WorldWindow wwd, File file, int width, int height)
		{
			this.wwd = wwd;
			this.file = file;
			this.width = width;
			this.height = height;
		}

		@Override
		public void stageChanged(RenderingEvent event)
		{
			if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
			{
				try
				{
					Screenshot.writeToFile(file, width, height);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				wwd.removeRenderingListener(this);
			}
		}
	}
}
