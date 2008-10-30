package layers.geonames;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeoName implements GeographicText
{
	public final String name;
	public final int geonameId;
	public final LatLon latlon;
	public final String featureClass;
	public final String featureCode;
	public final int level;

	private final VisibilityCalculator visibilityCalculator;
	private final Position position;
	private final TextProperties properties;

	private Collection<GeoName> children;
	private Object fileLock = new Object();

	public GeoName(String name, int geonameId, LatLon latlon,
			String featureClass, String featureCode, int level,
			TextProperties properties, VisibilityCalculator visibilityCalculator)
	{
		this.name = name;
		this.geonameId = geonameId;
		this.latlon = latlon;
		this.position = new Position(latlon, 0);
		this.featureClass = featureClass;
		this.featureCode = featureCode;
		this.level = level;
		this.properties = properties;
		this.visibilityCalculator = visibilityCalculator;
	}

	private String cacheFilename()
	{
		return "GeoNames/" + geonameId + ".xml";
	}

	private URL findCacheFile()
	{
		return WorldWind.getDataFileCache().findFile(cacheFilename(), true);
	}

	private File newCacheFile()
	{
		return WorldWind.getDataFileCache().newFile(cacheFilename());
	}

	public boolean cacheFileExists()
	{
		return findCacheFile() != null;
	}

	public void saveChildren(ByteBuffer buffer) throws IOException
	{
		synchronized (fileLock)
		{
			WWIO.saveBuffer(buffer, newCacheFile());
		}
	}

	public void loadChildren()
	{
		URL url = findCacheFile();
		if (url != null)
		{
			try
			{
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
						.newInstance();
				documentBuilderFactory.setNamespaceAware(false);
				DocumentBuilder documentBuilder = documentBuilderFactory
						.newDocumentBuilder();

				Document document = null;
				synchronized (fileLock)
				{
					document = documentBuilder.parse(url.openStream());
				}
				children = parseDocument(document);
				return;
			}
			catch (Exception e)
			{
				Logging.logger().log(java.util.logging.Level.SEVERE,
						"Deleting corrupt GeoNames .xml file", e);
				WorldWind.getDataFileCache().removeFile(url);
			}
		}
		children = null;
	}

	private List<GeoName> parseDocument(Document document)
	{
		NodeList resultsCount = document.getElementsByTagName("totalResultsCount");
		if(resultsCount.getLength() <= 0)
		{
			return null;
		}
		List<GeoName> geonames = new ArrayList<GeoName>();
		NodeList nodes = document.getElementsByTagName("geoname");
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				NodeList children = node.getChildNodes();

				String name = null;
				String featureClass = null;
				String featureCode = null;
				Integer geonameId = null;
				Double lat = null;
				Double lon = null;

				for (int j = 0; j < children.getLength(); j++)
				{
					try
					{
						Node child = children.item(j);
						if (child.getNodeName().equals("name"))
						{
							name = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcl"))
						{
							featureClass = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcode"))
						{
							featureCode = child.getTextContent();
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
					GeoName geoname = new GeoName(name, geonameId, latlon,
							featureCode, featureClass, level + 1, properties,
							visibilityCalculator);
					geonames.add(geoname);
				}
			}
		}
		return geonames;
	}

	public boolean loadedChildren()
	{
		return children != null;
	}

	public Collection<GeoName> getChildren()
	{
		return children;
	}

	public Color getBackgroundColor()
	{
		return properties.backgroundColor;
	}

	public Color getColor()
	{
		return properties.color;
	}

	public Font getFont()
	{
		return properties.font;
	}

	public Position getPosition()
	{
		return position;
	}

	public CharSequence getText()
	{
		return name;
	}

	public boolean isVisible()
	{
		return visibilityCalculator.isVisible(this);
	}

	@Deprecated
	public void setBackgroundColor(Color background)
	{
	}

	@Deprecated
	public void setColor(Color color)
	{
	}

	@Deprecated
	public void setFont(Font font)
	{
	}

	@Deprecated
	public void setPosition(Position position)
	{
	}

	@Deprecated
	public void setText(CharSequence text)
	{
	}

	@Deprecated
	public void setVisible(boolean visible)
	{
	}

	@Override
	public String toString()
	{
		return name + " " + position;
	}

	@Override
	public int hashCode()
	{
		return geonameId ^ 1234321;
	}
}
