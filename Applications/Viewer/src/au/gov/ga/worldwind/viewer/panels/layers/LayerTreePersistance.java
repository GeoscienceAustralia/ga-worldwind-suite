package au.gov.ga.worldwind.viewer.panels.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;

public class LayerTreePersistance
{
	public static INode readFromXML(Object source) throws MalformedURLException
	{
		XPath xpath = XMLUtil.makeXPath();
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			INode root = new FolderNode(null, null, null, true);
			Element[] elements = XMLUtil.getElements(elem, "//LayerList", xpath);
			if (elements != null)
			{
				for (Element element : elements)
				{
					addRelevant(element, root, xpath);
				}
			}
			return root;
		}
		return null;
	}

	private static void addRelevant(Element element, INode parent, XPath xpath)
			throws MalformedURLException
	{
		Element[] elements = XMLUtil.getElements(element, "Folder|Layer", xpath);
		if (elements != null)
		{
			for (Element e : elements)
			{
				if (e.getNodeName().equals("Folder"))
				{
					addFolder(e, parent, xpath);
				}
				else if (e.getNodeName().equals("Layer"))
				{
					addLayer(e, parent, xpath);
				}
			}
		}
	}

	private static void addFolder(Element element, INode parent, XPath xpath)
			throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name", xpath);
		URL info = XMLUtil.getURL(element, "@info", null, xpath);
		URL icon = XMLUtil.getURL(element, "@icon", null, xpath);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false, xpath);
		FolderNode node = new FolderNode(name, info, icon, expanded);
		parent.addChild(node);
		addRelevant(element, node, xpath);
	}

	private static void addLayer(Element element, INode parent, XPath xpath)
			throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name", xpath);
		URL icon = XMLUtil.getURL(element, "@icon", null, xpath);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false, xpath);
		URL layer = XMLUtil.getURL(element, "@layer", null, xpath);
		URL info = XMLUtil.getURL(element, "@info", null, xpath);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", false, xpath);
		double opacity = XMLUtil.getDouble(element, "@opacity", 1.0, xpath);
		Long expiryTime = XMLUtil.getLong(element, "@expiry", xpath);
		LayerNode node =
				new LayerNode(name, info, icon, expanded, layer, enabled, opacity, expiryTime);
		parent.addChild(node);
		addRelevant(element, node, xpath);
	}

	public static void saveToXML(INode root, File output)
	{
		DocumentBuilder db = XMLUtil.createDocumentBuilder(false);
		Document document = db.newDocument();
		Element element = document.createElement("LayerList");
		document.appendChild(element);
		for (int i = 0; i < root.getChildCount(); i++)
		{
			INode child = root.getChild(i);
			saveNodeToElement(document, element, child);
		}
		XMLUtil.saveDocumentToFormattedFile(document, output.getAbsolutePath());
	}

	private static void saveNodeToElement(Document document, Element element, INode node)
	{
		Element current = null;
		if (node instanceof FolderNode)
		{
			current = document.createElement("Folder");
		}
		else if (node instanceof LayerNode)
		{
			current = document.createElement("Layer");

			LayerNode layer = (LayerNode) node;
			if (layer.getLayerURL() != null)
				current.setAttribute("layer", layer.getLayerURL().toExternalForm());
			XMLUtil.setBooleanAttribute(current, "enabled", layer.isEnabled());
			XMLUtil.setDoubleAttribute(current, "opacity", layer.getOpacity());
			if (layer.getExpiryTime() != null)
				XMLUtil.setLongAttribute(current, "expiry", layer.getExpiryTime());
		}

		if (current != null)
		{
			element.appendChild(current);
			current.setAttribute("name", node.getName());
			if (node.getInfoURL() != null)
				current.setAttribute("info", node.getInfoURL().toExternalForm());
			if (node.getIconURL() != null)
				current.setAttribute("icon", node.getIconURL().toExternalForm());
			XMLUtil.setBooleanAttribute(current, "expanded", node.isExpanded());

			for (int i = 0; i < node.getChildCount(); i++)
			{
				INode child = node.getChild(i);
				saveNodeToElement(document, current, child);
			}
		}
	}
}
