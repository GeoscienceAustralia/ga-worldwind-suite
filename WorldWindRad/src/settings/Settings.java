package settings;

import globes.GAGlobe;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.Earth;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings
{
	private static Settings instance;

	public static void initialize(String node)
	{
		instance = new Settings(node);
	}

	public static Settings get()
	{
		if (instance == null)
		{
			throw new IllegalStateException(
					"Must call Settings.initialize() first");
		}
		return instance;
	}

	public enum ProjectionMode
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
	}

	public enum StereoMode
	{
		STEREOBUFFER("Hardware stereo buffer"),
		RCANAGLYPH("Red/cyan anaglyph"),
		GMANAGLYPH("Green/magenta anaglyph"),
		BYANAGLYPH("Blue/yellow anaglyph");

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
	}

	private Preferences preferences;
	private boolean stereoSupported;

	private Settings(String node)
	{
		preferences = Preferences.userRoot().node(node);
		updateProxyConfiguration();
		
		//force configuration update
		setVerticalExaggeration(getVerticalExaggeration());
		setUseTerrain(isUseTerrain());
	}

	public boolean isProxyEnabled()
	{
		return preferences.getBoolean("ProxyEnabled", false);
	}

	public void setProxyEnabled(boolean proxyEnabled)
	{
		preferences.putBoolean("ProxyEnabled", proxyEnabled);
		updateProxyConfiguration();
	}

	public String getProxyHost()
	{
		return preferences.get("ProxyHost", "");
	}

	public void setProxyHost(String proxyHost)
	{
		preferences.put("ProxyHost", proxyHost);
		updateProxyConfiguration();
	}

	public int getProxyPort()
	{
		return preferences.getInt("ProxyPort", 80);
	}

	public void setProxyPort(int proxyPort)
	{
		preferences.putInt("ProxyPort", proxyPort);
		updateProxyConfiguration();
	}

	public String getNonProxyHosts()
	{
		return preferences.get("NonProxyHosts", "");
	}

	public void setNonProxyHosts(String nonProxyHosts)
	{
		preferences.put("NonProxyHosts", nonProxyHosts);
	}

	private void updateProxyConfiguration()
	{
		if (isProxyEnabled())
		{
			System.setProperty("http.proxyHost", getProxyHost());
			System
					.setProperty("http.proxyPort", String
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
		return StereoMode.values()[preferences.getInt("StereoMode",
				StereoMode.RCANAGLYPH.ordinal())];
	}

	public void setStereoMode(StereoMode stereoMode)
	{
		preferences.putInt("StereoMode", stereoMode.ordinal());
	}

	public boolean isStereoSwap()
	{
		return preferences.getBoolean("StereoSwap", false);
	}

	public void setStereoSwap(boolean stereoSwap)
	{
		preferences.putBoolean("StereoSwap", stereoSwap);
	}

	public boolean isStereoEnabled()
	{
		return preferences.getBoolean("StereoEnabled", false);
	}

	public void setStereoEnabled(boolean stereoEnabled)
	{
		preferences.putBoolean("StereoEnabled", stereoEnabled);
	}

	public ProjectionMode getProjectionMode()
	{
		return ProjectionMode.values()[preferences.getInt("ProjectionMode",
				ProjectionMode.SIMPLE_OFFSET.ordinal())];
	}

	public void setProjectionMode(ProjectionMode projectionMode)
	{
		preferences.putInt("ProjectionMode", projectionMode.ordinal());
	}

	public double getEyeSeparation()
	{
		return preferences.getDouble("EyeSeparation", 1);
	}

	public void setEyeSeparation(double eyeSeparation)
	{
		preferences.putDouble("EyeSeparation", eyeSeparation);
	}

	public double getFocalLength()
	{
		return preferences.getDouble("FocalLength", 100);
	}

	public void setFocalLength(double focalLength)
	{
		preferences.putDouble("FocalLength", focalLength);
	}

	public double getVerticalExaggeration()
	{
		return preferences.getDouble("VerticalExaggeration", 1);
	}

	public void setVerticalExaggeration(double verticalExaggeration)
	{
		preferences.putDouble("VerticalExaggeration", verticalExaggeration);
		Configuration.setValue(AVKey.VERTICAL_EXAGGERATION,
				verticalExaggeration);
	}

	public boolean isUseTerrain()
	{
		return preferences.getBoolean("UseTerrain", false);
	}

	public void setUseTerrain(boolean useTerrain)
	{
		preferences.putBoolean("UseTerrain", useTerrain);
		Configuration.setValue(AVKey.GLOBE_CLASS_NAME,
				useTerrain ? GAGlobe.class.getName() : Earth.class.getName());
	}

	public void save() throws BackingStoreException
	{
		preferences.flush();
	}

	public boolean isStereoSupported()
	{
		return stereoSupported;
	}

	public void setStereoSupported(boolean stereoSupported)
	{
		this.stereoSupported = stereoSupported;
	}
}
