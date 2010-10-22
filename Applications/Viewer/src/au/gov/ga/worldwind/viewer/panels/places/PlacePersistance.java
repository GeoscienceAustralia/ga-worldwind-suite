package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreePersistance;

public class PlacePersistance
{
	public static List<Place> readFromXML(Object source)
	{
		XPath xpath = XMLUtil.makeXPath();
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			List<Place> places = new ArrayList<Place>();

			String nodeName = "Places";
			if (elem.getNodeName().equals(nodeName))
			{
				addPlaces(elem, places, xpath);
			}
			else
			{
				Element[] placesElements = XMLUtil.getElements(elem, nodeName, xpath);
				if (placesElements == null)
					return null;

				for (Element placesElement : placesElements)
				{
					addPlaces(placesElement, places, xpath);
				}
			}
			return places;
		}
		return null;
	}

	protected static void addPlaces(Element placesElement, List<Place> places, XPath xpath)
	{
		Element[] placeElements = XMLUtil.getElements(placesElement, "Place", xpath);
		if (placeElements != null)
		{
			for (Element placeElement : placeElements)
			{
				addPlace(placeElement, places, xpath);
			}
		}
	}

	protected static void addPlace(Element element, List<Place> places, XPath xpath)
	{
		Place place = new Place();
		places.add(place);
		place.setLatLon(XMLUtil.getLatLon(element, "LatLon", xpath));
		place.setLabel(XMLUtil.getText(element, "Label", xpath));
		place.setVisible(XMLUtil.getBoolean(element, "Visible", place.isVisible(), xpath));
		place.setMinZoom(XMLUtil.getDouble(element, "MinZoom", place.getMinZoom(), xpath));
		place.setMaxZoom(XMLUtil.getDouble(element, "MaxZoom", place.getMaxZoom(), xpath));
		place.setSaveCamera(XMLUtil.getBoolean(element, "SaveCamera", place.isSaveCamera(), xpath));
		place.setEyePosition(XMLUtil.getPosition(element, "EyePosition", xpath));
		place.setUpVector(XMLUtil.getVec4(element, "UpVector", xpath));
		place.setExcludeFromPlaylist(XMLUtil.getBoolean(element, "ExcludeFromPlaylist",
				place.isExcludeFromPlaylist(), xpath));
		try
		{
			place.setLayers(LayerTreePersistance.readFromXML(element));
		}
		catch (MalformedURLException e)
		{
			String message = "Error parsing layers from place";
			Logging.logger().log(Level.SEVERE, message, e);
		}
	}

	public static void saveToXML(List<Place> places, File output)
	{
		DocumentBuilder db = XMLUtil.createDocumentBuilder(false);
		Document document = db.newDocument();
		saveToNode(places, document, document);
		XMLUtil.saveDocumentToFormattedFile(document, output.getAbsolutePath());
	}

	public static void saveToNode(List<Place> places, Document document, Node parent)
	{
		Element element = document.createElement("Places");
		parent.appendChild(element);
		for (Place place : places)
		{
			savePlaceToElement(place, document, element);
		}
	}

	protected static void savePlaceToElement(Place place, Document document, Element element)
	{
		Element current = document.createElement("Place");
		element.appendChild(current);

		XMLUtil.appendLatLon(current, "LatLon", place.getLatLon());
		XMLUtil.appendText(current, "Label", place.getLabel());
		XMLUtil.appendBoolean(current, "Visible", place.isVisible());
		XMLUtil.appendDouble(current, "MinZoom", place.getMinZoom());
		XMLUtil.appendDouble(current, "MaxZoom", place.getMaxZoom());
		XMLUtil.appendBoolean(current, "SaveCamera", place.isSaveCamera());
		if (place.getEyePosition() != null)
			XMLUtil.appendPosition(current, "EyePosition", place.getEyePosition());
		if (place.getUpVector() != null)
			XMLUtil.appendVec4(current, "UpVector", place.getUpVector());
		XMLUtil.appendBoolean(current, "ExcludeFromPlaylist", place.isExcludeFromPlaylist());

		if (place.getLayers() != null)
			LayerTreePersistance.saveToNode(place.getLayers(), document, current);
	}
}
