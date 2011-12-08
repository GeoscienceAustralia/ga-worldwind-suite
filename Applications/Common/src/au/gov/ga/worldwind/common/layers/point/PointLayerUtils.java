package au.gov.ga.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.point.providers.ShapefilePointProvider;
import au.gov.ga.worldwind.common.layers.point.providers.XMLPointProvider;
import au.gov.ga.worldwind.common.layers.point.types.AnnotationPointLayer;
import au.gov.ga.worldwind.common.layers.point.types.IconPointLayer;
import au.gov.ga.worldwind.common.layers.point.types.MarkerPointLayer;
import au.gov.ga.worldwind.common.layers.styled.Attribute;
import au.gov.ga.worldwind.common.layers.styled.Style;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Helper class for the creation of {@link PointLayer}s. Contains XML parsing
 * functionality, as well as factory methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PointLayerUtils extends DataConfigurationUtils
{
	/**
	 * Create a new {@link PointLayer} from an XML definition.
	 * 
	 * @return New {@link PointLayer}.
	 */
	public static PointLayer createPointLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		PointLayerHelper helper = new PointLayerHelper(params);

		PointLayer layer;

		String type = WWXML.getText(domElement, "PointType");
		if ("Marker".equalsIgnoreCase(type))
		{
			layer = new MarkerPointLayer(helper);
		}
		else if ("Annotation".equalsIgnoreCase(type))
		{
			layer = new AnnotationPointLayer(helper);
		}
		else if ("Icon".equalsIgnoreCase(type))
		{
			layer = new IconPointLayer(helper);
		}
		else
		{
			throw new IllegalArgumentException("Could not find layer for PointType: " + type);
		}

		setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Call the standard {@link Layer} setters for values in the params AVList.
	 */
	protected static void setLayerParams(PointLayer layer, AVList params)
	{
		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			layer.setName(s);

		Double d = (Double) params.getValue(AVKey.OPACITY);
		if (d != null)
			layer.setOpacity(d);

		d = (Double) params.getValue(AVKey.MAX_ACTIVE_ALTITUDE);
		if (d != null)
			layer.setMaxActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MIN_ACTIVE_ALTITUDE);
		if (d != null)
			layer.setMinActiveAltitude(d);

		Boolean b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
			layer.setNetworkRetrievalEnabled(b);

		Object o = params.getValue(AVKey.URL_CONNECT_TIMEOUT);
		if (o != null)
			layer.setValue(AVKey.URL_CONNECT_TIMEOUT, o);

		o = params.getValue(AVKey.URL_READ_TIMEOUT);
		if (o != null)
			layer.setValue(AVKey.URL_READ_TIMEOUT, o);

		o = params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (o != null)
			layer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, o);

		/*ScreenCredit sc = (ScreenCredit) params.getValue(AVKey.SCREEN_CREDIT);
		if (sc != null)
			layer.setScreenCredit(sc);*/

		layer.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());
	}

	/**
	 * Fill the params with the values in the {@link PointLayer} specific XML
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
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.POINT_TYPE, "PointType", xpath);

		setupPointProvider(domElement, xpath, params);

		addStyles(domElement, xpath, params);
		addAttributes(domElement, xpath, params);

		return params;
	}

	/**
	 * Adds a {@link PointProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupPointProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.POINT_PROVIDER, new ShapefilePointProvider());
		}
		else if ("XML".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.POINT_PROVIDER, new XMLPointProvider(domElement));
		}
		else
		{
			throw new IllegalArgumentException("Could not find point provider for DataFormat: " + format);
		}
	}

	/**
	 * Parse and add the styles to params
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

		params.setValue(AVKeyMore.POINT_STYLES, styles);
	}

	/**
	 * Parse and add the attributes to params
	 */
	protected static void addAttributes(Element element, XPath xpath, AVList params)
	{
		List<Attribute> attributes = new ArrayList<Attribute>();

		Element[] attributesElements = WWXML.getElements(element, "Attributes/Attribute", xpath);
		if (attributesElements != null)
		{
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
		}

		params.setValue(AVKeyMore.POINT_ATTRIBUTES, attributes);
	}
}
