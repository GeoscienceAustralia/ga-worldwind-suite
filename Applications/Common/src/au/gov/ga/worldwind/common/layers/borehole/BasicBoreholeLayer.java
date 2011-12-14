package au.gov.ga.worldwind.common.layers.borehole;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.layers.point.types.MarkerPointLayer;
import au.gov.ga.worldwind.common.layers.styled.Attribute;
import au.gov.ga.worldwind.common.layers.styled.BasicStyleProvider;
import au.gov.ga.worldwind.common.layers.styled.Style;
import au.gov.ga.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.worldwind.common.layers.styled.StyleProvider;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.BufferUtil;

public class BasicBoreholeLayer extends AbstractLayer implements BoreholeLayer, SelectListener
{
	private BoreholeProvider boreholeProvider;
	private StyleProvider boreholeStyleProvider = new BasicStyleProvider();
	private StyleProvider sampleStyleProvider = new BasicStyleProvider();
	private final List<BoreholeImpl> boreholes = new ArrayList<BoreholeImpl>();
	private final List<Marker> markers = new ArrayList<Marker>();
	private final Map<Object, BoreholeImpl> idToBorehole = new HashMap<Object, BoreholeImpl>();
	private final MarkerRenderer markerRenderer = new MarkerRenderer();

	private URL context;
	private String url;
	private String dataCacheName;
	private String uniqueIdentifierAttribute;
	private String sampleDepthFromAttribute;
	private String sampleDepthToAttribute;

	private GlobeAnnotation tooltipAnnotation;

	private FastShape fastShape;
	private FloatBuffer boreholeColorBuffer;
	private FloatBuffer pickingColorBuffer;

	@SuppressWarnings("unchecked")
	public BasicBoreholeLayer(AVList params)
	{
		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		boreholeProvider = (BoreholeProvider) params.getValue(AVKeyMore.BOREHOLE_PROVIDER);

		boreholeStyleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.BOREHOLE_STYLES));
		boreholeStyleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.BOREHOLE_ATTRIBUTES));
		sampleStyleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_STYLES));
		sampleStyleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_ATTRIBUTES));

		uniqueIdentifierAttribute = params.getStringValue(AVKeyMore.BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE);
		sampleDepthFromAttribute = params.getStringValue(AVKeyMore.BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE);
		sampleDepthToAttribute = params.getStringValue(AVKeyMore.BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE);

		Validate.notBlank(url, "Borehole data url not set");
		Validate.notBlank(dataCacheName, "Borehole data cache name not set");

		Validate.notNull(boreholeProvider, "Borehole data provider is null");
		Validate.notNull(boreholeStyleProvider.getStyles(), "Borehole style list is null");
		Validate.notNull(boreholeStyleProvider.getAttributes(), "Borehole attribute list is null");
		Validate.notNull(sampleStyleProvider.getStyles(), "Borehole sample style list is null");
		Validate.notNull(sampleStyleProvider.getAttributes(), "Borehole sample attribute list is null");

		Validate.notBlank(uniqueIdentifierAttribute, "Borehole unique identifier attribute not set");
		Validate.notBlank(sampleDepthFromAttribute, "Borehole sample depth-from attribute not set");
		Validate.notBlank(sampleDepthToAttribute, "Borehole sample depth-to attribute not set");

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

		markerRenderer.setOverrideMarkerElevation(true);
		markerRenderer.setElevation(0);
	}

	@Override
	public Sector getSector()
	{
		return boreholeProvider.getSector();
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	public void addBoreholeSample(Position position, AVList attributeValues)
	{
		Object id = attributeValues.getValue(uniqueIdentifierAttribute);
		Validate.notNull(id, "Borehole attributes do not contain an identifier");

		Double depthFrom = objectToDouble(attributeValues.getValue(sampleDepthFromAttribute));
		Double depthTo = objectToDouble(attributeValues.getValue(sampleDepthToAttribute));
		Validate.notNull(depthFrom, "Borehole sample attributes do not contain a valid depth-from");
		Validate.notNull(depthTo, "Borehole sample attributes do not contain a valid depth-to");

		BoreholeImpl borehole = idToBorehole.get(id);
		if (borehole == null)
		{
			MarkerAttributes markerAttributes = new BasicMarkerAttributes();
			borehole = new BoreholeImpl(this, position, markerAttributes);
			idToBorehole.put(id, borehole);
			synchronized (markers)
			{
				boreholes.add(borehole);
				markers.add(borehole);
			}

			StyleAndText boreholeProperties = boreholeStyleProvider.getStyle(attributeValues);
			borehole.setUrl(boreholeProperties.link);
			borehole.setTooltipText(boreholeProperties.text);
			boreholeProperties.style.setPropertiesFromAttributes(context, attributeValues, markerAttributes, borehole);
			MarkerPointLayer.fixShapeType(markerAttributes);
		}

		BoreholeSampleImpl sample = new BoreholeSampleImpl(borehole);
		borehole.addSample(sample);
		sample.setDepthFrom(depthFrom);
		sample.setDepthTo(depthTo);

		StyleAndText sampleProperties = sampleStyleProvider.getStyle(attributeValues);
		sample.setText(sampleProperties.text);
		sample.setLink(sampleProperties.link);
		sampleProperties.style.setPropertiesFromAttributes(context, attributeValues, sample);
	}

	protected Double objectToDouble(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (o instanceof Double)
		{
			return (Double) o;
		}
		try
		{
			return Double.valueOf(o.toString());
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	@Override
	public void loadComplete()
	{
		for (BoreholeImpl borehole : boreholes)
		{
			borehole.loadComplete();
		}
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		doRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
			return;

		boreholeProvider.requestData(this);
		synchronized (markers)
		{
			markerRenderer.render(dc, markers);
		}
		tooltipAnnotation.render(dc);
		renderSamples(dc);
	}

	protected void renderSamples(DrawContext dc)
	{
		GL gl = dc.getGL();
		try
		{
			gl.glPushAttrib(GL.GL_LINE_BIT);
			gl.glLineWidth(10);

			for (BoreholeImpl borehole : boreholes)
			{
				borehole.render(dc);
			}

			/*if (!dc.isPickingMode())
			{
				fastShape.setColorBuffer(boreholeColorBuffer);
				fastShape.render(dc);
			}
			else
			{
				if(pickingColorBuffer == null)
				{
					createPickingColorBuffer(dc);
				}
				
				pickSupport.beginPicking(dc);

				fastShape.setColorBuffer(pickingColorBuffer);
				fastShape.render(dc);

				pickSupport.endPicking(dc);
				PickedObject o = pickSupport.resolvePick(dc, dc.getPickPoint(), this);
				if(o != null)
				{
					System.out.println(((BoreholeSample)o.getObject()).getBorehole().getPosition());
				}
			}*/
		}
		finally
		{
			gl.glPopAttrib();
		}
	}

	@Override
	public boolean isLoading()
	{
		return boreholeProvider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		boreholeProvider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		boreholeProvider.removeLoadingListener(listener);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
			return;

		PickedObject topPickedObject = e.getTopPickedObject();
		if (topPickedObject != null
				&& (topPickedObject.getObject() instanceof Borehole || topPickedObject.getObject() instanceof BoreholeSample))
		{
			highlight(topPickedObject.getObject(), true);

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
			{
				String link = null;
				if (topPickedObject.getObject() instanceof Borehole)
				{
					link = ((Borehole) topPickedObject.getObject()).getLink();
				}
				else if (topPickedObject.getObject() instanceof BoreholeSample)
				{
					link = ((BoreholeSample) topPickedObject.getObject()).getLink();
				}
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else if (topPickedObject != null && topPickedObject.getObject() instanceof BoreholeSample)
		{

		}
		else
		{
			highlight(null, false);
		}
	}

	protected void highlight(Object object, boolean highlight)
	{
		if (highlight)
		{
			String text = null;
			Position position = null;

			if (object instanceof Borehole)
			{
				Borehole borehole = (Borehole) object;
				text = borehole.getText();
				position = borehole.getPosition();
			}
			else if (object instanceof BoreholeSample)
			{
				BoreholeSample sample = (BoreholeSample) object;
				text = sample.getText();
				position =
						Position.fromDegrees(sample.getBorehole().getPosition().getLatitude().degrees, sample
								.getBorehole().getPosition().getLongitude().degrees,
								-(sample.getDepthTo() + sample.getDepthFrom()) / 2d);
			}

			if (text != null)
			{
				tooltipAnnotation.setText(text);
				tooltipAnnotation.setPosition(position);
				tooltipAnnotation.getAttributes().setVisible(true);
			}
		}
		else
		{
			tooltipAnnotation.getAttributes().setVisible(false);
		}
	}

	protected void createFastShape()
	{
		double widthInDegrees = 0.00002; //TODO make parameter
		double invsqrt2 = 1.0 / Math.sqrt(2.0);

		List<Position> positions = new ArrayList<Position>();
		List<Color> colors = new ArrayList<Color>();

		for (Borehole borehole : boreholes)
		{
			double latitude = borehole.getPosition().getLatitude().degrees;
			double longitude = borehole.getPosition().getLongitude().degrees;
			for (BoreholeSample sample : borehole.getSamples())
			{
				Position t = Position.fromDegrees(latitude, longitude, -sample.getDepthFrom());
				Position b = Position.fromDegrees(latitude, longitude, -sample.getDepthTo());
				positions.add(t);
				positions.add(b);

				/*Position t1 = Position.fromDegrees(latitude + widthInDegrees, longitude, -sample.getDepthFrom());
				Position t2 = Position.fromDegrees(latitude + widthInDegrees * invsqrt2, longitude + widthInDegrees * invsqrt2, -sample.getDepthFrom());
				Position t3 = Position.fromDegrees(latitude, longitude + widthInDegrees, -sample.getDepthFrom());
				Position t4 = Position.fromDegrees(latitude - widthInDegrees * invsqrt2, longitude + widthInDegrees * invsqrt2, -sample.getDepthFrom());
				Position t5 = Position.fromDegrees(latitude - widthInDegrees, longitude, -sample.getDepthFrom());
				Position t6 = Position.fromDegrees(latitude - widthInDegrees * invsqrt2, longitude - widthInDegrees * invsqrt2, -sample.getDepthFrom());
				Position t7 = Position.fromDegrees(latitude, longitude - widthInDegrees, -sample.getDepthFrom());
				Position t8 = Position.fromDegrees(latitude + widthInDegrees * invsqrt2, longitude - widthInDegrees * invsqrt2, -sample.getDepthFrom());
				Position b1 = Position.fromDegrees(latitude + widthInDegrees, longitude, -sample.getDepthTo());
				Position b2 = Position.fromDegrees(latitude + widthInDegrees * invsqrt2, longitude + widthInDegrees * invsqrt2, -sample.getDepthTo());
				Position b3 = Position.fromDegrees(latitude, longitude + widthInDegrees, -sample.getDepthTo());
				Position b4 = Position.fromDegrees(latitude - widthInDegrees * invsqrt2, longitude + widthInDegrees * invsqrt2, -sample.getDepthTo());
				Position b5 = Position.fromDegrees(latitude - widthInDegrees, longitude, -sample.getDepthTo());
				Position b6 = Position.fromDegrees(latitude - widthInDegrees * invsqrt2, longitude - widthInDegrees * invsqrt2, -sample.getDepthTo());
				Position b7 = Position.fromDegrees(latitude, longitude - widthInDegrees, -sample.getDepthTo());
				Position b8 = Position.fromDegrees(latitude + widthInDegrees * invsqrt2, longitude - widthInDegrees * invsqrt2, -sample.getDepthTo());

				positions.add(t1);
				positions.add(t2);
				positions.add(b2);
				positions.add(b1);
				
				positions.add(t2);
				positions.add(t3);
				positions.add(b3);
				positions.add(b2);
				
				positions.add(t3);
				positions.add(t4);
				positions.add(b4);
				positions.add(b3);
				
				positions.add(t4);
				positions.add(t5);
				positions.add(b5);
				positions.add(b4);
				
				positions.add(t5);
				positions.add(t6);
				positions.add(b6);
				positions.add(b5);
				
				positions.add(t6);
				positions.add(t7);
				positions.add(b7);
				positions.add(b6);
				
				positions.add(t7);
				positions.add(t8);
				positions.add(b8);
				positions.add(b7);
				
				positions.add(t8);
				positions.add(t1);
				positions.add(b1);
				positions.add(b8);*/

				colors.add(sample.getColor());
			}
		}

		boreholeColorBuffer = BufferUtil.newFloatBuffer(3 * colors.size() * 2);
		for (Color color : colors)
		{
			for (int i = 0; i < 2; i++)
			{
				boreholeColorBuffer.put(color.getRed() / 255f);
				boreholeColorBuffer.put(color.getGreen() / 255f);
				boreholeColorBuffer.put(color.getBlue() / 255f);
			}
		}

		fastShape = new FastShape(positions, GL.GL_LINES);
		fastShape.setFollowTerrain(true);
	}

	/*protected void createPickingColorBuffer(DrawContext dc)
	{
		List<Color> colors = new ArrayList<Color>();
		for (Borehole borehole : boreholes)
		{
			double latitude = borehole.getPosition().getLatitude().degrees;
			double longitude = borehole.getPosition().getLongitude().degrees;
			for (BoreholeSample sample : borehole.getSamples())
			{
				Color pickColor = dc.getUniquePickColor();
				colors.add(pickColor);
				colors.add(pickColor);
				Position position =
						Position.fromDegrees(latitude, longitude, -(sample.getDepthFrom() + sample.getDepthTo()) / 2.0);
				pickSupport.addPickableObject(pickColor.getRGB(), sample, position);
			}
		}
		pickingColorBuffer = FastShape.color3ToFloatBuffer(colors);
	}*/
}
