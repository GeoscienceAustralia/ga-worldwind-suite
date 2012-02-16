package au.gov.ga.worldwind.viewer.panels.geonames;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.worldwind.viewer.util.ColorFont;

/**
 * Helper class for searching the GeoNames.org database.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GeoNamesSearch
{
	private final static String GEONAMES_SEARCH = "http://ws.geonames.org/search";
	private final static Map<String, ColorFont> colorFonts = new HashMap<String, ColorFont>();
	private final static ColorFont defaultColorFont = new ColorFont(Font.decode("Arial-PLAIN-10"), Color.white,
			Color.black);

	static
	{
		colorFonts.put("A", new ColorFont(Font.decode("Arial-BOLD-11"), Color.lightGray, Color.black));
		colorFonts.put("H", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.cyan, Color.black));
		colorFonts.put("L", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.green, Color.black));
		colorFonts.put("P", new ColorFont(Font.decode("Arial-BOLD-11"), Color.yellow, Color.black));
		colorFonts.put("R", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.red, Color.black));
		colorFonts.put("S", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.pink, Color.black));
		colorFonts.put("T", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.orange, Color.black));
		colorFonts.put("U", new ColorFont(Font.decode("Arial-PLAIN-10"), Color.blue, Color.black));
		colorFonts.put("V", new ColorFont(Font.decode("Arial-PLAIN-10"), new Color(0, 128, 0), Color.black));
	}

	/**
	 * Enum of different search types supported by geonames.org.
	 */
	public static enum SearchType
	{
		FUZZY("q"),
		PLACE("name"),
		EXACT("name_equals");

		public final String queryParameter;

		SearchType(String queryParameter)
		{
			this.queryParameter = queryParameter;
		}
	}

	/**
	 * Container class to store results from a geonames.org search.
	 */
	public static class Results
	{
		public final String error;
		public final List<GeoName> places;

		public Results(List<GeoName> places)
		{
			this.places = places;
			this.error = null;
		}

		public Results(String error)
		{
			this.error = error;
			this.places = null;
		}
	}

	/**
	 * Search geonames.org for the given text, and parse and return the results.
	 * 
	 * @param text
	 *            Text to search for
	 * @param type
	 *            Type of search to perform
	 * @return Search results.
	 */
	public static Results search(String text, SearchType type)
	{
		try
		{
			text = URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return new Results(e.getMessage());
		}

		URL url = null;
		try
		{
			url = new URL(GEONAMES_SEARCH + "?style=long&" + type.queryParameter + "=" + text);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return new Results(e.getMessage());
		}

		try
		{
			InputStream is = url.openStream();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(false);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);

			return parse(document);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Results(e.getMessage());
		}
	}

	private static Results parse(Document document)
	{
		NodeList resultsCount = document.getElementsByTagName("totalResultsCount");
		if (resultsCount.getLength() <= 0)
		{
			try
			{
				NodeList statusList = document.getElementsByTagName("status");
				Node status = statusList.item(0);
				Node message = status.getAttributes().getNamedItem("message");
				String error = message.getTextContent();
				return new Results(error);
			}
			catch (Exception e)
			{
				return new Results("Unknown error occurred");
			}
		}
		List<GeoName> places = new ArrayList<GeoName>();
		NodeList nodes = document.getElementsByTagName("geoname");
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				NodeList children = node.getChildNodes();

				String name = null;
				String fcl = null;
				String fcode = null;
				String fclName = null;
				String fcodeName = null;
				Integer geonameId = null;
				Double lat = null;
				Double lon = null;
				String country = null;

				for (int j = 0; j < children.getLength(); j++)
				{
					try
					{
						Node child = children.item(j);
						if (child.getNodeName().equals("name"))
						{
							name = child.getTextContent();
						}
						else if (child.getNodeName().equals("countryName"))
						{
							country = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcl"))
						{
							fcl = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcode"))
						{
							fcode = child.getTextContent();
						}
						else if (child.getNodeName().equals("fclName"))
						{
							fclName = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcodeName"))
						{
							fcodeName = child.getTextContent();
						}
						else if (child.getNodeName().equals("lat"))
						{
							lat = Double.valueOf(child.getTextContent());
						}
						else if (child.getNodeName().equals("lng"))
						{
							lon = Double.valueOf(child.getTextContent());
						}
						else if (child.getNodeName().equals("geonameId"))
						{
							geonameId = Integer.valueOf(child.getTextContent());
						}
					}
					catch (Exception e)
					{
					}
				}

				if (lat != null && lon != null && name != null && name.length() > 0)
				{
					LatLon latlon = new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
					ColorFont colorFont = colorFonts.get(fcl);
					if (colorFont == null)
					{
						colorFont = defaultColorFont;
					}

					GeoName place =
							new GeoName(name, country, geonameId, latlon, fcl, fclName, fcode, fcodeName, colorFont);
					places.add(place);
				}
			}
		}
		return new Results(places);
	}
}
