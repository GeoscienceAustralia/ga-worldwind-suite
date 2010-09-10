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

import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifier;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.animation.layer.LayerIdentifierImpl;
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
			saveDefaultAnimationLayers(rootElement);
			saveKnownLayers(rootElement);
			
			XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(new File(getUserDirectory(), SETTINGS_FILE_NAME)));
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
	}

	private static void saveKnownLayers(Element rootElement)
	{
		Element animationLayersContainer = WWXML.appendElement(rootElement, "knownLayers");
		List<LayerIdentifier> layersList = instance.getKnownLayers();
		for (int i = 0; i < layersList.size(); i++)
		{
			Element layerElement = WWXML.appendElement(animationLayersContainer, "layer");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "name", layersList.get(i).getName());
			WWXML.setTextAttribute(layerElement, "url", layersList.get(i).getLocation());
		}
		
	}

	private static void saveDefaultAnimationLayers(Element rootElement)
	{
		Element animationLayersContainer = WWXML.appendElement(rootElement, "defaultLayers");
		List<LayerIdentifier> layersList = instance.getDefaultAnimationLayers();
		for (int i = 0; i < layersList.size(); i++)
		{
			Element layerElement = WWXML.appendElement(animationLayersContainer, "layer");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "name", layersList.get(i).getName());
			WWXML.setTextAttribute(layerElement, "url", layersList.get(i).getLocation());
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
		
		// If no file is detected, continue with the vanilla instance
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
		loadDefaultAnimationLayers(rootElement);
		loadKnownLayers(rootElement);
	}

	private static void loadKnownLayers(Element rootElement)
	{
		List<LayerIdentifier> loadedIdentifiers = new ArrayList<LayerIdentifier>();
		Integer layerCount = WWXML.getInteger(rootElement, "count(//knownLayers/layer)", null);
		for (int i = 0; i < layerCount ; i++)
		{
			String layerName = WWXML.getText(rootElement, "//knownLayers/layer[@index='" + i + "']/@name");
			String layerUrl = WWXML.getText(rootElement, "//knownLayers/layer[@index='" + i + "']/@url");
			if (!Util.isBlank(layerUrl) && !Util.isBlank(layerName))
			{
				loadedIdentifiers.add(new LayerIdentifierImpl(layerName, layerUrl));
			}
		}
		if (!loadedIdentifiers.isEmpty())
		{
			instance.setKnownLayers(loadedIdentifiers);
		}
		
	}

	private static void loadDefaultAnimationLayers(Element rootElement)
	{
		List<LayerIdentifier> loadedIdentifiers = new ArrayList<LayerIdentifier>();
		Integer layerCount = WWXML.getInteger(rootElement, "count(//defaultLayers/layer)", null);
		for (int i = 0; i < layerCount ; i++)
		{
			String layerName = WWXML.getText(rootElement, "//defaultLayers/layer[@index='" + i + "']/@name");
			String layerUrl = WWXML.getText(rootElement, "//defaultLayers/layer[@index='" + i + "']/@url");
			if (!Util.isBlank(layerUrl) && !Util.isBlank(layerName))
			{
				loadedIdentifiers.add(new LayerIdentifierImpl(layerName, layerUrl));
			}
		}
		if (!loadedIdentifiers.isEmpty())
		{
			instance.setDefaultAnimationLayers(loadedIdentifiers);
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
	private List<LayerIdentifier> defaultAnimationLayers = new ArrayList<LayerIdentifier>(Arrays.asList(new LayerIdentifier[]{
			new LayerIdentifierImpl("Stars", "file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/stars.xml"),
			new LayerIdentifierImpl("Sky", "file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/sky.xml"),
			new LayerIdentifierImpl("Blue Marble", "file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/blue_marble.xml"),
			new LayerIdentifierImpl("Landsat", "file://marl/sandpit/symbolic-links/world-wind/current/dataset/standard/layers/landsat.xml"),
	}));
	
	/** The list of known layer locations for populating the layer palette */
	private List<LayerIdentifier> knownLayers = LayerIdentifierFactory.readFromPropertiesFile("au.gov.ga.worldwind.animator.animation.layer.worldwindLayerIdentities");
	
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
	 * Set {@link #defaultAnimationLayers}
	 */
	public void setDefaultAnimationLayers(List<LayerIdentifier> defaultAnimationLayers)
	{
		this.defaultAnimationLayers = defaultAnimationLayers;
	}
	
	/**
	 * @return {@link #defaultAnimationLayers}
	 */
	public List<LayerIdentifier> getDefaultAnimationLayers()
	{
		return defaultAnimationLayers;
	}
	
	/**
	 * Add the provided layer identifier to the list of default animation layers
	 */
	public void addDefaultAnimationLayer(LayerIdentifier layer)
	{
		if (defaultAnimationLayers.contains(layer))
		{
			return;
		}
		defaultAnimationLayers.add(layer);
	}
	
	/**
	 * @return {@link #knownLayers}
	 */
	public List<LayerIdentifier> getKnownLayers()
	{
		return knownLayers;
	}
	
	/**
	 * Set {@link #knownLayers}
	 */
	public void setKnownLayers(List<LayerIdentifier> knownLayers)
	{
		this.knownLayers = knownLayers;
	}
	
	/**
	 * Add the provided layer identifier to the list of known layers
	 */
	public void addKnownLayer(LayerIdentifier layer)
	{
		if (knownLayers.contains(layer))
		{
			return;
		}
		knownLayers.add(layer);
	}
	
}
