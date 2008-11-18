package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.util.StatusBar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import path.AnimationPath;
import path.AnimationPoint;
import path.Point;
import path.Position;
import path.Vector;

import com.sun.opengl.util.BufferUtil;

public class Animator
{
	static
	{
		if (Configuration.isWindowsOS())
		{
			System.setProperty("sun.java2d.noddraw", "true");
		}
		else if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name",
					"World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes",
					"false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");
	}

	public static void main(String[] args)
	{
		new Animator();
	}

	private JFrame frame;
	private WorldWindowGLCanvas wwd;
	private boolean takingScreenshot = false;

	public Animator()
	{
		frame = new JFrame("World Wind");

		frame.setLayout(new BorderLayout());
		wwd = new WorldWindowGLCanvas();
		Model model = new BasicModel();
		wwd.setModel(model);
		wwd.setPreferredSize(new Dimension(800, 600));
		frame.add(wwd, BorderLayout.CENTER);

		JPanel left = new JPanel(new GridLayout(0, 1));
		frame.add(left, BorderLayout.WEST);

		JButton button = new JButton("Take screenshot");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				takeScreenshot("screenshot.png");
			}
		});

		button = new JButton("Animate");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate();
			}
		});

		StatusBar statusBar = new StatusBar();
		frame.add(statusBar, BorderLayout.PAGE_END);
		statusBar.setEventSource(wwd);

		//do not take a screenshot while the following are true:
		//WorldWind.getTaskService().hasActiveTasks();
		//WorldWind.getRetrievalService().hasActiveTasks();

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				quit();
			}
		});

		frame.pack();
		frame.setVisible(true);
	}

	public void quit()
	{
		frame.dispose();
		System.exit(0);
	}

	private void takeScreenshot(String filename)
	{
		if (!EventQueue.isDispatchThread())
		{
			takingScreenshot = true;
		}

		wwd.addRenderingListener(new Screenshotter(filename));
		wwd.redraw();

		if (!EventQueue.isDispatchThread())
		{
			while (takingScreenshot)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private double e2a(double elevation)
	{
		return Math.log(elevation) * 10d;
	}

	private double a2e(double animation)
	{
		return Math.pow(Math.E, animation / 10d);
	}

	private void animate()
	{
		Vector pos = new Vector(0, 0, 40);
		Vector zero = new Vector(0, 0, 0);

		final AnimationPath path = new AnimationPath();
		/*AnimationPoint p1 = new AnimationPoint(0, new Position(41.68695,
				-87.70575, e2a(374070)), 0d, 0d, zero, zero);
		AnimationPoint p2 = new AnimationPoint(4, new Position(41.68695,
				-87.70575, e2a(6889382)), 0d, 0d, zero, pos);
		AnimationPoint p3 = new AnimationPoint(8, new Position(51.44871,
				-0.01974, e2a(6889382)), 0d, 0d, pos, zero);
		AnimationPoint p4 = new AnimationPoint(12, new Position(51.44871,
				-0.01974, e2a(374070)), 0d, 0d, zero, zero);
		path.points.add(p1);
		path.points.add(p2);
		path.points.add(p3);
		path.points.add(p4);*/

		AnimationPoint p1 = new AnimationPoint(0, new Position(41.68695,
				-87.70575, e2a(374070)), 0d, 0d, zero, pos);
		AnimationPoint p2 = new AnimationPoint(5, new Position(51.44871,
				-0.01974, e2a(374070)), 0d, 0d, pos, zero);
		AnimationPoint p3 = new AnimationPoint(7, new Position(51.44871,
				-0.01974, e2a(374070)), 0d, 0d, zero, pos);
		AnimationPoint p4 = new AnimationPoint(12, new Position(-27, 133.5,
				e2a(374070)), 0d, 0d, pos, zero);
		path.points.add(p1);
		path.points.add(p2);
		path.points.add(p3);
		path.points.add(p4);

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				View view = wwd.getSceneController().getView();

				long currentTime = 0;
				while (currentTime / 1000d <= path.getMaxTime())
				{
					currentTime += 10;
					double time = currentTime / 1000d;
					Point point = path.getPositionAt(time);
					System.out.println(point.position.elevation);
					point.position.elevation = a2e(point.position.elevation);
					gov.nasa.worldwind.geom.Position position = point.position
							.getPosition();
					view.setEyePosition(position);
					wwd.redraw();
					
					takeScreenshot("screen" + (currentTime / 10) + ".png");

					System.out.println(time + " = " + position);
				}
			}
		});
		thread.start();
	}

	private class Screenshotter implements RenderingListener
	{
		private final String filename;

		public Screenshotter(String filename)
		{
			this.filename = filename;
		}

		public void stageChanged(RenderingEvent event)
		{
			if (WorldWind.getTaskService().hasActiveTasks()
					|| WorldWind.getRetrievalService().hasActiveTasks())
			{
				System.out
						.println("Screenshot not ready, sleeping and redrawing");
				sleep();
				wwd.redraw();
			}
			else if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
			{
				wwd.removeRenderingListener(this);
				saveFrame(filename);
				takingScreenshot = false;
			}
		}

		private void sleep()
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	};

	private void saveFrame(String filename)
	{
		GL gl = wwd.getGL();
		int width = wwd.getWidth();
		int height = wwd.getHeight();
		ByteBuffer buffer = BufferUtil.newByteBuffer(width * height * 3);
		gl.glReadPixels(0, 0, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
				buffer);
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int index = 3 * ((height - y - 1) * width + x);
				int argb = (((int) (buffer.get(index + 0)) & 0xFF) << 16) //r
						| (((int) (buffer.get(index + 1)) & 0xFF) << 8) //g
						| (((int) (buffer.get(index + 2)) & 0xFF)); //b

				img.setRGB(x, y, argb);
			}
		}

		File out = new File(filename);
		try
		{
			ImageIO.write(img, "png", out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Screenshot saved to " + out);
	}
}
