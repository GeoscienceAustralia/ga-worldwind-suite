package au.gov.ga.worldwind.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nasa.worldwind.util.WWXML;

public class XMLUtil extends WWXML
{
	public static Element getElementFromSource(Object source)
	{
		if (source != null)
		{
			if (source instanceof Element)
				return (Element) source;
			else if (source instanceof Document)
				return ((Document) source).getDocumentElement();
			else
			{
				Document document = WWXML.openDocument(source);
				if (document != null)
					return document.getDocumentElement();
			}
		}
		return null;
	}
	
	public static boolean getBoolean(Element context, String path, boolean def)
	{
		Boolean b = XMLUtil.getBoolean(context, path, null);
		if (b == null)
			return def;
		return b;
	}

	public static URL getURL(Element element, String path, URL context)
			throws MalformedURLException
	{
		String text = XMLUtil.getText(element, path);
		return getURL(text, context);
	}

	public static URL getURL(String text, URL context) throws MalformedURLException
	{
		if (text == null || text.length() == 0)
			return null;
		if (context == null)
			return new URL(text);
		return new URL(context, text);
	}
}
