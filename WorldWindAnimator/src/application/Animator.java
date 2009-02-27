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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.render.Polyline;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import layers.WestMacGlobe;
import path.Parameter;
import tessellator.ConfigurableTessellator;
import view.BasicRollOrbitView;
import view.RollOrbitView;
import camera.CameraPath;
import camera.motion.MotionParams;
import camera.params.Heading;
import camera.params.LatLon;
import camera.params.Latitude;
import camera.params.Longitude;
import camera.params.Pitch;
import camera.params.Roll;
import camera.params.Zoom;

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
	private LineBuilder lineBuilder;

	public Animator()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicRollOrbitView.class
				.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME,
				ConfigurableTessellator.class.getName());
		Configuration.setValue(AVKey.GLOBE_CLASS_NAME, WestMacGlobe.class
				.getName());

		frame = new JFrame("World Wind");

		frame.setLayout(new BorderLayout());
		wwd = new WorldWindowGLCanvas();
		Model model = new BasicModel();
		/*((ConfigurableTessellator) model.getGlobe().getTessellator())
				.setLog10ResolutionTarget(2.2);*/
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
		lineBuilder = new LineBuilder(wwd, null, null);

		wwd.getSceneController().setVerticalExaggeration(5.0);

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

		button = new JButton("Print eye position");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				View view = wwd.getView();
				System.out.println(view.getEyePosition());
			}
		});

		button = new JButton("New line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				wwd.getModel().getLayers().remove(lineBuilder.getLayer());
				lineBuilder.setArmed(false);
				lineBuilder = new LineBuilder(wwd, null, null);
				lineBuilder.setArmed(true);
			}
		});

		button = new JButton("Pause line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lineBuilder.setArmed(false);
			}
		});

		button = new JButton("Resume line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				lineBuilder.setArmed(true);
			}
		});

		button = new JButton("Print line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Polyline line = lineBuilder.getLine();
				for (Position position : line.getPositions())
				{
					System.out.println(position);
				}
			}
		});

		button = new JButton("Save line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Polyline line = lineBuilder.getLine();
				File file = new File("line.xml");
				saveStringToFile(file, line.getRestorableState());
			}
		});

		button = new JButton("Load line");
		left.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File file = new File("line.xml");
				String str = readFileAsString(file);
				Polyline line = new Polyline();
				line.restoreState(str);

				wwd.getModel().getLayers().remove(lineBuilder.getLayer());
				lineBuilder.setArmed(false);
				lineBuilder = new LineBuilder(wwd, null, line);
			}
		});

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

	private static void saveStringToFile(File file, String str)
	{
		try
		{
			FileWriter writer = new FileWriter(file);
			writer.append(str);
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String readFileAsString(File file)
	{
		try
		{
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1)
			{
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			return fileData.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
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
		LatLon l0 = LatLon.fromDegrees(0, 360);
		LatLon l1 = LatLon.fromDegrees(-27, 133.5);
		LatLon l2 = LatLon.fromDegrees(-21.0474, 119.6);

		LatLon l21 = LatLon.fromDegrees(-21.0474, 118.6);
		LatLon l22 = LatLon.fromDegrees(-21.0474, 120.6);

		LatLon l3 = LatLon.fromDegrees(-27, 133.5);

		Zoom zoom0 = Zoom.fromCameraZoom(20000000);
		Zoom zoom1 = Zoom.fromCameraZoom(12000000);
		Zoom zoom2 = Zoom.fromCameraZoom(500000);
		Zoom zoom21 = Zoom.fromCameraZoom(50000);
		Zoom zoom22 = Zoom.fromCameraZoom(800000);
		Zoom zoom3 = Zoom.fromCameraZoom(12000000);

		Heading heading1 = Heading.fromDegrees(0);
		Heading heading2 = Heading.fromDegrees(-270);
		Heading heading21 = Heading.fromDegrees(-300);
		Heading heading3 = Heading.fromDegrees(-380);
		Heading heading4 = Heading.fromDegrees(-360);

		Pitch pitch1 = Pitch.fromDegrees(0);
		Pitch pitch2 = Pitch.fromDegrees(70);
		Pitch pitch3 = Pitch.fromDegrees(50);
		Pitch pitch31 = Pitch.fromDegrees(80);
		Pitch pitch4 = Pitch.fromDegrees(0);

		Roll roll1 = Roll.fromDegrees(0);

		double time0 = 6;
		double time1 = 18;
		double time2 = 24;

		double time21 = 28;
		double time22 = 40;

		double time3 = 44;

		double endtime = 50;

		//MotionParams centerMotion = MotionParams.standard(8, 2.5, 0, 0);
		MotionParams centerMotion = MotionParams.calculateAcceleration(0, 0);
		MotionParams zoomMotion = MotionParams.calculateAcceleration(0, 0);
		MotionParams headingMotion = MotionParams.calculateAcceleration(0, 0);
		MotionParams pitchMotion = MotionParams.calculateAcceleration(0, 0);

		CameraPath path = new CameraPath(l0, l0, zoom0, heading1, pitch1,
				roll1, false);

		path.addLatLon(l1, l1, l1, time0 + 3, centerMotion);
		path.addZoom(zoom1, time0, zoomMotion);

		path.addLatLon(l2, l2, l2, time1, centerMotion);
		path.addZoom(zoom2, time1, zoomMotion);
		path.addLatLon(l2, l2, l2, time2, centerMotion);
		path.addZoom(zoom2, time2, zoomMotion);
		path.addLatLon(l21, l21, l21, time21, centerMotion);
		path.addZoom(zoom21, time21 + 2, zoomMotion);
		path.addLatLon(l22, l22, l22, time22, centerMotion);
		path.addZoom(zoom21, time22, zoomMotion);
		path.addZoom(zoom22, time3, zoomMotion);


		path.addHeading(heading1, time0 + 3, headingMotion);
		path.addHeading(heading2, time2 + 2, headingMotion);
		path.addHeading(heading2, time22 - 2, headingMotion);
		path.addHeading(heading3, time3 - 2, headingMotion);


		path.addPitch(pitch1, time0, pitchMotion);
		path.addPitch(pitch2, time0 + (time1 - time0) / 2, pitchMotion);
		path.addPitch(pitch3, time1, pitchMotion);
		path.addPitch(pitch3, time2, pitchMotion);
		path.addPitch(pitch31, time21, pitchMotion);
		path.addPitch(pitch31, time22, pitchMotion);
		path.addPitch(pitch3, time3, pitchMotion);


		path.addLatLon(l3, l3, l3, endtime, centerMotion);
		path.addZoom(zoom3, endtime, zoomMotion);
		path.addHeading(heading4, endtime, headingMotion);
		path.addPitch(pitch4, endtime, pitchMotion);

		//path.setStartOffset(time2 - 2);

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
		MotionParams motion = MotionParams.constantVelocity();
		//MotionParams headingsMotion = new MotionParams(10000000, 10000000, 0, 0);
		//MotionParams motion = MotionParams.calculateAccelerationUsePrevious(0);
		//MotionParams headingsMotion = MotionParams.calculateAcceleration(0, 0);
		MotionParams headingsMotion = MotionParams.constantVelocity();


		Pitch pitch2 = Pitch.fromDegrees(70);
		Zoom zoom2 = Zoom.fromCameraZoom(2500);

		List<LatLon> positions = new ArrayList<LatLon>();

		/*LatLon l2 = LatLon.fromDegrees(37.09, -111.26);
		LatLon l3 = LatLon.fromDegrees(36.945, -111.46);
		LatLon l4 = LatLon.fromDegrees(36.875, -111.56);
		LatLon l5 = LatLon.fromDegrees(36.85, -111.61);
		LatLon l6 = LatLon.fromDegrees(36.64, -111.76);*/

		/*positions.add(LatLon.fromDegrees(37, 111));
		positions.add(LatLon.fromDegrees(37, 112));
		positions.add(LatLon.fromDegrees(38, 112));
		positions.add(LatLon.fromDegrees(38, 111));
		positions.add(LatLon.fromDegrees(37, 111));*/

		File file = new File("line.xml");
		String str = readFileAsString(file);
		Polyline line = new Polyline();
		line.restoreState(str);

		for (Position position : line.getPositions())
		{
			positions.add(LatLon.fromLatLon(position.getLatLon()));
		}

		Heading[] headings = new Heading[positions.size()];
		double[] times = new double[positions.size()];
		LatLon[] ins = new LatLon[positions.size()];
		LatLon[] outs = new LatLon[positions.size()];
		Roll[] rolls = new Roll[positions.size()];

		//Heading heading2 = Heading.fromDegrees(l2.angleBetween(l6) + 90d);

		//heading is angle between this point and next point
		//time is (distance between this point and last point) * constant

		LatLon current, last = null, next = null;
		times[0] = time1;
		for (int i = 0; i < positions.size(); i++)
		{
			current = positions.get(i);

			if (i < positions.size() - 1)
			{
				next = positions.get(i + 1);
				headings[i] = Heading
						.fromDegrees(current.angleBetween(next) + 90d);
				outs[i] = LatLon.interpolate(current, next, 0.5);
			}
			else
			{
				//last
				headings[i] = headings[i - 1];
				outs[i] = positions.get(i);
			}

			if (i > 0)
			{
				last = positions.get(i - 1);
				times[i] = current.distance(last) * 100 + times[i - 1];
				ins[i] = LatLon.interpolate(last, current, 0.5);
				rolls[i] = Roll.fromDegrees(Heading.difference(headings[i - 1],
						headings[i]));
			}
			else
			{
				//first
				times[i] = time1;
				ins[i] = positions.get(i);
				rolls[i] = Roll.fromDegrees(0);
			}

			if (i > 0 && i < positions.size() - 1)
			{
				double latdiff = Latitude.difference(last.latitude,
						next.latitude);
				double londiff = Longitude.difference(last.longitude,
						next.longitude);

				double alpha = 0.5;
				LatLon in = LatLon.fromDegrees(current.latitude.degrees + alpha
						* latdiff, current.longitude.degrees + alpha * londiff);
				LatLon out = LatLon.fromDegrees(current.latitude.degrees
						- alpha * latdiff, current.longitude.degrees - alpha
						* londiff);
				ins[i] = out;
				outs[i] = in;
			}
			last = current;
		}

		CameraPath path = new CameraPath(l1, l1, zoom1, headings[0], pitch1,
				roll1, true);
		path.addZoom(zoom2, time1, motion);
		path.addPitch(pitch2, time1, motion);

		for (int i = 0; i < positions.size(); i++)
		{
			path.addLatLon(positions.get(i), ins[i], outs[i], times[i], motion);
			path.addHeading(headings[i], times[i], headingsMotion);
			//path.addRoll(rolls[i], times[i], headingsMotion);
		}

		startOffset = times[10];

		//path.addPitch(Pitch.fromDegrees(100), times[times.length - 1], motion);

		//path.addHeading(heading2, time1, motion);

		path.setStartOffset(startOffset);
		path.setEndOffset(endOffset);
		path.refresh();
		return path;
	}

	private path.CameraPath createWestMacPath()
	{
		Parameter eyeLat = new Parameter();
		Parameter eyeLon = new Parameter();
		Parameter eyeZoom = new Parameter();
		Parameter targetLat = new Parameter();
		Parameter targetLon = new Parameter();
		Parameter targetZoom = new Parameter();

		eyeLat.addKey(0, -23);
		eyeLat.addKey(100, -50);
		
		eyeLon.addKey(0, 131);
		eyeLon.addKey(200, 160);
		
		eyeZoom.addKey(0, path.CameraPath.c2z(600000));
		eyeZoom.addKey(150, path.CameraPath.c2z(1200000));
		
		targetLat.addKey(0, -23);
		targetLat.addKey(100, -23);
		targetLat.addKey(200, -47);
		
		targetLon.addKey(0, 131);
		targetLon.addKey(100, 131);
		targetLon.addKey(150, 150);
		
		targetZoom.addKey(0, 0);

		return new path.CameraPath(eyeLat, eyeLon, eyeZoom, targetLat,
				targetLon, targetZoom);
	}

	private void animate(final boolean savingFrames)
	{
		final path.CameraPath path = createWestMacPath();

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

				int firstFrame = path.getFirstFrame();
				int lastFrame = path.getLastFrame();
				for (int frame = firstFrame; frame <= lastFrame; frame++)
				{
					path.applyFrame(view, frame);
					wwd.redrawNow();

					if (savingFrames)
					{
						takeScreenshot("frames/frame" + frame + ".tga");
					}
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
					com.sun.opengl.util.Screenshot.writeToTargaFile(out, wwd
							.getWidth(), wwd.getHeight());
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
