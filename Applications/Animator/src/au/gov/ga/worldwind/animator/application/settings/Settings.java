package au.gov.ga.worldwind.animator.application.settings;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.util.Util;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A class that holds global settings for the Animator application.
 * <p/>
 * These settings can be persisted between executions of the application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Settings
{

	/** The settings folder name to use */
	private static final String SETTINGS_FOLDER_NAME = ".gaww";
	
	/** The settings file name to use */
	private static final String SETTINGS_FILE_NAME = "animatorSettings.xml";
	
	/** The Singleton Settings instance */
	private static Settings instance;
	
	/**
	 * Gets the Singleton instance of the global application {@link Settings}.
	 * <p/>
	 * If one has not yet been loaded, will lazy-load the settings from the configured location.
	 * 
	 * @return The current global settings
	 */
	public static Settings get()
	{
		if (instance == null)
		{
			loadSettings();
		}
		return instance;
	}
	
	/**
	 * Persists the settings to the configured xml file location
	 */
	public static void save()
	{
		if (instance == null)
		{
			return;
		}
		
		try
		{
			Document document = WWXML.createDocumentBuilder(false).newDocument();
		
			Element rootElement = document.createElement("animationSettings");
			document.appendChild(rootElement);
			
			saveLastUsedLocation(rootElement);
			saveRecentFilesList(rootElement);
			saveSplitLocation(rootElement);
			saveDefaultAnimationLayerUrls(rootElement);
			
			XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(new File(getUserDirectory(), SETTINGS_FILE_NAME)));
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
	}

	private static void saveDefaultAnimationLayerUrls(Element rootElement)
	{
		Element animationLayersContainer = WWXML.appendElement(rootElement, "defaultLayers");
		List<String> layersList = instance.getDefaultAnimationLayerUrls();
		for (int i = 0; i < layersList.size(); i++)
		{
			Element layerElement = WWXML.appendElement(animationLayersContainer, "layer");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(animationLayersContainer, "url", layersList.get(i));
		}
	}

	private static void saveSplitLocation(Element rootElement)
	{
		Element splitLocationElement = WWXML.appendElement(rootElement, "splitLocation");
		WWXML.setIntegerAttribute(splitLocationElement, "value", instance.getSplitLocation());
	}

	private static void saveRecentFilesList(Element rootElement)
	{
		Element recentFilesContainer = WWXML.appendElement(rootElement, "recentFiles");
		List<File> recentFiles = instance.getRecentFiles();
		for (int i = 0; i < recentFiles.size(); i++)
		{
			Element recentFileElement = WWXML.appendElement(recentFilesContainer, "file");
			WWXML.setIntegerAttribute(recentFileElement, "index", i);
			WWXML.setTextAttribute(recentFileElement, "path", recentFiles.get(i).getAbsolutePath());
		}
	}

	private static void saveLastUsedLocation(Element rootElement)
	{
		File lastUsedLocation = instance.getLastUsedLocation();
		if (lastUsedLocation != null)
		{
			WWXML.appendText(rootElement, "lastUsedLocation", lastUsedLocation.getAbsolutePath());
		}
	}
	
	/**
	 * Load the settings instance from the persisted xml file
	 */
	private static void loadSettings()
	{
		instance = new Settings();
		
		// If no file is detected, continue with the vanilla instance file
		File settingsFile = new File(getUserDirectory(), SETTINGS_FILE_NAME);
		if (!settingsFile.exists())
		{
			return;
		}
		
		// Otherwise load the settings from the file
		Document xmlDocument = WWXML.openDocument(settingsFile);
		Element rootElement = xmlDocument.getDocumentElement();
		
		loadLastUsedLocation(rootElement);
		loadRecentFilesList(rootElement);
		loadSplitLocation(rootElement);
		loadDefaultAnimationLayerUrls(rootElement);
	}

	private static void loadDefaultAnimationLayerUrls(Element rootElement)
	{
		List<String> loadedUrls = new ArrayList<String>();
		Integer layerCount = WWXML.getInteger(rootElement, "count(//defaultLayers/layer)", null);
		for (int i = layerCount - 1; i >= 0; i--)
		{
			String layerUrl = WWXML.getText(rootElement, "//defaultLayers/layer[@index='" + i + "']/@url");
			if (!Util.isBlank(layerUrl))
			{
				loadedUrls.add(layerUrl);
			}
		}
		if (!loadedUrls.isEmpty())
		{
			instance.setDefaultAnimationLayerUrls(loadedUrls);
		}
	}

	private static void loadLastUsedLocation(Element rootElement)
	{
		String lastUsedLocationPath = WWXML.getText(rootElement, "//lastUsedLocation");
		if (!Util.isBlank(lastUsedLocationPath))
		{
			instance.setLastUsedLocation(new File(lastUsedLocationPath));
		}
	}

	private static void loadRecentFilesList(Element rootElement)
	{
		for (int i = MAX_NUMBER_RECENT_FILES - 1; i >= 0; i--)
		{
			String recentFileName = WWXML.getText(rootElement, "//recentFiles/file[@index='" + i + "']/@path");
			if (!Util.isBlank(recentFileName))
			{
				instance.addRecentFile(new File(recentFileName));
			}
		}
	}

	private static void loadSplitLocation(Element rootElement)
	{
		Integer splitLocation = WWXML.getInteger(rootElement, "//splitLocation/@value", null);
		if (splitLocation != null)
		{
			instance.setSplitLocation(splitLocation);
		}
	}
	
	/**
	 * @return The user's home directory
	 */
	private static File getUserDirectory()
	{
		String home = System.getProperty("user.home");
		File homeDir = new File(home);
		File dir = new File(homeDir, SETTINGS_FOLDER_NAME);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	// ----------------------------------
	// Instance members
	// ----------------------------------
	
	/** The maximum number of recent files to remember */
	public static final int MAX_NUMBER_RECENT_FILES = 5;
	
	/** The last used location (i.e the last location used to open or save an animation file) */
	private File lastUsedLocation = null;
	
	/** The list of recently used files */
	private List<File> recentFiles = new ArrayList<File>(MAX_NUMBER_RECENT_FILES);
	
	/** The location of the split in the split pane */
	private int splitLocation = 300;
	
	/** The default set of animation layers to include in new animations */
	private List<String> defaultAnimationLayerUrls = new ArrayList<String>(Arrays.asList(new String[]{
			"file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/stars.xml",
			"file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/sky.xml",
			"file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/blue_marble.xml",
			"file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/landsat.xml",
	}));
	
	/**
	 * Private constructor. Use the Singleton accessor {@link #get()}.
	 */
	private Settings(){};
	
	/**
	 * @return The last used location. If not <code>null</code>, will always be a directory location.
	 */
	public File getLastUsedLocation()
	{
		return lastUsedLocation;
	}
	
	/**
	 * @param lastUsedLocation the location of the last opened or saved file
	 */
	public void setLastUsedLocation(File lastUsedLocation)
	{
		if (lastUsedLocation == null)
		{
			return;
		}
		
		if (lastUsedLocation.isDirectory())
		{
			this.lastUsedLocation = lastUsedLocation;
			return;
		}
		
		this.lastUsedLocation = lastUsedLocation.getParentFile();
	}
	
	/**
	 * @return An unmodifiable view on the list of recent files, ordered most recent to least recent.
	 */
	public List<File> getRecentFiles()
	{
		return Collections.unmodifiableList(recentFiles);
	}
	
	/**
	 * Add the provided recent file to the list of recent files as the most recently used file.
	 * 
	 * @param recentFile The file to add
	 */
	public void addRecentFile(File recentFile)
	{
		if (recentFile == null || recentFiles.contains(recentFile))
		{
			return;
		}
		
		if (recentFiles.size() == MAX_NUMBER_RECENT_FILES)
		{
			recentFiles.remove(recentFiles.size());
		}
		
		recentFiles.add(0, recentFile);
	}

	/**
	 * @return the splitLocation
	 */
	public int getSplitLocation()
	{
		return splitLocation;
	}

	/**
	 * @param splitLocation the splitLocation to set
	 */
	public void setSplitLocation(int splitLocation)
	{
		this.splitLocation = splitLocation;
	}

	/**
	 * @return the defaultAnimationLayerUrls
	 */
	public List<String> getDefaultAnimationLayerUrls()
	{
		return defaultAnimationLayerUrls;
	}

	/**
	 * @param defaultAnimationLayerUrls the defaultAnimationLayerUrls to set
	 */
	public void setDefaultAnimationLayerUrls(List<String> defaultAnimationLayerUrls)
	{
		this.defaultAnimationLayerUrls = defaultAnimationLayerUrls;
	}
	
	/**
	 * Add the provided URL to the list of default animation layer URLs.
	 */
	public void addDefaultAnimationLayerUrl(String url)
	{
		if (url == null || defaultAnimationLayerUrls.contains(url))
		{
			return;
		}
		defaultAnimationLayerUrls.add(url);
	}
	
	
}
