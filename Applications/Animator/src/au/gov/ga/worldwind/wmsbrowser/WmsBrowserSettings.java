package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.XMLUtil;


/**
 * A class used to retrieve and persist WMS browser settings between
 * invocations.
 */
public class WmsBrowserSettings
{
	/** The settings file name to use */
	private static final String SETTINGS_FILE_NAME = "wmsBrowser.xml";
	
	/** The Singleton Settings instance */
	private static WmsBrowserSettings instance;
	
	public static WmsBrowserSettings get()
	{
		if (instance == null)
		{
			loadSettings();
		}
		return instance;
	}

	public static void save()
	{
		if (instance == null)
		{
			return;
		}
		
		try
		{
			Document document = WWXML.createDocumentBuilder(false).newDocument();
		
			Element rootElement = document.createElement("wmsBrowserSettings");
			document.appendChild(rootElement);
			
			saveSplitLocation(rootElement);
			
			XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(new File(Util.getUserGAWorldWindDirectory(), SETTINGS_FILE_NAME)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void saveSplitLocation(Element rootElement)
	{
		Element splitLocationElement = WWXML.appendElement(rootElement, "splitLocation");
		WWXML.setIntegerAttribute(splitLocationElement, "value", instance.getSplitLocation());
	}

	private static void loadSettings()
	{
		instance = new WmsBrowserSettings();
		
		// If no file is detected, continue with the vanilla instance
		File settingsFile = new File(Util.getUserGAWorldWindDirectory(), SETTINGS_FILE_NAME);
		if (!settingsFile.exists())
		{
			return;
		}
		
		// Otherwise load the settings from the file
		Document xmlDocument = WWXML.openDocument(settingsFile);
		Element rootElement = xmlDocument.getDocumentElement();
		XPath xpath = WWXML.makeXPath();
		
		loadSplitLocation(rootElement, xpath);
	}
	
	private static void loadSplitLocation(Element rootElement, XPath xpath)
	{
		Integer splitLocation = WWXML.getInteger(rootElement, "//splitLocation/@value", xpath);
		if (splitLocation != null)
		{
			instance.setSplitLocation(splitLocation);
		}
	}

	// ----------------------------------
	// Instance members
	// ----------------------------------

	/** The location of the split pane split bar */
	private int splitLocation = 300;
	
	/**
	 * Private constructor. Obtain a {@link WmsBrowserSettings} instance using the static {@link #get()} method.
	 */
	private WmsBrowserSettings(){}
	
	
	public int getSplitLocation()
	{
		return splitLocation;
	}
	
	public void setSplitLocation(int splitLocation)
	{
		this.splitLocation = splitLocation;
	}
}
