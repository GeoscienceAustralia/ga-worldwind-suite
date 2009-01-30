package settings;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bookmarks.Bookmark;


public class Settings
{
	private final static String SETTINGS_FILENAME = ".gaww.xml";

	private static Settings instance;
	private Properties settings;
	private static boolean stereoSupported = false;

	static
	{
		instance = new Settings(load());
		instance.init();
	}

	public static Properties get()
	{
		return instance.settings;
	}

	private Settings(Properties settings)
	{
		this.settings = settings;
	}

	private static Properties load()
	{
		Properties settings = null;
		File file = getSettingsFile();
		if (file.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(file);
				XMLDecoder xmldec = new XMLDecoder(fis);
				settings = (Properties) xmldec.readObject();
			}
			catch (Exception e)
			{
				settings = null;
			}
		}
		if (settings == null)
			settings = new Properties();
		return settings;
	}

	private void init()
	{
		//force configuration update
		get().updateProxyConfiguration();
		get().setVerticalExaggeration(get().getVerticalExaggeration());
	}

	private static File getSettingsFile()
	{
		String home = System.getProperty("user.home");
		return new File(new File(home), SETTINGS_FILENAME);
	}

	public static void save()
	{
		try
		{
			File file = getSettingsFile();
			FileOutputStream fos = new FileOutputStream(file);
			XMLEncoder xmlenc = new XMLEncoder(fos);
			xmlenc.writeObject(instance.settings);
			xmlenc.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class Properties implements Serializable
	{
		private boolean proxyEnabled = false;
		private String proxyHost = "";
		private int proxyPort = 80;
		private String nonProxyHosts = "";
		private StereoMode stereoMode = StereoMode.RC_ANAGLYPH;
		private boolean stereoSwap = false;
		private String displayId = null;
		private double eyeSeparation = 1.0;
		private double focalLength = 100.0;
		private ProjectionMode projectionMode = ProjectionMode.ASYMMETRIC_FRUSTUM;
		private int[] splitLocations = null;
		private double verticalExaggeration = 1.0;
		private Rectangle windowBounds = null;
		private boolean spanDisplays = false;
		private boolean stereoCursor = false;
		private boolean stereoEnabled = false;
		private boolean windowMaximized = false;
		private List<Bookmark> bookmarks = new ArrayList<Bookmark>();

		public boolean isProxyEnabled()
		{
			return proxyEnabled;
		}

		public void setProxyEnabled(boolean proxyEnabled)
		{
			this.proxyEnabled = proxyEnabled;
			updateProxyConfiguration();
		}

		public String getProxyHost()
		{
			return proxyHost;
		}

		public void setProxyHost(String proxyHost)
		{
			if (proxyHost == null)
				proxyHost = "";
			this.proxyHost = proxyHost;
			updateProxyConfiguration();
		}

		public int getProxyPort()
		{
			return proxyPort;
		}

		public void setProxyPort(int proxyPort)
		{
			if (proxyPort <= 0)
				proxyPort = 80;
			this.proxyPort = proxyPort;
			updateProxyConfiguration();
		}

		public String getNonProxyHosts()
		{
			return nonProxyHosts;
		}

		public void setNonProxyHosts(String nonProxyHosts)
		{
			if (nonProxyHosts == null)
				nonProxyHosts = "";
			this.nonProxyHosts = nonProxyHosts;
			updateProxyConfiguration();
		}

		private void updateProxyConfiguration()
		{
			if (isProxyEnabled())
			{
				System.setProperty("http.proxyHost", getProxyHost());
				System.setProperty("http.proxyPort", String
						.valueOf(getProxyPort()));
				System.setProperty("http.nonProxyHosts", getNonProxyHosts());
			}
			else
			{
				System.setProperty("http.proxyHost", "");
				System.setProperty("http.proxyPort", "80");
				System.setProperty("http.nonProxyHosts", "");
			}
		}

		public StereoMode getStereoMode()
		{
			return stereoMode;
		}

		public void setStereoMode(StereoMode stereoMode)
		{
			if (stereoMode == null)
				stereoMode = StereoMode.RC_ANAGLYPH;
			this.stereoMode = stereoMode;
		}

		public boolean isStereoSwap()
		{
			return stereoSwap;
		}

		public void setStereoSwap(boolean stereoSwap)
		{
			this.stereoSwap = stereoSwap;
		}

		public boolean isStereoEnabled()
		{
			return stereoEnabled;
		}

		public void setStereoEnabled(boolean stereoEnabled)
		{
			this.stereoEnabled = stereoEnabled;
		}

		public ProjectionMode getProjectionMode()
		{
			return projectionMode;
		}

		public void setProjectionMode(ProjectionMode projectionMode)
		{
			if (projectionMode == null)
				projectionMode = ProjectionMode.ASYMMETRIC_FRUSTUM;
			this.projectionMode = projectionMode;
		}

		public double getEyeSeparation()
		{
			return eyeSeparation;
		}

		public void setEyeSeparation(double eyeSeparation)
		{
			this.eyeSeparation = eyeSeparation;
		}

		public double getFocalLength()
		{
			return focalLength;
		}

		public void setFocalLength(double focalLength)
		{
			this.focalLength = focalLength;
		}

		public boolean isStereoCursor()
		{
			return stereoCursor;
		}

		public void setStereoCursor(boolean stereoCursor)
		{
			this.stereoCursor = stereoCursor;
		}

		public double getVerticalExaggeration()
		{
			return verticalExaggeration;
		}

		public void setVerticalExaggeration(double verticalExaggeration)
		{
			if (verticalExaggeration < 0)
				verticalExaggeration = 1.0;
			this.verticalExaggeration = verticalExaggeration;
			Configuration.setValue(AVKey.VERTICAL_EXAGGERATION,
					verticalExaggeration);
		}

		public Rectangle getWindowBounds()
		{
			checkWindowBounds();
			return windowBounds;
		}

		public void setWindowBounds(Rectangle windowBounds)
		{
			this.windowBounds = windowBounds;
			checkWindowBounds();
		}

		private void checkWindowBounds()
		{
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();

			if (windowBounds == null)
			{
				Point center = ge.getCenterPoint();
				windowBounds = new Rectangle(center.x - 400, center.y - 300,
						800, 600);
			}

			Rectangle maximumBounds = ge.getMaximumWindowBounds();
			if (!maximumBounds.contains(windowBounds))
			{
				windowBounds = new Rectangle(maximumBounds.x, maximumBounds.y,
						windowBounds.width, windowBounds.height);
				if (!maximumBounds.contains(windowBounds))
				{
					windowBounds = new Rectangle(maximumBounds.x + 10,
							maximumBounds.y + 10, maximumBounds.width - 20,
							maximumBounds.height - 20);
				}
			}
		}

		public int[] getSplitLocations()
		{
			return splitLocations;
		}

		public void setSplitLocations(int[] splitLocations)
		{
			this.splitLocations = splitLocations;
		}

		public boolean isWindowMaximized()
		{
			return windowMaximized;
		}

		public void setWindowMaximized(boolean windowMaximized)
		{
			this.windowMaximized = windowMaximized;
		}

		public boolean isSpanDisplays()
		{
			return spanDisplays;
		}

		public void setSpanDisplays(boolean spanDisplays)
		{
			this.spanDisplays = spanDisplays;
		}

		public String getDisplayId()
		{
			return displayId;
		}

		public void setDisplayId(String displayId)
		{
			this.displayId = displayId;
		}

		public List<Bookmark> getBookmarks()
		{
			return bookmarks;
		}

		public void setBookmarks(List<Bookmark> bookmarks)
		{
			if (bookmarks == null)
				bookmarks = new ArrayList<Bookmark>();
			this.bookmarks = bookmarks;
		}
	}

	public static boolean isStereoSupported()
	{
		return stereoSupported;
	}

	public static void setStereoSupported(boolean stereoSupported)
	{
		Settings.stereoSupported = stereoSupported;
	}

	public enum ProjectionMode implements Serializable
	{
		SIMPLE_OFFSET("Simple offset"),
		ASYMMETRIC_FRUSTUM("Asymmetric frustum");

		private String pretty;

		ProjectionMode(String pretty)
		{
			this.pretty = pretty;
		}

		@Override
		public String toString()
		{
			return pretty;
		}

		static
		{
			EnumPersistenceDelegate.installFor(values());
		}
	}

	public enum StereoMode implements Serializable
	{
		STEREO_BUFFER("Hardware stereo buffer"),
		RC_ANAGLYPH("Red/cyan anaglyph"),
		GM_ANAGLYPH("Green/magenta anaglyph"),
		BY_ANAGLYPH("Blue/yellow anaglyph");

		private String pretty;

		StereoMode(String pretty)
		{
			this.pretty = pretty;
		}

		@Override
		public String toString()
		{
			return pretty;
		}

		static
		{
			EnumPersistenceDelegate.installFor(values());
		}
	}
}
