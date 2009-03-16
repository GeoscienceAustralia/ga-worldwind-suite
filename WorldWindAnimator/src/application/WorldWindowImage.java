package application;

import gov.nasa.worldwind.awt.WorldWindowGLJPanel;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

import javax.imageio.ImageIO;
import javax.media.opengl.GLJPanel;

public class WorldWindowImage extends WorldWindowGLJPanel
{
	private boolean ignore = false;
	private boolean forced = true;
	private boolean save = false;
	private File file;
	private Object semaphore = new Object();

	@Override
	protected void paintComponent(Graphics g)
	{
		if (!ignore || forced || save)
		{
			super.paintComponent(g);
			if (save && file != null)
			{
				try
				{
					Field field = GLJPanel.class
							.getDeclaredField("offscreenImage");
					field.setAccessible(true);
					Object object = field.get(this);
					if (object instanceof BufferedImage)
					{
						BufferedImage image = (BufferedImage) object;
						String format = file.getName().substring(
								file.getName().lastIndexOf('.') + 1);
						ImageIO.write(image, format, file);
					}
					synchronized (semaphore)
					{
						semaphore.notifyAll();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				file = null;
			}
		}
		else
		{
			g.clearRect(0, 0, getWidth(), getHeight());
			g.drawString("Ignoring repaint", getWidth() / 2, getHeight() / 2);
		}
		forced = false;
		save = false;
	}

	public void forceRepaint()
	{
		forced = true;
		repaint();
	}

	public void ignoreRepaint(boolean ignore)
	{
		this.ignore = ignore;
	}

	public void saveImage(File file)
	{
		this.file = file;
		save = true;
		repaint();
		synchronized (semaphore)
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
