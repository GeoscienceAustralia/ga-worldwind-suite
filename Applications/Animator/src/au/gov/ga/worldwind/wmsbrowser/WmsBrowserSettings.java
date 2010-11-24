package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.Util.isBlank;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifierImpl;


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
			saveWindowSize(rootElement);
			saveWmsServerLocations(rootElement);
			
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

	private static void saveWindowSize(Element rootElement)
	{
		Element windowSizeElement = WWXML.appendElement(rootElement, "windowSize");
		WWXML.setIntegerAttribute(windowSizeElement, "width", instance.getWindowDimension().width);
		WWXML.setIntegerAttribute(windowSizeElement, "height", instance.getWindowDimension().height);
	}
	
	private static void saveWmsServerLocations(Element rootElement)
	{
		Element serverLocationsContainer = WWXML.appendElement(rootElement, "serverLocations");
		List<WmsServerIdentifier> servers = instance.getWmsServers();
		for (int i = 0; i < servers.size(); i++)
		{
			Element layerElement = WWXML.appendElement(serverLocationsContainer, "server");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "name", servers.get(i).getName());
			WWXML.setTextAttribute(layerElement, "url", servers.get(i).getCapabilitiesUrl().toExternalForm());
		}
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
		loadWindowSize(rootElement, xpath);
		loadWmsServerLocations(rootElement, xpath);
	}
	
	private static void loadSplitLocation(Element rootElement, XPath xpath)
	{
		Integer splitLocation = WWXML.getInteger(rootElement, "//splitLocation/@value", xpath);
		if (splitLocation != null)
		{
			instance.setSplitLocation(splitLocation);
		}
	}
	
	private static void loadWindowSize(Element rootElement, XPath xpath)
	{
		Integer width = WWXML.getInteger(rootElement, "//windowSize/@width", xpath);
		Integer height = WWXML.getInteger(rootElement, "//windowSize/@height", xpath);
		if (width != null && height != null)
		{
			instance.setWindowDimension(new Dimension(width, height));
		}
	}
	
	private static void loadWmsServerLocations(Element rootElement, XPath xpath)
	{
		List<WmsServerIdentifier> servers = new ArrayList<WmsServerIdentifier>();
		Integer serverCount = WWXML.getInteger(rootElement, "count(//serverLocations/server)", null);
		for (int i = 0; i < serverCount; i++)
		{
			String serverName = WWXML.getText(rootElement, "//serverLocations/server[@index='" + i + "']/@name");
			String serverLocation = WWXML.getText(rootElement, "//serverLocations/server[@index='" + i + "']/@url");
			if (!isBlank(serverLocation))
			{
				URL url = toUrl(serverLocation);
				if (url != null)
				{
					servers.add(new WmsServerIdentifierImpl(serverName, url));
				}
			}
		}
		if (!servers.isEmpty())
		{
			instance.setWmsServers(servers);
		}
	}

	private static URL toUrl(String serverLocation)
	{
		try
		{
			return new URL(serverLocation);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	// ----------------------------------
	// Instance members
	// ----------------------------------

	/** The location of the split pane split bar */
	private int splitLocation = 300;
	
	private Dimension windowDimension = new Dimension(768, 640);
	
	private List<WmsServerIdentifier> wmsServers;
	
	/**
	 * Private constructor. Obtain a {@link WmsBrowserSettings} instance using the static {@link #get()} method.
	 */
	private WmsBrowserSettings()
	{
		try
		{
			wmsServers = new ArrayList<WmsServerIdentifier>(Arrays.asList(new WmsServerIdentifier[]{
					new WmsServerIdentifierImpl("Geoscience Australia", new URL("http://www.ga.gov.au/wms/getmap?dataset=geows_outcrops&service=wms&request=getcapabilities")),
					new WmsServerIdentifierImpl("NASA Earth Observations", new URL("http://neowms.sci.gsfc.nasa.gov/wms/wms")),
			}));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public int getSplitLocation()
	{
		return splitLocation;
	}
	
	public void setSplitLocation(int splitLocation)
	{
		this.splitLocation = splitLocation;
	}

	public List<WmsServerIdentifier> getWmsServers()
	{
		return wmsServers;
	}
	
	public void addWmsServer(WmsServerIdentifier identifier)
	{
		if (identifier == null || wmsServers.contains(identifier))
		{
			return;
		}
		wmsServers.add(identifier);
	}
	
	public void setWmsServers(List<WmsServerIdentifier> identifiers)
	{
		this.wmsServers = identifiers;
	}
	
	public Dimension getWindowDimension()
	{
		return windowDimension;
	}

	public void setWindowDimension(Dimension windowDimension)
	{
		this.windowDimension = windowDimension;
	}
	
}
