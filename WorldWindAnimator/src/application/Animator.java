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
import gov.nasa.worldwind.util.StatusBar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import layers.WestMacGlobe;
import path.Parameter;
import tessellator.ConfigurableTessellator;
import view.BasicRollOrbitView;
import view.RollOrbitView;

public class Animator
{
	static
	{
		if (Configuration.isMacOS())
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
	private FrameSlider slider;

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

		wwd.getSceneController().setVerticalExaggeration(5.0);

		JPanel bottom = new JPanel(new BorderLayout());
		frame.add(bottom, BorderLayout.SOUTH);

		slider = new FrameSlider(0, 0, 100);
		slider.setMinimumSize(new Dimension(0, 54));
		bottom.add(slider, BorderLayout.CENTER);

		StatusBar statusBar = new StatusBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		bottom.add(statusBar, BorderLayout.SOUTH);
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

	private path.CameraPath createTestPath()
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
		final path.CameraPath path = createTestPath();

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
