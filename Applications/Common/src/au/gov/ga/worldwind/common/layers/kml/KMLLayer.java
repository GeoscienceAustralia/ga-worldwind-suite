package au.gov.ga.worldwind.common.layers.kml;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.custom.CustomKMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMLFile;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMLInputStream;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMZFile;
import gov.nasa.worldwind.ogc.kml.relativeio.RelativeKMZInputStream;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.util.Loader;
import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A {@link Layer} that parses and renders KML content from a provided KML
 * source.
 */
public class KMLLayer extends RenderableLayer implements Loader
{
	private boolean loading = false;
	private LoadingListenerList loadingListeners = new LoadingListenerList();
	private Object lock = new Object();

	public KMLLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		XMLUtil.checkAndSetURLParam(domElement, params, AVKey.URL, "URL", xpath);

		return params;
	}

	public KMLLayer(AVList params)
	{
		setValues(params);
		final URL url = (URL) params.getValue(AVKey.URL);

		if (url == null)
		{
			throw new IllegalArgumentException("KML url undefined");
		}

		loading = true;
		notifyListeners();

		RetrievalHandler handler = new RetrievalHandler()
		{
			@Override
			public void handle(RetrievalResult result)
			{
				loadKml(url, result.getAsInputStream());
			}
		};
		Downloader.downloadIfModified(url, handler, handler, false);
	}

	public KMLLayer(URL sourceUrl, InputStream stream, AVList params)
	{
		setValues(params);
		setValue(AVKey.URL, sourceUrl);
		loadKml(sourceUrl, stream);
	}

	protected void loadKml(final URL url, final InputStream inputStream)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				//don't allow multiple threads to attempt load at the same time
				synchronized (lock)
				{
					loading = true;
					notifyListeners();

					try
					{
						String contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(url.getPath()));
						boolean isKmz = KMLConstants.KMZ_MIME_TYPE.equals(contentType);
						File file = URLUtil.urlToFile(url);

						KMLDoc doc;
						if (file != null)
						{
							doc =
									isKmz ? new RelativeKMZFile(URLUtil.urlToFile(url), url.toString(), null)
											: new RelativeKMLFile(URLUtil.urlToFile(url), url.toString(), null);
						}
						else
						{
							InputStream stream = inputStream != null ? inputStream : WWIO.openStream(url);
							doc =
									isKmz ? new RelativeKMZInputStream(stream, url.toURI(), url.toString(), null)
											: new RelativeKMLInputStream(stream, url.toURI(), url.toString(), null);
						}

						CustomKMLRoot root = new CustomKMLRoot(doc);
						root.parse();
						KMLController controller = new KMLController(root);
						addRenderable(controller);
						setName(formName(url, root));
					}
					catch (Exception e)
					{
						String message = "Error parsing KML";
						Logging.logger().log(Level.SEVERE, message, e);
						throw new IllegalArgumentException(message, e);
					}
					finally
					{
						loading = false;
						notifyListeners();
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	//from KMLViewer.java
	public static String formName(Object kmlSource, KMLRoot kmlRoot)
	{
		KMLAbstractFeature rootFeature = kmlRoot.getFeature();

		if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
			return rootFeature.getName();

		if (kmlSource instanceof File)
			return ((File) kmlSource).getName();

		if (kmlSource instanceof URL)
			return ((URL) kmlSource).getPath();

		if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
			return WWIO.makeURL((String) kmlSource).getPath();

		return "KML Layer";
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}

	protected void notifyListeners()
	{
		loadingListeners.notifyListeners(isLoading());
	}
}
