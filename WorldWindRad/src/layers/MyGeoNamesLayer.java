package layers;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyGeoNamesLayer extends RenderableLayer
{
	private final Object fileLock = new Object();

	private final static String GEONAMES_CHILDREN = "http://ws.geonames.org/children";
	private final static int GEONAMES_GLOBE_ID = 6295630;

	//TODO different attributes for different feature codes (fcode)
	private AnnotationAttributes attributes;

	private Vec4 lastEyePoint;

	public MyGeoNamesLayer()
	{
		this.setName("GeoNames");

		attributes = new AnnotationAttributes();
		attributes.setTextColor(Color.YELLOW);
		attributes.setBackgroundColor(Color.BLACK);
		attributes.setFont(Font.decode("Arial-Bold-10"));
		attributes.setFrameShape(FrameFactory.SHAPE_NONE);
		attributes.setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
		attributes.setEffect(MultiLineTextRenderer.EFFECT_OUTLINE);
		attributes.setDrawOffset(new Point(0, 0));
		attributes.setInsets(new Insets(0, 0, 0, 0));
	}

	public void doRender(DrawContext dc)
	{
		if (this.needsToUpdate(dc))
		{
			this.lastEyePoint = dc.getView().getEyePoint();
			Sector sector = dc.getVisibleSector();
			update(sector);
		}
		super.doRender(dc);
	}

	private boolean needsToUpdate(DrawContext dc)
	{
		if (this.lastEyePoint == null)
			return true;

		View view = dc.getView();
		double altitudeAboveGround = computeAltitudeAboveGround(dc);
		return view.getEyePoint().distanceTo3(this.lastEyePoint) > altitudeAboveGround / 10; // 10% of AAG
	}

	private double computeAltitudeAboveGround(DrawContext dc)
	{
		View view = dc.getView();
		Position eyePosition = view.getEyePosition();
		Vec4 surfacePoint = getSurfacePoint(dc, eyePosition.getLatitude(),
				eyePosition.getLongitude());
		return view.getEyePoint().distanceTo3(surfacePoint);
	}

	private Vec4 getSurfacePoint(DrawContext dc, Angle latitude, Angle longitude)
	{
		Vec4 surfacePoint = dc.getSurfaceGeometry().getSurfacePoint(latitude,
				longitude);
		if (surfacePoint == null)
			surfacePoint = dc.getGlobe().computePointFromPosition(
					new Position(latitude, longitude, dc.getGlobe()
							.getElevation(latitude, longitude)));
		return surfacePoint;
	}

	private void update(Sector sector)
	{
		load(GEONAMES_GLOBE_ID, sector, 0, 1);
	}

	private File getCacheFile(int geonameId)
	{
		return WorldWind.getDataFileCache().newFile(
				"GeoNames/" + geonameId + ".xml");
	}

	private void load(int geonameId, Sector sector, int level, int maxLevels)
	{
		File xmlFile = getCacheFile(geonameId);
		if (xmlFile.exists())
		{
			if (load(xmlFile, sector, level, maxLevels))
			{
				return;
			}
		}
		download(geonameId, sector, level, maxLevels);
	}

	private boolean load(File xmlFile, Sector sector, int level, int maxLevels)
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
				document = documentBuilder.parse(xmlFile);
			}
			parse(document, sector, level, maxLevels);
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("GeoRSS.IOExceptionParsing",
					xmlFile.getPath());
			Logging.logger().log(Level.SEVERE, message, e);
			return false;
		}
		return true;
	}

	private void parse(Document document, Sector sector, int level,
			int maxLevels)
	{
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
					if (sector.contains(latlon))
					{
						Place place = new Place(name, latlon, geonameId,
								featureCode, featureClass);
						addPlace(place);

						//TODO ONLY LOAD NEXT LEVEL AT CERTAIN ZOOM LEVELS!

						if (level < maxLevels)
						{
							load(geonameId, sector, level + 1, maxLevels);
						}
					}
				}
			}
		}
	}

	private void addPlace(Place place)
	{
		System.out.println("Added place " + place);
		GlobeAnnotation ga = new GlobeAnnotation(place.name, new Position(
				place.position, 0), attributes);
		addRenderable(ga);
	}

	private void download(int geonameId, Sector sector, int level, int maxLevels)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL url = null;
		try
		{
			url = new URL(GEONAMES_CHILDREN + "?geonameId=" + geonameId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			return;

		Retriever retriever;
		DownloadPostProcessor dpp = new DownloadPostProcessor(geonameId, sector, level, maxLevels);

		if ("http".equalsIgnoreCase(url.getProtocol()))
		{
			retriever = new HTTPRetriever(url, dpp);
		}
		else
		{
			Logging.logger().severe(
					"UnknownRetrievalProtocol: " + url.toString());
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this,
				AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(this,
				AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever);
	}

	private void saveBuffer(ByteBuffer buffer, File file) throws IOException
	{
		synchronized (fileLock)
		{
			WWIO.saveBuffer(buffer, file);
		}
	}

	private class DownloadPostProcessor implements RetrievalPostProcessor
	{
		private int geonameId;
		private Sector sector;
		private int level;
		private int maxLevels;

		public DownloadPostProcessor(int geonameId, Sector sector, int level, int maxLevels)
		{
			this.geonameId = geonameId;
			this.sector = sector;
			this.level = level;
			this.maxLevels = maxLevels;
		}

		public ByteBuffer run(Retriever retriever)
		{
			if (retriever == null)
			{
				String msg = Logging.getMessage("nullValue.RetrieverIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			try
			{
				if (!retriever.getState().equals(
						Retriever.RETRIEVER_STATE_SUCCESSFUL))
					return null;

				URLRetriever r = (URLRetriever) retriever;
				ByteBuffer buffer = r.getBuffer();

				if (retriever instanceof HTTPRetriever)
				{
					HTTPRetriever htr = (HTTPRetriever) retriever;
					if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
					{
						return null;
					}
				}

				final File outFile = getCacheFile(geonameId);
				if (outFile == null)
					return null;

				if (outFile.exists())
					return buffer;

				if (buffer != null)
				{
					String contentType = r.getContentType();
					if (contentType == null)
					{
						return null;
					}

					if (contentType.contains("xml")
							|| contentType.contains("html")
							|| contentType.contains("text"))
					{

						saveBuffer(buffer, outFile);
					}
					else
					{
						return null;
					}

					load(geonameId, sector, level, maxLevels); //load the downloaded file!
					return buffer;
				}
			}
			catch (java.io.IOException e)
			{
				Logging.logger().log(java.util.logging.Level.SEVERE,
						e.getMessage());
			}
			return null;
		}
	}

	private class Place
	{
		public final String name;
		public final LatLon position;
		public final int geonameId;
		public final String featureCode;
		public final String featureClass;

		public Place(String name, LatLon position, int geonameId,
				String featureCode, String featureClass)
		{
			this.name = name;
			this.position = position;
			this.geonameId = geonameId;
			this.featureCode = featureCode;
			this.featureClass = featureClass;
		}

		@Override
		public String toString()
		{
			return "PLACE: " + name + " " + position;
		}
	}
}
