package au.gov.ga.worldwind.viewer.settings;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.util.EnumPersistenceDelegate;
import au.gov.ga.worldwind.common.util.Proxy;
import au.gov.ga.worldwind.common.view.stereo.StereoViewParameters;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemeHUD;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

public class Settings implements StereoViewParameters
{
	private static Settings instance;
	private static boolean stereoSupported = false;

	public static void init()
	{
		instance = SettingsPersistance.load(getSettingsFile());
		if (instance == null)
		{
			instance = new Settings();
		}
	}

	public static Settings get()
	{
		if (instance == null)
		{
			throw new IllegalStateException(
					"Settings have not yet been initialised; must call init() first");
		}
		return instance;
	}
	
	public static void set(Settings newSettings)
	{
		instance = newSettings;
	}

	public static File getSettingsFile()
	{
		return SettingsUtil.getSettingsFile("settings.xml");
	}

	public static boolean isStereoSupported()
	{
		return stereoSupported;
	}

	public static void setStereoSupported(boolean stereoSupported)
	{
		Settings.stereoSupported = stereoSupported;
		if (!stereoSupported)
		{
			get().setHardwareStereoEnabled(false);
		}
	}

	public Settings()
	{
		//force configuration update
		updateProxyConfiguration();
		setVerticalExaggeration(getVerticalExaggeration());
	}

	public void save()
	{
		SettingsPersistance.save(this, getSettingsFile());
	}

	private Proxy proxy = new Proxy();
	private StereoMode stereoMode = StereoMode.RC_ANAGLYPH;
	private boolean stereoSwap = false;
	private String displayId = null;
	private double eyeSeparation = 1.0;
	private double eyeSeparationMultiplier = 1.0;
	private double focalLength = 100.0;
	private boolean dynamicStereo = true;
	private double verticalExaggeration = 1.0;
	private Rectangle windowBounds = null;
	private boolean spanDisplays = false;
	private boolean stereoCursor = false;
	private boolean stereoEnabled = false;
	private boolean hardwareStereoEnabled = false;
	private boolean windowMaximized = false;
	private double viewIteratorSpeed = 1.0;
	private boolean showDownloads = true;
	private int placesPause = 1000;
	private double fieldOfView = 45.0;

	private int splitLocation = 300;
	private List<ThemePanelProperties> panelProperties = new ArrayList<ThemePanelProperties>();
	private List<ThemeHUDProperties> hudProperties = new ArrayList<ThemeHUDProperties>();

	public Proxy getProxy()
	{
		return proxy;
	}

	public void setProxy(Proxy proxy)
	{
		this.proxy = proxy;
		updateProxyConfiguration();
	}

	private void updateProxyConfiguration()
	{
		proxy.set();
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

	public boolean isHardwareStereoEnabled()
	{
		return hardwareStereoEnabled;
	}

	public void setHardwareStereoEnabled(boolean hardwareStereoEnabled)
	{
		this.hardwareStereoEnabled = hardwareStereoEnabled;
	}

	@Override
	public double getEyeSeparation()
	{
		return eyeSeparation;
	}

	@Override
	public void setEyeSeparation(double eyeSeparation)
	{
		this.eyeSeparation = eyeSeparation;
	}

	@Override
	public double getEyeSeparationMultiplier()
	{
		return eyeSeparationMultiplier;
	}

	@Override
	public void setEyeSeparationMultiplier(double eyeSeparationMultiplier)
	{
		this.eyeSeparationMultiplier = eyeSeparationMultiplier;
	}

	@Override
	public double getFocalLength()
	{
		return focalLength;
	}

	@Override
	public void setFocalLength(double focalLength)
	{
		this.focalLength = focalLength;
	}

	@Override
	public boolean isDynamicStereo()
	{
		return dynamicStereo;
	}

	@Override
	public void setDynamicStereo(boolean dynamicStereo)
	{
		this.dynamicStereo = dynamicStereo;
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
		Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, verticalExaggeration);
	}

	public Rectangle getWindowBounds()
	{
		windowBounds = checkWindowBounds(windowBounds);
		return windowBounds;
	}

	public void setWindowBounds(Rectangle windowBounds)
	{
		this.windowBounds = checkWindowBounds(windowBounds);
	}

	private Rectangle checkWindowBounds(Rectangle windowBounds)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		if (windowBounds == null)
		{
			Point center = ge.getCenterPoint();
			windowBounds = new Rectangle(center.x - 400, center.y - 300, 800, 600);
		}
		else
		{
			windowBounds = new Rectangle(windowBounds);
		}

		Rectangle maximumBounds = new Rectangle();
		for (GraphicsDevice gd : ge.getScreenDevices())
		{
			maximumBounds = maximumBounds.union(gd.getDefaultConfiguration().getBounds());
		}

		if (!maximumBounds.contains(windowBounds) && windowBounds.width <= maximumBounds.width
				&& windowBounds.height <= maximumBounds.height)
		{
			if (windowBounds.x < maximumBounds.x)
				windowBounds.x = maximumBounds.x;
			if (windowBounds.y < maximumBounds.y)
				windowBounds.y = maximumBounds.y;
			if (windowBounds.x + windowBounds.width > maximumBounds.x + maximumBounds.width)
				windowBounds.x = maximumBounds.x + maximumBounds.width - windowBounds.width;
			if (windowBounds.y + windowBounds.height > maximumBounds.y + maximumBounds.height)
				windowBounds.y = maximumBounds.y + maximumBounds.height - windowBounds.height;
		}

		if (!maximumBounds.contains(windowBounds))
		{
			windowBounds = maximumBounds;
		}

		return windowBounds;
	}

	public int getSplitLocation()
	{
		return splitLocation;
	}

	public void setSplitLocation(int splitLocation)
	{
		this.splitLocation = splitLocation;
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

	public double getViewIteratorSpeed()
	{
		return viewIteratorSpeed;
	}

	public void setViewIteratorSpeed(double viewIteratorSpeed)
	{
		this.viewIteratorSpeed = viewIteratorSpeed;
	}

	public boolean isShowDownloads()
	{
		return showDownloads;
	}

	public void setShowDownloads(boolean showDownloads)
	{
		this.showDownloads = showDownloads;
	}

	public int getPlacesPause()
	{
		return placesPause;
	}

	public void setPlacesPause(int annotationsPause)
	{
		this.placesPause = annotationsPause;
	}

	public double getFieldOfView()
	{
		return fieldOfView;
	}

	public void setFieldOfView(double fieldOfView)
	{
		if (fieldOfView <= 0d)
			fieldOfView = 1d;
		else if (fieldOfView > 180)
			fieldOfView = 180;
		this.fieldOfView = fieldOfView;
	}

	public List<ThemePanelProperties> getPanelProperties()
	{
		return panelProperties;
	}

	public void setPanelProperties(List<ThemePanelProperties> panelProperties)
	{
		if (panelProperties == null)
			panelProperties = new ArrayList<ThemePanelProperties>();
		this.panelProperties = removeInvalidClassNames(panelProperties);
	}

	public List<ThemeHUDProperties> getHudProperties()
	{
		return hudProperties;
	}

	public void setHudProperties(List<ThemeHUDProperties> hudProperties)
	{
		if (hudProperties == null)
			hudProperties = new ArrayList<ThemeHUDProperties>();
		this.hudProperties = removeInvalidClassNames(hudProperties);
	}

	private static <E extends ThemeClassProperties> List<E> removeInvalidClassNames(List<E> list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			E e = list.get(i);
			if (e == null || e.getClassName() == null || !classExists(e.getClassName()))
				list.remove(i--);
		}
		return list;
	}

	private static boolean classExists(String className)
	{
		try
		{
			Class.forName(className);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public void loadThemeProperties(Theme theme)
	{
		for (ThemePanel panel : theme.getPanels())
		{
			for (ThemePanelProperties pp : getPanelProperties())
			{
				if (pp.getClassName().equals(panel.getClass().getName()))
				{
					panel.setExpanded(pp.isExpanded());
					panel.setWeight(pp.getWeight());
					if (theme.hasMenuBar())
						panel.setOn(pp.isEnabled());
					break;
				}
			}
		}

		for (ThemeHUD hud : theme.getHUDs())
		{
			for (ThemeHUDProperties hp : getHudProperties())
			{
				if (hp.getClassName().equals(hud.getClass().getName()))
				{
					if (theme.hasMenuBar())
						hud.setOn(hp.isEnabled());
					break;
				}
			}
		}

		boolean settingsAvailable = theme.hasMenuBar() || theme.hasToolBar();

		if (theme.getVerticalExaggeration() != null)
			setVerticalExaggeration(theme.getVerticalExaggeration());
		else if (!settingsAvailable)
			setVerticalExaggeration(1d);

		if (theme.getFieldOfView() != null)
			setFieldOfView(theme.getFieldOfView());
		else if (!settingsAvailable)
			setFieldOfView(45d);
	}

	public void saveThemeProperties(Theme theme)
	{
		for (ThemePanel panel : theme.getPanels())
		{
			ThemePanelProperties prop = null;
			for (ThemePanelProperties pp : getPanelProperties())
			{
				if (pp.getClassName().equals(panel.getClass().getName()))
				{
					prop = pp;
					break;
				}
			}
			if (prop == null)
			{
				prop = new ThemePanelProperties();
				getPanelProperties().add(prop);
			}
			prop.setClassName(panel.getClass().getName());
			prop.setExpanded(panel.isExpanded());
			prop.setWeight(panel.getWeight());
			if (theme.hasMenuBar())
				prop.setEnabled(panel.isOn());
		}

		for (ThemeHUD hud : theme.getHUDs())
		{
			ThemeHUDProperties prop = null;
			for (ThemeHUDProperties hp : getHudProperties())
			{
				if (hp.getClassName().equals(hud.getClass().getName()))
				{
					prop = hp;
					break;
				}
			}
			if (prop == null)
			{
				prop = new ThemeHUDProperties();
				getHudProperties().add(prop);
			}
			prop.setClassName(hud.getClass().getName());
			if (theme.hasMenuBar())
				prop.setEnabled(hud.isOn());
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

	public static abstract class ThemeClassProperties
	{
		private String className;
		private boolean enabled;

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	}

	public static class ThemePanelProperties extends ThemeClassProperties
	{
		private boolean expanded;
		private float weight;

		public boolean isExpanded()
		{
			return expanded;
		}

		public void setExpanded(boolean expanded)
		{
			this.expanded = expanded;
		}

		public float getWeight()
		{
			return weight;
		}

		public void setWeight(float weight)
		{
			this.weight = weight;
		}
	}

	public static class ThemeHUDProperties extends ThemeClassProperties
	{
	}
}
