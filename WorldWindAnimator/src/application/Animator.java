package application;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.util.StatusBar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import view.BasicRollOrbitView;
import view.RollOrbitView;
import camera.CameraPath;
import camera.motion.MotionParams;
import camera.params.Heading;
import camera.params.LatLon;
import camera.params.Pitch;
import camera.params.Roll;
import camera.params.Zoom;

import com.sun.opengl.util.Screenshot;

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
		//Animator.createPath();
	}

	private JFrame frame;
	private WorldWindowGLCanvas wwd;
	private boolean takingScreenshot = false;

	public Animator()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicRollOrbitView.class
				.getName());

		frame = new JFrame("World Wind");

		frame.setLayout(new BorderLayout());
		wwd = new WorldWindowGLCanvas();
		Model model = new BasicModel();
		wwd.setModel(model);
		wwd.setPreferredSize(new Dimension(1024, 576));
		frame.add(wwd, BorderLayout.CENTER);

		LayerList layers = model.getLayers();

		layers.add(new StarsLayer());
		layers.add(new SkyGradientLayer());
		//layers.add(new FogLayer());
		//layers.add(new BMNGOneImage());
		layers.add(new BMNGWMSLayer());
		//layers.add(new LandsatI3WMSLayer());
		//layers.add(new EarthNASAPlaceNameLayer());
		layers.add(new CompassLayer());
		layers.add(new WorldMapLayer());
		//layers.add(new ScalebarLayer());
		//layers.add(new MGRSGraticuleLayer());
		//layers.add(new TernaryAreasLayer());

		wwd.getSceneController().setVerticalExaggeration(2.0);

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

		button = new JButton("Preview");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(false);
			}
		});

		button = new JButton("Save");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				animate(true);
			}
		});

		/*button = new JButton("Orient");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Position eyePosition = Position.fromDegrees(20, 20, 1000000);
				RollOrbitView view = (RollOrbitView) wwd.getView();
				for (int i = 0; i < 361; i++)
				{
					view.setEye(eyePosition, Angle.fromDegrees(i), Angle.fromDegrees(60),
							Angle.fromDegrees(i));
					wwd.redrawNow();
				}
			}
		});*/

		StatusBar statusBar = new StatusBar();
		frame.add(statusBar, BorderLayout.PAGE_END);
		statusBar.setEventSource(wwd);

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

	private static CameraPath createPilbaraPath()
	{
		LatLon l1 = LatLon.fromDegrees(-27, 133.5);
		LatLon l2 = LatLon.fromDegrees(-21.0474, 119.6);

		Zoom zoom1 = Zoom.fromCameraZoom(12000000);
		Zoom zoom2 = Zoom.fromCameraZoom(500000);

		Heading heading1 = Heading.fromDegrees(0);
		Heading heading2 = Heading.fromDegrees(-360);

		Pitch pitch1 = Pitch.fromDegrees(0);
		Pitch pitch2 = Pitch.fromDegrees(50);

		Roll roll1 = Roll.fromDegrees(0);

		double time1 = 4;
		double time3 = 12;

		MotionParams centerMotion = new MotionParams(8, 2.5, 0, 0);
		MotionParams zoomMotion = new MotionParams(2, 0.5, 0, 0);
		MotionParams headingMotion = new MotionParams(20, 20, 0, 0);
		MotionParams pitchMotion = new MotionParams(20, 20, 0, 0);

		CameraPath path = new CameraPath(l1, l1, zoom1, heading1, pitch1,
				roll1, false);

		path.addLatLon(l2, l2, null, time1, centerMotion);
		path.addZoom(zoom2, time1, zoomMotion);

		path.addHeading(heading2, time3, headingMotion);

		path.addPitch(pitch2, time1, pitchMotion);
		//path.addPitch(pitch3, time3, pitchMotion);

		path.refresh();
		return path;
	}

	private static CameraPath createCanyonPath()
	{
		LatLon l1 = LatLon.fromDegrees(-27, 133.5);
		Zoom zoom1 = Zoom.fromCameraZoom(12000000);
		Heading heading1 = Heading.fromDegrees(0);
		Pitch pitch1 = Pitch.fromDegrees(0);
		Roll roll1 = Roll.fromDegrees(0);

		double time1 = 1;
		double startOffset = 1;
		double endOffset = 0;
		MotionParams motion = new MotionParams(1000, 1000, 0, 0);
		MotionParams headingsMotion = new MotionParams(50, 35, 0, 0);

		Pitch pitch2 = Pitch.fromDegrees(85);
		Zoom zoom2 = Zoom.fromCameraZoom(3000);

		LatLon l2 = LatLon.fromDegrees(37.09, -111.26);
		LatLon l3 = LatLon.fromDegrees(36.945, -111.46);
		LatLon l4 = LatLon.fromDegrees(36.875, -111.56);
		LatLon l5 = LatLon.fromDegrees(36.85, -111.61);
		LatLon l6 = LatLon.fromDegrees(36.64, -111.76);

		/*LatLon l2 = LatLon.fromDegrees(37, 111);
		LatLon l3 = LatLon.fromDegrees(37, 112);
		LatLon l4 = LatLon.fromDegrees(38, 112);
		LatLon l5 = LatLon.fromDegrees(38, 111);
		LatLon l6 = LatLon.fromDegrees(37, 111);*/

		LatLon[] positions = new LatLon[] { l2, l3, l4, l5, l6 };
		Heading[] headings = new Heading[positions.length];
		double[] times = new double[positions.length];
		LatLon[] ins = new LatLon[positions.length];
		LatLon[] outs = new LatLon[positions.length];
		Roll[] rolls = new Roll[positions.length];

		Heading heading2 = Heading.fromDegrees(l2.angleBetween(l6) + 90d);

		//heading is angle between this point and next point
		//time is (distance between this point and last point) * constant

		LatLon last, current, next;
		times[0] = time1;
		for (int i = 0; i < positions.length; i++)
		{
			current = positions[i];
			rolls[i] = Roll.fromDegrees(new Random().nextDouble() * 180 - 90);

			if (i + 1 < positions.length)
			{
				next = positions[i + 1];
				headings[i] = Heading
						.fromDegrees(current.angleBetween(next) + 90d);
				outs[i] = LatLon.interpolate(current, next, 0.5);
			}
			else
			{
				headings[i] = headings[i - 1];
				outs[i] = positions[i];
			}

			if (i != 0)
			{
				last = positions[i - 1];
				times[i] = current.distance(last) * 30 + times[i - 1];
				ins[i] = LatLon.interpolate(last, current, 0.5);
			}
			else
			{
				times[i] = time1;
				ins[i] = positions[i];
			}
			last = current;
		}

		CameraPath path = new CameraPath(l1, l1, zoom1, headings[0], pitch1,
				roll1, true);
		path.addZoom(zoom2, time1, motion);
		path.addPitch(pitch2, time1, motion);

		for (int i = 0; i < positions.length; i++)
		{
			path.addLatLon(positions[i], ins[i], outs[i], times[i], motion);
			path.addHeading(headings[i], times[i], headingsMotion);
			path.addRoll(rolls[i], times[i], motion);
		}

		//path.addHeading(heading2, time1, motion);

		path.setStartOffset(startOffset);
		path.setEndOffset(endOffset);
		path.refresh();
		return path;
	}

	private void animate(final boolean savingFrames)
	{
		final CameraPath path = createCanyonPath();
		//final CameraPath path = createPilbaraPath();

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				Animator.this.frame.setAlwaysOnTop(savingFrames);

				if (savingFrames)
				{
					Toolkit tk = Toolkit.getDefaultToolkit();
					BufferedImage image = new BufferedImage(1, 1,
							BufferedImage.TYPE_INT_ARGB);
					Cursor blankCursor = tk.createCustomCursor(image,
							new Point(0, 0), "BlackCursor");
					wwd.setCursor(blankCursor);
				}

				View v = wwd.getSceneController().getView();
				if (!(v instanceof RollOrbitView))
					return;
				RollOrbitView view = (RollOrbitView) v;
				boolean detectCollisions = view.isDetectCollisions();
				view.setDetectCollisions(false);

				int fps = 50;
				int frame = 0;

				double totalTime = path.getTime();
				double startTime = System.currentTimeMillis() / 1000d;
				double currentTime = 0;
				while (currentTime <= totalTime)
				{
					frame++;

					if (savingFrames)
					{
						currentTime = frame / (double) fps;
					}
					else
					{
						currentTime = System.currentTimeMillis() / 1000d
								- startTime;
					}

					LatLon latlon = path.getLatLon(currentTime);
					Zoom zoom = path.getZoom(currentTime);
					Heading heading = path.getHeading(currentTime);
					Pitch pitch = path.getPitch(currentTime);
					Roll roll = path.getRoll(currentTime);

					if (path.isEyePath())
					{
						view.setEye(latlon.getLatLon(), heading.getAngle(),
								pitch.getAngle(), roll.getAngle(), zoom
										.toCameraZoom());
					}
					else
					{
						view.setCenter(latlon.getLatLon(), heading.getAngle(),
								pitch.getAngle(), roll.getAngle(), zoom
										.toCameraZoom());
					}

					wwd.redrawNow();

					if (savingFrames)
					{
						takeScreenshot("frames/frame" + frame + ".tga");
					}

					//System.out.println(currentTime + " = " + position + " zoom = " + zoom);
				}

				view.setDetectCollisions(detectCollisions);

				Animator.this.frame.setAlwaysOnTop(false);
				wwd.setCursor(null);
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
				sleep();
				wwd.redraw();
			}
			else if (event.getStage() == RenderingEvent.BEFORE_BUFFER_SWAP)
			{
				wwd.removeRenderingListener(this);

				File out = new File(filename);
				if (!out.getParentFile().exists())
				{
					out.getParentFile().mkdirs();
				}
				try
				{
					Screenshot.writeToTargaFile(out, wwd.getWidth(), wwd
							.getHeight());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

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
}
