package au.gov.ga.worldwind.layers.point.old;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.point.old.Attribute.LinkAttribute;
import au.gov.ga.worldwind.layers.point.old.Attribute.StyleAttribute;
import au.gov.ga.worldwind.layers.point.old.Attribute.TextAttribute;

public class PointLayer extends AbstractLayer
{
	/* points can be represented by different classes:
	 *  - Annotation
	 *  - UserFacingIcon
	 *  - Marker
	 *  - UserFacingText
	 * 
	 * Layer definition file will specify different 'styles', which correspond to the different
	 * classes above, including the attributes (such as icon url, annotation style, marker type, etc).
	 * 
	 * Layer definition file will also link the attributes of the points to different styles (or just
	 * use a default style for all points).
	 */
	
	/*
	 * Actually, only support one point type per layer (subclasses of this class).
	 * Layer definition selects the kind of type.
	 * Point attributes can:
	 *  - select a style (switch)
	 *  - select a style (value range)
	 *  - be used as a value within a style
	 */

	public final static String POINT_STYLES = PointLayer.class.getName() + ".PointStyles";
	public final static String POINT_ATTRIBUTES = PointLayer.class.getName() + ".PointAttributes";
	public final static String DATA_TYPE = PointLayer.class.getName() + ".DataType";
	public final static String DATA_URL = PointLayer.class.getName() + ".DataURL";

	protected Style[] styles;
	protected Style defaultStyle;
	protected Map<String, Style> styleMap = new HashMap<String, Style>();

	protected Attribute[] attributes;
	protected List<TextAttribute> textAttributes = new ArrayList<TextAttribute>();
	protected LinkAttribute linkAttribute;
	protected Map<String, StyleAttribute> styleAttributes = new HashMap<String, StyleAttribute>();

	protected String url;
	protected String dataCacheName;

	@Deprecated
	private boolean loaded = false;
	protected long loadTime = -1;

	protected IconLayer iconLayer;
	protected AnnotationLayer annotationLayer;
	protected MarkerLayer markerLayer;
	protected TextLayer textLayer;

	public PointLayer(AVList params)
	{
		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			setName(s);

		s = params.getStringValue(DATA_URL);
		if (s != null)
			setUrl(s);

		s = params.getStringValue(AVKey.DATA_CACHE_NAME);
		if (s != null)
			setDataCacheName(s);

		Long l = (Long) params.getValue(AVKey.EXPIRY_TIME);
		if (l != null)
			setExpiryTime(l);

		Object o = params.getValue(POINT_STYLES);
		if (o != null)
			setStyles((Style[]) o);

		o = params.getValue(POINT_ATTRIBUTES);
		if (o != null)
			setAttributes((Attribute[]) o);
	}

	public PointLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		AbstractLayer.getLayerConfigParams(domElement, params);
		PointLayerUtils.getParamsFromDocument(domElement, params);

		return params;
	}

	public Style[] getStyles()
	{
		return styles;
	}

	public void setStyles(Style[] styles)
	{
		this.styles = styles;
	}

	public Attribute[] getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Attribute[] attributes)
	{
		this.attributes = attributes;
		recalculateAttributes();
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getDataCacheName()
	{
		return dataCacheName;
	}

	public void setDataCacheName(String dataCacheName)
	{
		this.dataCacheName = dataCacheName;
	}

	protected void recalculateStyles()
	{
		defaultStyle = null;
		styleMap.clear();

		for (Style style : styles)
		{
			if (style.isDefault() && defaultStyle == null)
			{
				defaultStyle = style;
			}
			styleMap.put(style.getName(), style);
		}
	}

	protected void recalculateAttributes()
	{
		styleAttributes.clear();
		textAttributes.clear();
		linkAttribute = null;

		for (Attribute attribute : attributes)
		{
			if (attribute instanceof TextAttribute)
			{
				textAttributes.add((TextAttribute) attribute);
			}
			else if (attribute instanceof LinkAttribute && linkAttribute == null)
			{
				linkAttribute = (LinkAttribute) attribute;
			}
			else if (attribute instanceof StyleAttribute)
			{
				StyleAttribute sa = (StyleAttribute) attribute;
				styleAttributes.put(sa.name, sa);
			}

		}
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		if (isEnabled())
		{
			if (iconLayer != null)
				iconLayer.pick(dc, point);
			if (annotationLayer != null)
				annotationLayer.pick(dc, point);
			if (markerLayer != null)
				markerLayer.pick(dc, point);
			if (textLayer != null)
				textLayer.pick(dc, point);
		}
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		//TESTING
		if (!loaded)
		{
			try
			{
				loadShapefile(new URL(url));
			}
			catch (MalformedURLException e)
			{
			}
			loaded = true;
		}
		//TESTING


		if (isEnabled())
		{
			if (iconLayer != null)
				iconLayer.render(dc);
			if (annotationLayer != null)
				annotationLayer.render(dc);
			if (markerLayer != null)
				markerLayer.render(dc);
			if (textLayer != null)
				textLayer.render(dc);
		}
	}

	protected void loadShapefile(URL url)
	{
		Shapefile shapefile = new Shapefile(url);
		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();
			DBaseRecord attributes = record.getAttributes();
			Style style = getStyle(attributes);

			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				VecBuffer buffer = record.getPointBuffer(part);
				int size = buffer.getSize();
				for (int i = 0; i < size; i++)
				{
					addPoint(buffer.getPosition(i), attributes, style);
				}
			}
		}
	}

	protected Style getStyle(DBaseRecord attributes)
	{
		//TODO
		for (Entry<String, StyleAttribute> entry : styleAttributes.entrySet())
		{
			StyleAttribute sa = entry.getValue();
			
		}

		return defaultStyle;
	}

	protected void addPoint(Position position, DBaseRecord attributes, Style style)
	{


		switch (style.getType())
		{
		case Annotation:
			break;
		case Icon:
			break;
		case Marker:
			break;
		case Text:
			break;
		}
	}
}
