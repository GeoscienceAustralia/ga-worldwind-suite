package au.gov.ga.worldwind.animator.application.settings;

import static au.gov.ga.worldwind.animator.util.Util.isBlank;
import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierFactory;
import au.gov.ga.worldwind.animator.layers.LayerIdentifierImpl;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifierImpl;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
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
			saveDefaultElevationModels(rootElement);
			saveUtilityLayerFlags(rootElement);
			saveDebugFlags(rootElement);
			
			XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(new File(getUserDirectory(), SETTINGS_FILE_NAME)));
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
	}

	private static void saveDebugFlags(Element rootElement)
	{
		WWXML.appendBoolean(rootElement, "logAnimationEvents", instance.isAnimationEventsLogged());
	}

	private static void saveUtilityLayerFlags(Element rootElement)
	{
		WWXML.appendBoolean(rootElement, "showCameraPath", instance.isCameraPathShown());
		WWXML.appendBoolean(rootElement, "showGrid", instance.isGridShown());
		WWXML.appendBoolean(rootElement, "showRuleOfThirds", instance.isRuleOfThirdsShown());
		WWXML.appendBoolean(rootElement, "showCrosshairs", instance.isCrosshairsShown());
	}

	private static void saveDefaultElevationModels(Element rootElement)
	{
		Element animationLayersContainer = WWXML.appendElement(rootElement, "defaultModels");
		List<ElevationModelIdentifier> modelList = instance.getDefaultElevationModels();
		for (int i = 0; i < modelList.size(); i++)
		{
			Element layerElement = WWXML.appendElement(animationLayersContainer, "model");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "name", modelList.get(i).getName());
			WWXML.setTextAttribute(layerElement, "url", modelList.get(i).getLocation());
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
		loadDefaultElevationModels(rootElement);
		loadUtilityLayerFlags(rootElement);
		loadDebugFlags(rootElement);
	}

	private static void loadDebugFlags(Element rootElement)
	{
		instance.setAnimationEventsLogged(XMLUtil.getBoolean(rootElement, "//logAnimationEvents", false));
	}

	private static void loadUtilityLayerFlags(Element rootElement)
	{
		instance.setCameraPathShown(XMLUtil.getBoolean(rootElement, "//showCameraPath", true));
		instance.setGridShown(XMLUtil.getBoolean(rootElement, "//showGrid", true));
		instance.setRuleOfThirdsShown(XMLUtil.getBoolean(rootElement, "//showRuleOfThirds", true));
		instance.setCrosshairsShown(XMLUtil.getBoolean(rootElement, "//showCrosshairs", true));
	}

	private static void loadDefaultElevationModels(Element rootElement)
	{
		List<ElevationModelIdentifier> loadedIdentifiers = new ArrayList<ElevationModelIdentifier>();
		Integer layerCount = WWXML.getInteger(rootElement, "count(//defaultModels/model)", null);
		for (int i = 0; i < layerCount ; i++)
		{
			String modelName = WWXML.getText(rootElement, "//defaultModels/model[@index='" + i + "']/@name");
			String modelUrl = WWXML.getText(rootElement, "//defaultModels/model[@index='" + i + "']/@url");
			if (!isBlank(modelUrl) && !isBlank(modelName))
			{
				loadedIdentifiers.add(new ElevationModelIdentifierImpl(modelName, modelUrl));
			}
		}
		if (!loadedIdentifiers.isEmpty())
		{
			instance.setDefaultElevationModels(loadedIdentifiers);
		}
	}

	private static void loadKnownLayers(Element rootElement)
	{
		List<LayerIdentifier> loadedIdentifiers = new ArrayList<LayerIdentifier>();
		Integer layerCount = WWXML.getInteger(rootElement, "count(//knownLayers/layer)", null);
		for (int i = 0; i < layerCount ; i++)
		{
			String layerName = WWXML.getText(rootElement, "//knownLayers/layer[@index='" + i + "']/@name");
			String layerUrl = WWXML.getText(rootElement, "//knownLayers/layer[@index='" + i + "']/@url");
			if (!isBlank(layerUrl) && !isBlank(layerName))
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
			if (!isBlank(layerUrl) && !isBlank(layerName))
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
		if (!isBlank(lastUsedLocationPath))
		{
			instance.setLastUsedLocation(new File(lastUsedLocationPath));
		}
	}

	private static void loadRecentFilesList(Element rootElement)
	{
		for (int i = MAX_NUMBER_RECENT_FILES - 1; i >= 0; i--)
		{
			String recentFileName = WWXML.getText(rootElement, "//recentFiles/file[@index='" + i + "']/@path");
			if (!isBlank(recentFileName))
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
			new LayerIdentifierImpl("Stars", "http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/stars.xml"),
			new LayerIdentifierImpl("Sky", "http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/sky.xml"),
			new LayerIdentifierImpl("Blue Marble", "http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/blue_marble.xml"),
			new LayerIdentifierImpl("Landsat", "http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/landsat.xml"),
	}));
	
	/** The list of known layer locations for populating the layer palette */
	private List<LayerIdentifier> knownLayers = LayerIdentifierFactory.readFromPropertiesFile("au.gov.ga.worldwind.animator.layers.worldwindLayerIdentities");
	
	/** The default set of elevation models to include in new animations */
	private List<ElevationModelIdentifier> defaultElevationModels = new ArrayList<ElevationModelIdentifier>(Arrays.asList(new ElevationModelIdentifier[]{
			new ElevationModelIdentifierImpl("Earth", "http://www.ga.gov.au/apps/world-wind/dataset/standard/layers/earth_elevation_model.xml"),
	}));
	
	private boolean crosshairsShown = true;
	private boolean gridShown = true;
	private boolean cameraPathShown = true;
	private boolean ruleOfThirdsShown = true;
	private boolean animationEventsLogged = false;
	
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
			recentFiles.remove(recentFiles.size() - 1);
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

	/**
	 * Remove the provided layer from the list of known layers
	 */
	public void removeKnownLayer(LayerIdentifier identifier)
	{
		knownLayers.remove(identifier);
	}
	
	/**
	 * @return {@link #defaultElevationModels}
	 */
	public List<ElevationModelIdentifier> getDefaultElevationModels()
	{
		return defaultElevationModels;
	}
	
	/**
	 * Set {@link #defaultElevationModels}
	 */
	public void setDefaultElevationModels(List<ElevationModelIdentifier> defaultElevationModels)
	{
		this.defaultElevationModels = defaultElevationModels;
	}
	
	/**
	 * @return {@link #gridShown}
	 */
	public boolean isGridShown()
	{
		return gridShown;
	}
	
	/**
	 * Set {@link #gridShown}
	 */
	public void setGridShown(boolean gridShown)
	{
		this.gridShown = gridShown;
	}
	
	/**
	 * @return {@link #ruleOfThirdsShown}
	 */
	public boolean isRuleOfThirdsShown()
	{
		return ruleOfThirdsShown;
	}
	
	/**
	 * Set {@link #ruleOfThirdsShown}
	 */
	public void setRuleOfThirdsShown(boolean ruleOfThirdsShown)
	{
		this.ruleOfThirdsShown = ruleOfThirdsShown;
	}
	
	/**
	 * @return {@link #cameraPathShown}
	 */
	public boolean isCameraPathShown()
	{
		return cameraPathShown;
	}
	
	/**
	 * Set {@link #cameraPathShown}
	 */
	public void setCameraPathShown(boolean cameraPathShown)
	{
		this.cameraPathShown = cameraPathShown;
	}
	
	/**
	 * @return {@link #crosshairsShown}
	 */
	public boolean isCrosshairsShown()
	{
		return crosshairsShown;
	}
	
	/**
	 * Set {@link #crosshairsShown}
	 */
	public void setCrosshairsShown(boolean crosshairsShown)
	{
		this.crosshairsShown = crosshairsShown;
	}

	public boolean isAnimationEventsLogged()
	{
		return animationEventsLogged;
	}

	public void setAnimationEventsLogged(boolean animationEventsLogged)
	{
		this.animationEventsLogged = animationEventsLogged;
	}

}
