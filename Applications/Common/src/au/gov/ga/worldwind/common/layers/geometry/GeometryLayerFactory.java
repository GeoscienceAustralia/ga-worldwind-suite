package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.geometry.provider.ShapefileShapeProvider;
import au.gov.ga.worldwind.common.layers.geometry.types.airspace.AirspaceGeometryLayer;
import au.gov.ga.worldwind.common.layers.styled.Attribute;
import au.gov.ga.worldwind.common.layers.styled.Style;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A factory class used to create {@link GeometryLayer}s from XML layer
 * definition files
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GeometryLayerFactory
{
	protected static final String DATE_TIME_PATTERN = "dd MM yyyy HH:mm:ss z";

	/**
	 * Create a new {@link GeometryLayer} from an XML definition.
	 */
	public static GeometryLayer createGeometryLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		GeometryLayer layer;

		String type = WWXML.getText(domElement, "RenderType");
		if ("Airspace".equalsIgnoreCase(type))
		{
			layer = new AirspaceGeometryLayer(params);
		}
		else
		{
			throw new IllegalArgumentException("Could not find layer for GeometryLayer: " + type);
		}

		setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Call the standard {@link Layer} setters for values in the params AVList.
	 */
	protected static void setLayerParams(GeometryLayer layer, AVList params)
	{
		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
		{
			layer.setName(s);
		}

		Double d = (Double) params.getValue(AVKey.OPACITY);
		if (d != null)
		{
			layer.setOpacity(d);
		}

		d = (Double) params.getValue(AVKey.MAX_ACTIVE_ALTITUDE);
		if (d != null)
		{
			layer.setMaxActiveAltitude(d);
		}

		d = (Double) params.getValue(AVKey.MIN_ACTIVE_ALTITUDE);
		if (d != null)
		{
			layer.setMinActiveAltitude(d);
		}

		Boolean b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
		{
			layer.setNetworkRetrievalEnabled(b);
		}

		Object o = params.getValue(AVKey.URL_CONNECT_TIMEOUT);
		if (o != null)
		{
			layer.setValue(AVKey.URL_CONNECT_TIMEOUT, o);
		}

		o = params.getValue(AVKey.URL_READ_TIMEOUT);
		if (o != null)
		{
			layer.setValue(AVKey.URL_READ_TIMEOUT, o);
		}

		o = params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (o != null)
		{
			layer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, o);
		}

		layer.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());
	}

	/**
	 * Fill the params with the values in the {@link GeometryLayer} specific XML
	 * elements.
	 */
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate", DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.RENDER_TYPE, "RenderType", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.SHAPE_TYPE, "ShapeType", xpath);

		setupShapeProvider(domElement, xpath, params);

		addStyles(domElement, xpath, params);
		addAttributes(domElement, xpath, params);

		return params;
	}

	/**
	 * Adds a {@link ShapeProvider} to params matching the 'DataFormat' XML element.
	 */
	protected static void setupShapeProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.SHAPE_PROVIDER, new ShapefileShapeProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find shape provider for DataFormat: " + format);
		}
	}

	/**
	 * Parse and add the styles to params.
	 */
	protected static void addStyles(Element element, XPath xpath, AVList params)
	{
		List<Style> styles = new ArrayList<Style>();

		Element[] styleElements = WWXML.getElements(element, "Styles/Style", xpath);
		if (styleElements != null)
		{
			for (Element s : styleElements)
			{
				String name = WWXML.getText(s, "@name", xpath);
				boolean defalt = XMLUtil.getBoolean(s, "@default", false);
				Style style = new Style(name, defalt);

				Element[] properties = WWXML.getElements(s, "Property", xpath);
				if (properties != null)
				{
					for (Element p : properties)
					{
						String pname = WWXML.getText(p, "@name", xpath);
						String value = WWXML.getText(p, "@value", xpath);
						String type = WWXML.getText(p, "@type", xpath);
						style.addProperty(pname, value, type);
					}
				}
				styles.add(style);
			}
		}

		params.setValue(AVKeyMore.SHAPE_STYLES, styles);
	}

	/**
	 * Parse and add the attributes to params.
	 */
	protected static void addAttributes(Element element, XPath xpath, AVList params)
	{
		List<Attribute> attributes = new ArrayList<Attribute>();

		Element[] attributesElements = WWXML.getElements(element, "Attributes/Attribute", xpath);
		if (attributesElements == null)
		{
			params.setValue(AVKeyMore.SHAPE_ATTRIBUTES, new ArrayList<Attribute>(0));
			return;
		}
		for (Element a : attributesElements)
		{
			String name = WWXML.getText(a, "@name", xpath);
			Attribute attribute = new Attribute(name);

			Element[] cases = WWXML.getElements(a, "Case", xpath);
			if (cases != null)
			{
				for (Element c : cases)
				{
					String value = WWXML.getText(c, "@value", xpath);
					String style = WWXML.getText(c, "@style", xpath);
					attribute.addCase(value, style);
				}
			}

			Element[] regexes = WWXML.getElements(a, "Regex", xpath);
			if (regexes != null)
			{
				for (Element r : regexes)
				{
					String pattern = WWXML.getText(r, "@pattern", xpath);
					String style = WWXML.getText(r, "@style", xpath);
					attribute.addRegex(pattern, style);
				}
			}

			Element[] ranges = WWXML.getElements(a, "Range", xpath);
			if (ranges != null)
			{
				for (Element r : ranges)
				{
					Double min = WWXML.getDouble(r, "@min", xpath);
					Double max = WWXML.getDouble(r, "@max", xpath);
					String style = WWXML.getText(r, "@style", xpath);
					attribute.addRange(min, max, style);
				}
			}

			Element text = WWXML.getElement(a, "Text", xpath);
			if (text != null)
			{
				String value = WWXML.getText(text, "@value", xpath);
				String placeholder = WWXML.getText(text, "@placeholder", xpath);
				attribute.addText(value, placeholder);
			}

			Element link = WWXML.getElement(a, "Link", xpath);
			if (link != null)
			{
				String url = WWXML.getText(link, "@url", xpath);
				String placeholder = WWXML.getText(link, "@placeholder", xpath);
				attribute.addLink(url, placeholder);
			}

			attributes.add(attribute);
		}

		params.setValue(AVKeyMore.SHAPE_ATTRIBUTES, attributes);
	}
}
