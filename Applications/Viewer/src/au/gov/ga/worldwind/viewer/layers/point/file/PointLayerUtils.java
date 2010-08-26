package au.gov.ga.worldwind.viewer.layers.point.file;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.viewer.layers.point.Attribute;
import au.gov.ga.worldwind.viewer.layers.point.Style;
import au.gov.ga.worldwind.viewer.util.XMLUtil;

public class PointLayerUtils extends DataConfigurationUtils
{
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate",
				DATE_TIME_PATTERN, xpath);

		addStyles(domElement, xpath, params);
		addAttributes(domElement, xpath, params);

		return params;
	}

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
						style.addProperty(pname, value);
					}
				}

				styles.add(style);
			}
		}

		Style[] s = styles.toArray(new Style[styles.size()]);
		params.setValue(PointLayer.POINT_STYLES, s);
	}



	protected static void addAttributes(Element element, XPath xpath, AVList params)
	{
		List<Attribute> attributes = new ArrayList<Attribute>();

		Element[] attributesElements = WWXML.getElements(element, "Attributes/Attribute", xpath);
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

		Attribute[] a = attributes.toArray(new Attribute[attributes.size()]);
		params.setValue(PointLayer.POINT_ATTRIBUTES, a);
	}
}
