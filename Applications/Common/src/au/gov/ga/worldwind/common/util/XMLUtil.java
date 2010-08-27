package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
				Document document = openDocument(source);
				if (document != null)
					return document.getDocumentElement();
			}
		}
		return null;
	}

	public static boolean getBoolean(Element context, String path, boolean def)
	{
		Boolean b = getBoolean(context, path, null);
		if (b == null)
			return def;
		return b;
	}

	public static double getDouble(Element context, String path, double def)
	{
		Double d = getDouble(context, path, null);
		if (d == null)
			return def;
		return d;
	}

	public static URL getURL(Element element, String path, URL context)
			throws MalformedURLException
	{
		String text = getText(element, path);
		return getURL(text, context);
	}

	public static URL getURL(String text, URL context) throws MalformedURLException
	{
		URL url = getURL2(text, context);
		url = URLTransformer.transform(url);
		return url;
	}

	protected static URL getURL2(String text, URL context) throws MalformedURLException
	{
		if (text == null || text.length() == 0)
			return null;
		if (context == null)
			return new URL(text);
		return new URL(context, text);
	}

	public static Element appendColor(Element context, String path, Color color)
	{
		Element element = WWXML.appendElement(context, path);
		WWXML.setIntegerAttribute(element, "red", color.getRed());
		WWXML.setIntegerAttribute(element, "green", color.getGreen());
		WWXML.setIntegerAttribute(element, "blue", color.getBlue());
		WWXML.setIntegerAttribute(element, "alpha", color.getAlpha());
		return element;
	}

	public static void saveDocumentToFormattedFile(Document doc, String filePath)
	{
		if (doc == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (filePath == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			FileOutputStream outputStream = new FileOutputStream(filePath);
			Source source = new DOMSource(doc);
			Result result = new StreamResult(outputStream);
			Transformer transformer = createTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
			transformer.transform(source, result);
			outputStream.close();
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.ExceptionAttemptingToWriteXml", filePath);
			Logging.logger().severe(message);
			throw new WWRuntimeException(e);
		}
	}
}
