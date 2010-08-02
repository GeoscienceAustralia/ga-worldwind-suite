package au.gov.ga.worldwind.layers.point.old;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.worldwind.layers.point.old.Attribute.LinkAttribute;
import au.gov.ga.worldwind.layers.point.old.Attribute.StyleAttribute;
import au.gov.ga.worldwind.layers.point.old.Attribute.TextAttribute;
import au.gov.ga.worldwind.layers.point.old.Style.StyleType;
import au.gov.ga.worldwind.util.AVKeyMore;
import au.gov.ga.worldwind.util.XMLUtil;

public class PointLayerUtils extends DataConfigurationUtils
{
	public static void main(String[] args)
	{
		InputStream is = PointLayerUtils.class.getResourceAsStream("layer_definition.xml");
		Element domElement = XMLUtil.getElementFromSource(is);
		AVList params = getParamsFromDocument(domElement, null);
	}

	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate",
				DATE_TIME_PATTERN, xpath);

		WWXML.checkAndSetStringParam(domElement, params, PointLayer.DATA_TYPE, "Data/@type", xpath);
		WWXML.checkAndSetStringParam(domElement, params, PointLayer.DATA_URL, "Data/URL", xpath);

		List<Style> styles = new ArrayList<Style>();
		Element stylesElement = WWXML.getElement(domElement, "Styles", xpath);
		addAnnotationStyles(stylesElement, xpath, styles, params);
		addIconStyles(stylesElement, xpath, styles, params);
		addMarkerStyles(stylesElement, xpath, styles, params);
		params.setValue(PointLayer.POINT_STYLES, styles.toArray(new Style[styles.size()]));

		List<Attribute> attributes = new ArrayList<Attribute>();
		Element attributesElement = WWXML.getElement(domElement, "Attributes", xpath);
		addAttributes(attributesElement, xpath, attributes, params);
		params.setValue(PointLayer.POINT_ATTRIBUTES, attributes.toArray(new Attribute[attributes
				.size()]));

		return params;
	}

	protected static void addAnnotationStyles(Element element, XPath xpath, List<Style> styles,
			AVList params)
	{
		Element[] annotationStyles = WWXML.getElements(element, "AnnotationStyle", xpath);
		if (annotationStyles != null)
		{
			for (Element as : annotationStyles)
			{
				String name = WWXML.getText(as, "@name", xpath);
				boolean defalt = XMLUtil.getBoolean(as, "@default", false);
				Style style = new Style(name, StyleType.Annotation, defalt);
				parseVariables(as, style);

				String frameShape = WWXML.getText(as, "FrameShape", xpath);
				String leaderShape = WWXML.getText(as, "LeaderShape", xpath);

				AnnotationAttributes attributes = new AnnotationAttributes();
				if (frameShape != null)
					attributes.setFrameShape(frameShape);
				if (leaderShape != null)
					attributes.setLeader(leaderShape);
				style.setAttributes(attributes);

				styles.add(style);
			}
		}
	}

	protected static void addIconStyles(Element element, XPath xpath, List<Style> styles,
			AVList params)
	{
		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);

		Element[] iconStyles = WWXML.getElements(element, "IconStyle", xpath);
		if (iconStyles != null)
		{
			for (Element is : iconStyles)
			{
				String name = WWXML.getText(is, "@name", xpath);
				boolean defalt = XMLUtil.getBoolean(is, "@default", false);
				Style style = new Style(name, StyleType.Icon, defalt);
				parseVariables(is, style);

				URL url;
				try
				{
					url = XMLUtil.getURL(is, "URL", context);
				}
				catch (Exception e)
				{
					String message = "Error parsing XML: " + e.getMessage();
					Logging.logger().severe(message);
					throw new IllegalArgumentException(message);
				}
				IconAttributes attributes = new IconAttributes();
				if (url != null)
					attributes.setIconUrl(url);
				style.setAttributes(attributes);

				styles.add(style);
			}
		}
	}

	protected static void addTextStyles(Element element, XPath xpath, List<Style> styles,
			AVList params)
	{
		Element[] textStyles = WWXML.getElements(element, "TextStyle", xpath);
		if (textStyles != null)
		{
			for (Element ts : textStyles)
			{
				String name = WWXML.getText(ts, "@name", xpath);
				boolean defalt = XMLUtil.getBoolean(ts, "@default", false);
				Style style = new Style(name, StyleType.Text, defalt);
				parseVariables(ts, style);

				Color color = WWXML.getColor(ts, "Color", xpath);
				Color backgroundColor = WWXML.getColor(ts, "BackgroundColor", xpath);
				String f = WWXML.getText(ts, "Font", xpath);
				Font font = f != null ? Font.decode(f) : null;

				TextAttributes attributes = new TextAttributes();
				if (color != null)
					attributes.setColor(color);
				if (backgroundColor != null)
					attributes.setBackgroundColor(backgroundColor);
				if (font != null)
					attributes.setFont(font);
				style.setAttributes(attributes);

				styles.add(style);
			}
		}
	}

	protected static void addMarkerStyles(Element element, XPath xpath, List<Style> styles,
			AVList params)
	{
		Element[] markerStyles = WWXML.getElements(element, "MarkerStyle", xpath);
		if (markerStyles != null)
		{
			for (Element ms : markerStyles)
			{
				String name = WWXML.getText(ms, "@name", xpath);
				boolean defalt = XMLUtil.getBoolean(ms, "@default", false);
				Style style = new Style(name, StyleType.Marker, defalt);
				parseVariables(ms, style);

				String shape = WWXML.getText(ms, "Shape", xpath);
				Color color = WWXML.getColor(ms, "Color", xpath);
				Double opacity = WWXML.getDouble(ms, "Opacity", xpath);
				Double pixels = WWXML.getDouble(ms, "Pixels", xpath);
				Double minSize = WWXML.getDouble(ms, "MinSize", xpath);
				Double maxSize = WWXML.getDouble(ms, "MaxSize", xpath);

				MarkerAttributes attributes = new BasicMarkerAttributes();
				if (shape != null)
					attributes.setShapeType(shape);
				if (color != null)
					attributes.setMaterial(new Material(color));
				if (opacity != null)
					attributes.setOpacity(opacity);
				if (pixels != null)
					attributes.setMarkerPixels(pixels);
				if (minSize != null)
					attributes.setMinMarkerSize(minSize);
				if (maxSize != null)
					attributes.setMaxMarkerSize(maxSize);
				style.setAttributes(attributes);

				styles.add(style);
			}
		}
	}

	protected static void parseVariables(Element element, Style style)
	{
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (node instanceof Element)
			{
				Element e = (Element) node;
				String text = e.getTextContent();
				if (text.length() >= 2 && text.startsWith("%") && text.endsWith("%"))
				{
					String variable = text.substring(1, text.length() - 1);
					style.addVariable(e.getNodeName(), variable);
					e.setTextContent("");
				}

				NamedNodeMap attributes = e.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++)
				{
					node = attributes.item(j);
					if (node instanceof Attr)
					{
						Attr a = (Attr) node;
						text = a.getValue();
						if (text.length() >= 2 && text.startsWith("%") && text.endsWith("%"))
						{
							String variable = text.substring(1, text.length() - 1);
							style.addVariable(e.getNodeName() + "." + a.getName(), variable);
							a.setValue("");
						}
					}
				}
			}
		}
	}

	protected static void addAttributes(Element element, XPath xpath, List<Attribute> attributes,
			AVList params)
	{
		Element[] styleAttributes = WWXML.getElements(element, "StyleAttribute", xpath);
		for (Element sa : styleAttributes)
		{
			String name = WWXML.getText(sa, "@name", xpath);
			StyleAttribute attribute = new StyleAttribute(name);

			Element[] cases = WWXML.getElements(sa, "Case", xpath);
			if (cases != null)
			{
				for (Element c : cases)
				{
					String value = WWXML.getText(c, "@value", xpath);
					String style = WWXML.getText(c, "@style", xpath);
					attribute.addCase(value, style);
				}
			}

			Element[] ranges = WWXML.getElements(sa, "Range", xpath);
			if (ranges != null)
			{
				for (Element r : ranges)
				{
					Double min = WWXML.getDouble(r, "@min", xpath);
					Double max = WWXML.getDouble(r, "@max", xpath);
					String style = WWXML.getText(r, "@style", xpath);
					if (min != null && max != null && style != null)
						attribute.addRange(min, max, style);
				}
			}

			Element[] variables = WWXML.getElements(sa, "Variable", xpath);
			if (variables != null)
			{
				for (Element v : variables)
				{
					String variable = WWXML.getText(v, "@name", xpath);
					attribute.addVariable(variable);
				}
			}

			attributes.add(attribute);
		}

		Element[] textAttributes = WWXML.getElements(element, "TextAttribute", xpath);
		for (Element ta : textAttributes)
		{
			String name = WWXML.getText(ta, "@name", xpath);
			TextAttribute attribute = new TextAttribute(name);
			attributes.add(attribute);
		}

		Element linkAttribute = WWXML.getElement(element, "LinkAttribute", xpath);
		if (linkAttribute != null)
		{
			String name = WWXML.getText(linkAttribute, "@name", xpath);
			String url = WWXML.getText(linkAttribute, "@url", xpath);
			String placeholder = WWXML.getText(linkAttribute, "@placeholder", xpath);
			LinkAttribute attribute = new LinkAttribute(name, url, placeholder);
			attributes.add(attribute);
		}
	}
}
