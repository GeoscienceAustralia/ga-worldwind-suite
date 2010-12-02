package au.gov.ga.worldwind.common.layers.earthquakes;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.view.BasicView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.HSLColor;
import au.gov.ga.worldwind.common.util.Loader;
import au.gov.ga.worldwind.common.util.Setupable;

public class RSSEarthquakesLayer extends RenderableLayer implements Setupable, Loader
{
	private static final String RSS_URL = "http://www.ga.gov.au/rss/quakesfeed.rss";

	private static final int UPDATE_TIME = 10 * 60 * 1000; //10 minutes
	private static final long ONE_DAY = 24 * 60 * 60 * 1000; //1 day
	private static final long MAX_TIME = 30 * ONE_DAY;

	private AnnotationAttributes attributes;
	private Timer updateTimer;
	private SurfaceEarthquakeAnnotation mouseEq, latestEq;
	private GlobeAnnotation tooltipAnnotation;
	private List<LoadingListener> loadingListeners = new ArrayList<LoadingListener>();
	private boolean loading;

	public RSSEarthquakesLayer()
	{
		// Init tooltip annotation
		this.tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
		Font font = Font.decode("Arial-Plain-16");
		this.tooltipAnnotation.getAttributes().setFont(font);
		this.tooltipAnnotation.getAttributes().setSize(new Dimension(270, 0));
		this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
		this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
		this.tooltipAnnotation.getAttributes().setVisible(false);
		this.tooltipAnnotation.setPickEnabled(false);
		this.tooltipAnnotation.setAlwaysOnTop(true);

		updateTimer = new Timer(UPDATE_TIME, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startEarthquakeDownload();
			}
		});
		updateTimer.start();
		startEarthquakeDownload();
	}

	protected void startEarthquakeDownload()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				downloadEarthquakes();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	protected void downloadEarthquakes()
	{
		setLoading(true);
		try
		{
			RetrievalHandler handler = new RetrievalHandler()
			{
				@Override
				public void handle(RetrievalResult result)
				{
					synchronized (this)
					{
						try
						{
							DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
							builderFactory.setNamespaceAware(false);
							DocumentBuilder builder = builderFactory.newDocumentBuilder();
							StringReader reader = new StringReader(result.getAsString().trim());
							InputSource source = new InputSource(reader);
							Document document = builder.parse(source);

							Element[] items = WWXML.getElements(document.getDocumentElement(), "//item", null);
							if (items != null)
							{
								List<Earthquake> earthquakes =
										new ArrayList<RSSEarthquakesLayer.Earthquake>(items.length);
								for (Element item : items)
								{
									earthquakes.add(new Earthquake(item));
								}

								clearRenderables();
								for (Earthquake earthquake : earthquakes)
								{
									addEarthquake(earthquake);
								}
								addRenderable(tooltipAnnotation);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						setLoading(false);
						firePropertyChange(AVKey.LAYER, null, this);
					}
				}
			};

			URL url = new URL(RSS_URL);
			Downloader.downloadAnyway(url, handler, handler, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void addEarthquake(Earthquake earthquake)
	{
		if (attributes == null)
		{
			// Init default attributes for all eq
			attributes = new AnnotationAttributes();
			attributes.setLeader(FrameFactory.LEADER_NONE);
			attributes.setDrawOffset(new Point(0, -16));
			attributes.setSize(new Dimension(32, 32));
			attributes.setBorderWidth(0);
			attributes.setCornerRadius(0);
			attributes.setBackgroundColor(new Color(0, 0, 0, 0));
		}

		Position surfacePosition = new Position(earthquake.position, 0);
		SurfaceEarthquakeAnnotation surfaceAnnotation =
				new SurfaceEarthquakeAnnotation(surfacePosition, earthquake, attributes);
		long time = MAX_TIME;
		if (earthquake.date != null)
		{
			// Compute days since
			Date now = new Date();
			time = now.getTime() - earthquake.date.getTime();

			// Update latestEq
			if (this.latestEq == null || this.latestEq.earthquake.date.getTime() < earthquake.date.getTime())
			{
				this.latestEq = surfaceAnnotation;
			}
		}

		double percent = Math.max(0, Math.min(1, time / (double) MAX_TIME));
		HSLColor hslColor = new HSLColor((float) percent * 240f, 80f, 50f);
		Color color = hslColor.getRGB();
		surfaceAnnotation.getAttributes().setTextColor(color);
		surfaceAnnotation.getAttributes().setScale(earthquake.magnitude / 10);
		addRenderable(surfaceAnnotation);

		EarthquakeAnnotation deepAnnotation =
				new SubSurfaceEarthquakeAnnotation(earthquake.position, earthquake, attributes, surfaceAnnotation);
		deepAnnotation.getAttributes().setTextColor(color);
		deepAnnotation.getAttributes().setScale(earthquake.magnitude / 10);
		deepAnnotation.setPickEnabled(false);
		addRenderable(deepAnnotation);
	}

	@Override
	public void setup(final WorldWindow wwd)
	{
		wwd.addSelectListener(new SelectListener()
		{
			@Override
			public void selected(SelectEvent event)
			{
				Object o = event.getTopObject();
				if (event.getEventAction().equals(SelectEvent.ROLLOVER))
					highlight(o);
				else if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
					click(o);

				if (wwd instanceof Component)
				{
					Cursor cursor =
							(o instanceof SurfaceEarthquakeAnnotation) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
									: null;
					((Component) wwd).setCursor(cursor);
				}
			}
		});
	}

	private void highlight(Object o)
	{
		if (this.mouseEq == o)
			return; // same thing selected

		if (this.mouseEq != null)
		{
			this.mouseEq.getAttributes().setHighlighted(false);
			this.mouseEq = null;
			this.tooltipAnnotation.getAttributes().setVisible(false);
		}

		if (o != null && o instanceof SurfaceEarthquakeAnnotation)
		{
			this.mouseEq = (SurfaceEarthquakeAnnotation) o;
			this.mouseEq.getAttributes().setHighlighted(true);
			this.tooltipAnnotation.setText("<p><b>" + this.mouseEq.earthquake.title + "</b></p>"
					+ this.mouseEq.earthquake.date + "<br/>" + "Magnitude: <b>" + this.mouseEq.earthquake.magnitude
					+ "</b><br/>Depth: <b>" + (this.mouseEq.earthquake.position.elevation / -1000) + " km</b>");
			this.tooltipAnnotation.setPosition(this.mouseEq.getPosition());
			this.tooltipAnnotation.getAttributes().setVisible(true);
		}
	}

	private void click(Object o)
	{
		if (o != null && o instanceof SurfaceEarthquakeAnnotation)
		{
			String link = ((SurfaceEarthquakeAnnotation) o).earthquake.link;
			try
			{
				DefaultLauncher.openURL(new URL(link));
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		if (enabled != updateTimer.isRunning())
		{
			if (enabled)
				updateTimer.start();
			else
				updateTimer.stop();
		}
	}

	public class Earthquake
	{
		public final String title;
		public final Date date;
		public final String link;
		public final Position position;
		public final double magnitude;

		public Earthquake(Element content)
		{
			XPath xpath = WWXML.makeXPath();
			String dateTitle = WWXML.getText(content, "title", xpath);
			link = WWXML.getText(content, "link", xpath);
			String description = WWXML.getText(content, "description", xpath);

			String title = dateTitle;
			Date date = null;
			{
				Pattern pattern = Pattern.compile("(\\d+/\\d+/\\d+ \\d+:\\d+:\\d+\\(\\w+\\)) (.*)");
				Matcher matcher = pattern.matcher(dateTitle);
				if (matcher.find())
				{
					try
					{
						DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss(z)");
						date = formatter.parse(matcher.group(1));
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}

					title = matcher.group(2);
				}
			}
			this.title = title;
			this.date = date;

			LatLon latlon = null;
			double magnitude = 0;
			double elevation = 0;
			{
				Pattern pattern =
						Pattern.compile("Latitude:\\s*(-?\\d*\\.?\\d*)\\s*Longitude:\\s*(-?\\d*\\.?\\d*)\\s*Magnitude:\\s*(-?\\d*\\.?\\d*)\\s*Depth\\(km\\):\\s*(\\d+)");
				Matcher matcher = pattern.matcher(description);
				if (matcher.find())
				{
					latlon =
							LatLon.fromDegrees(Double.parseDouble(matcher.group(1)),
									Double.parseDouble(matcher.group(2)));
					magnitude = Double.parseDouble(matcher.group(3));
					elevation = Double.parseDouble(matcher.group(4)) * -1000;
				}
			}
			this.position = new Position(latlon, elevation);
			this.magnitude = magnitude;
		}
	}

	private abstract class EarthquakeAnnotation extends GlobeAnnotation
	{
		protected final Earthquake earthquake;
		protected DoubleBuffer shapeBuffer;

		public EarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults)
		{
			super("", position, defaults);
			this.earthquake = earthquake;
		}

		@Override
		protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
		{
			double finalScale = scale * this.computeScale(dc);

			GL gl = dc.getGL();
			gl.glTranslated(x, y, 0);
			gl.glScaled(finalScale, finalScale, 1);
		}

		@Override
		public void render(DrawContext dc)
		{
			//override to pass the annotation point to the AnnotationRenderer, so that
			//frustum culling works for subsurface annotations

			if (dc == null)
			{
				String message = Logging.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			if (!this.getAttributes().isVisible())
				return;

			Vec4 annotationPoint = getAnnotationDrawPoint(dc);
			dc.getAnnotationRenderer().render(dc, this, annotationPoint, dc.getCurrentLayer());
		}

		@Override
		protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
		{
			// Draw colored circle around screen point - use annotation's text color
			if (dc.isPickingMode())
			{
				this.bindPickableObject(dc, pickPosition);
			}

			this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);
			drawEarthquake(dc);
		}

		protected abstract void drawEarthquake(DrawContext dc);
	}

	private class SurfaceEarthquakeAnnotation extends EarthquakeAnnotation
	{
		public SurfaceEarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults)
		{
			super(position, earthquake, defaults);
		}

		@Override
		protected void drawEarthquake(DrawContext dc)
		{
			// Draw 32x32 shape from its bottom left corner
			int size = 32;
			if (this.shapeBuffer == null)
				this.shapeBuffer = FrameFactory.createShapeBuffer(FrameFactory.SHAPE_ELLIPSE, size, size, 0, null);

			dc.getGL().glTranslated(-size / 2, -size / 2, 0);
			FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
		}
	}

	private class SubSurfaceEarthquakeAnnotation extends EarthquakeAnnotation
	{
		private final SurfaceEarthquakeAnnotation surfaceAnnotation;

		public SubSurfaceEarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults,
				SurfaceEarthquakeAnnotation surfaceAnnotation)
		{
			super(position, earthquake, defaults);
			this.surfaceAnnotation = surfaceAnnotation;
		}

		@Override
		protected void drawEarthquake(DrawContext dc)
		{
			// Draw 32x32 shape from its bottom left corner
			int size = 32;
			if (this.shapeBuffer == null)
				this.shapeBuffer = FrameFactory.createShapeBuffer(FrameFactory.SHAPE_RECTANGLE, size, 4, 0, null);

			dc.getGL().glTranslated(-size / 2, -2, 0);
			FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);


			//if this is the subsurface annotation, draw a connected line
			if (surfaceAnnotation != null)
			{
				Vec4 drawPoint = getAnnotationDrawPoint(dc);
				Vec4 surfacePoint = surfaceAnnotation.getAnnotationDrawPoint(dc);

				if (drawPoint != null && surfacePoint != null)
				{
					GL gl = dc.getGL();
					OGLStackHandler stack = new OGLStackHandler();

					try
					{
						stack.pushModelview(gl);
						stack.pushProjection(gl);
						BasicView.loadGLViewState(dc, dc.getView().getModelviewMatrix(), dc.getView()
								.getProjectionMatrix());

						gl.glLineWidth(1f);
						gl.glBegin(GL.GL_LINES);
						{
							gl.glVertex3d(drawPoint.x, drawPoint.y, drawPoint.z);
							gl.glVertex3d(surfacePoint.x, surfacePoint.y, surfacePoint.z);
						}
						gl.glEnd();
					}
					finally
					{
						stack.pop(gl);
					}
				}
			}
		}
	}

	protected void fireLoadingStateChanged()
	{
		for (int i = loadingListeners.size() - 1; i >= 0; i--)
		{
			LoadingListener listener = loadingListeners.get(i);
			listener.loadingStateChanged(isLoading());
		}
	}

	protected void setLoading(boolean loading)
	{
		this.loading = loading;
		fireLoadingStateChanged();
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
}
