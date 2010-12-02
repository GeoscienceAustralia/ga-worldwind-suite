package au.gov.ga.worldwind.common.layers.earthquakes;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Loader;
import au.gov.ga.worldwind.common.util.XMLUtil;

import com.sun.opengl.util.BufferUtil;

public class HistoricEarthquakesLayer extends AbstractLayer implements Loader
{
	public final static String DATE_COLORING = "Date";
	public final static String MAGNITUDE_COLORING = "Magnitude";
	public final static String DEPTH_COLORING = "Depth";

	private final URL url;
	private final String coloring;
	private Long coloringMinDate;
	private Long coloringMaxDate;

	private double pointSize;

	private boolean loading = false;
	private final List<LoadingListener> loadingListeners = new ArrayList<LoadingListener>();
	private boolean loaded = false;

	private FastShape shape;
	private final Object shapeLock = new Object();

	public HistoricEarthquakesLayer(AVList params)
	{
		if (params.getValue(AVKey.URL) == null)
		{
			throw new IllegalArgumentException("URL not defined");
		}
		URL url = null;
		try
		{
			URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			url = new URL(context, params.getStringValue(AVKey.URL));
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException(e);
		}
		this.url = url;

		String coloring = MAGNITUDE_COLORING;
		if (params.getValue(AVKeyMore.COLORING) != null)
		{
			coloring = params.getStringValue(AVKeyMore.COLORING);
		}
		this.coloring = coloring;

		pointSize = 1;
		if (params.getValue(AVKeyMore.POINT_SIZE) != null)
		{
			pointSize = (Double) params.getValue(AVKeyMore.POINT_SIZE);
		}

		coloringMinDate = (Long) params.getValue(AVKeyMore.COLORING_MIN_DATE);
		coloringMaxDate = (Long) params.getValue(AVKeyMore.COLORING_MAX_DATE);
	}

	public HistoricEarthquakesLayer(Document dom, AVList params)
	{
		this(dom.getDocumentElement(), params);
	}

	public HistoricEarthquakesLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		// Common layer properties.
		AbstractLayer.getLayerConfigParams(domElement, params);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.COLORING, "Coloring", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_SIZE, "PointSize", xpath);
		XMLUtil.checkAndSetFormattedDateParam(domElement, params, AVKeyMore.COLORING_MIN_DATE, "ColoringMinDate", xpath);
		XMLUtil.checkAndSetFormattedDateParam(domElement, params, AVKeyMore.COLORING_MAX_DATE, "ColoringMaxDate", xpath);

		return params;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!loaded)
		{
			loaded = true;
			downloadData();
		}

		GL gl = dc.getGL();
		gl.glPointSize((float) pointSize);
		synchronized (shapeLock)
		{
			if (shape != null)
			{
				shape.render(dc);
			}
		}
		gl.glPointSize(1f);
	}

	protected void downloadData()
	{
		//run download in separate thread, so that data loading from download
		//cache doesn't freeze up the render thread

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				RetrievalHandler handler = new RetrievalHandler()
				{
					@Override
					public void handle(RetrievalResult result)
					{
						if (result.hasData())
						{
							loadData(result.getAsInputStream());
						}
						else if (result.getError() != null)
						{
							result.getError().printStackTrace();
						}
					}
				};

				Downloader.downloadIfModified(url, handler, handler);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	protected void loadData(InputStream is)
	{
		try
		{
			List<Earthquake> quakes = new ArrayList<Earthquake>();
			ObjectInputStream ois = new ObjectInputStream(is);
			while (ois.available() > 0)
			{
				double lat = ois.readDouble();
				double lon = ois.readDouble();
				double elevation = ois.readDouble();
				double magnitude = ois.readDouble();
				long timeInMillis = ois.readLong();

				Position position = Position.fromDegrees(lat, lon, elevation);
				Earthquake quake = new Earthquake(position, magnitude, timeInMillis);
				quakes.add(quake);
			}
			ois.close();

			loadEarthquakes(quakes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void loadEarthquakes(List<Earthquake> earthquakes)
	{
		List<Position> positions = new ArrayList<Position>();
		for (Earthquake earthquake : earthquakes)
		{
			positions.add(earthquake.position);
		}

		DoubleBuffer colorBuffer = BufferUtil.newDoubleBuffer(positions.size() * 3);
		generateColorBuffer(colorBuffer, earthquakes);

		FastShape shape = new FastShape(positions, GL.GL_POINTS);
		shape.setColorBuffer(colorBuffer);
		shape.setColorBufferElementSize(3);

		synchronized (shapeLock)
		{
			this.shape = shape;
		}
	}

	private void generateColorBuffer(DoubleBuffer colorBuffer, List<Earthquake> earthquakes)
	{
		if (DEPTH_COLORING.equalsIgnoreCase(coloring))
		{
			double minElevation = Double.MAX_VALUE;
			double maxElevation = -Double.MAX_VALUE;
			for (Earthquake earthquake : earthquakes)
			{
				minElevation = Math.min(minElevation, earthquake.position.elevation);
				maxElevation = Math.max(maxElevation, earthquake.position.elevation);
			}
			for (Earthquake earthquake : earthquakes)
			{
				double percent = (earthquake.position.elevation - minElevation) / (maxElevation - minElevation);
				Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
				colorBuffer.put(color.getRed() / 255d).put(color.getGreen() / 255d).put(color.getBlue() / 255d);
			}
		}
		else if (DATE_COLORING.equalsIgnoreCase(coloring))
		{
			long minTime = Long.MAX_VALUE;
			long maxTime = Long.MIN_VALUE;

			//if either of the custom min/max dates are null, calculate from the data
			if (coloringMinDate == null || coloringMaxDate == null)
			{
				for (Earthquake earthquake : earthquakes)
				{
					minTime = Math.min(minTime, earthquake.timeInMillis);
					maxTime = Math.max(maxTime, earthquake.timeInMillis);
				}
			}

			minTime = coloringMinDate != null ? coloringMinDate : minTime;
			maxTime = coloringMaxDate != null ? coloringMaxDate : maxTime;

			for (Earthquake earthquake : earthquakes)
			{
				double percent = (earthquake.timeInMillis - minTime) / (double) (maxTime - minTime);
				percent = Math.max(0, Math.min(1, percent));
				Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
				colorBuffer.put(color.getRed() / 255d).put(color.getGreen() / 255d).put(color.getBlue() / 255d);
			}
		}
		else
		{
			//magnitude coloring
			double minMagnitude = Double.MAX_VALUE;
			double maxMagnitude = -Double.MAX_VALUE;
			for (Earthquake earthquake : earthquakes)
			{
				minMagnitude = Math.min(minMagnitude, earthquake.magnitude);
				maxMagnitude = Math.max(maxMagnitude, earthquake.magnitude);
			}
			for (Earthquake earthquake : earthquakes)
			{
				double percent = (earthquake.magnitude - minMagnitude) / (maxMagnitude - minMagnitude);
				
				//scale the magnitude (VERY crude equalisation)
				percent = Math.pow(percent, 0.2);
				
				Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
				colorBuffer.put(color.getRed() / 255d).put(color.getGreen() / 255d).put(color.getBlue() / 255d);
			}
		}
	}

	private static class Earthquake
	{
		public final Position position;
		public final double magnitude;
		public final long timeInMillis;

		public Earthquake(Position position, double magnitude, long timeInMillis)
		{
			this.position = position;
			this.magnitude = magnitude;
			this.timeInMillis = timeInMillis;
		}
	}

	protected void fireLoadingStateChanged()
	{
		for (int i = loadingListeners.size() - 1; i >= 0; i--)
		{
			loadingListeners.get(i).loadingStateChanged(isLoading());
		}
	}

	protected void setLoading(boolean loading)
	{
		this.loading = loading;
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}
}
