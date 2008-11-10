package panels.places;

import geonames.FeatureClass;
import geonames.FeatureCode;
import geonames.FeatureCodes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeoNamesSearch
{
	private final static String GEONAMES_SEARCH = "http://ws.geonames.org/search";

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

	public static class Results
	{
		public final String error;
		public final List<Place> places;

		public Results(List<Place> places)
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
			url = new URL(GEONAMES_SEARCH + "?" + type.queryParameter + "="
					+ text);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return new Results(e.getMessage());
		}

		try
		{
			InputStream is = url.openStream();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			documentBuilderFactory.setNamespaceAware(false);
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
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
		NodeList resultsCount = document
				.getElementsByTagName("totalResultsCount");
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
		List<Place> places = new ArrayList<Place>();
		NodeList nodes = document.getElementsByTagName("geoname");
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				NodeList children = node.getChildNodes();

				String name = null;
				String fclass = null;
				String fcode = null;
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
							fclass = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcode"))
						{
							fcode = child.getTextContent();
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

				if (lat != null && lon != null && geonameId != null
						&& name != null && name.length() > 0)
				{
					LatLon latlon = new LatLon(Angle.fromDegreesLatitude(lat),
							Angle.fromDegreesLongitude(lon));
					FeatureClass featureClass = FeatureCodes.getClass(fclass);
					FeatureCode featureCode = FeatureCodes.getCode(fcode);

					Place place = new Place(name, country, geonameId, latlon,
							featureClass, featureCode);
					places.add(place);
				}
			}
		}
		return new Results(places);
	}
}
