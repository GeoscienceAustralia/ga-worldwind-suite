package application;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GLContext;
import javax.swing.JDialog;

import animation.SimpleAnimation;

import com.sun.opengl.util.Screenshot;

public class Renderer extends JDialog
{
	public Renderer(Frame owner, final SimpleAnimation animation,
			final Model model, final double verticalExaggeration)
	{
		super(owner, false);

		final int width = animation.getWidth();
		final int height = animation.getHeight();

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				final WorldWindowPBuffer pbuffer = new WorldWindowPBuffer(
						model, width, height);
				pbuffer.getSceneController().setVerticalExaggeration(
						verticalExaggeration);

				pbuffer.redrawNow();
				OrbitView view = (OrbitView) pbuffer.getView();
				view.setDetectCollisions(false);

				for (int frame = animation.getFirstFrame(); frame <= animation
						.getLastFrame(); frame++)
				{
					animation.applyFrame(view, frame);
					pbuffer.redrawNow();

					boolean anyTasks = anyTasks();
					while (anyTasks)
					{
						sleep();
						anyTasks = anyTasks();
						if (!anyTasks)
						{
							pbuffer.redrawNow();
							anyTasks = anyTasks();
						}
					}

					GLContext context = pbuffer.getPbuffer()
							.createContext(null);
					context.makeCurrent();
					BufferedImage image = Screenshot.readToBufferedImage(width,
							height, true);
					context.release();
					context.destroy();
					File file = new File("frames/frame" + frame + ".png");
					try
					{
						ImageIO.write(image, "PNG", file);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			};

			private boolean anyTasks()
			{
				return WorldWind.getTaskService().hasActiveTasks()
						|| WorldWind.getRetrievalService().hasActiveTasks();
			}

			private void sleep()
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
		});
		thread.setDaemon(true);
		thread.start();
	}
}
