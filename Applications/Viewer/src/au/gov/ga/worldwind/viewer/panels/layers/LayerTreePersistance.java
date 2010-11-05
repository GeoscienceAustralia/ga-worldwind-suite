package au.gov.ga.worldwind.viewer.panels.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.worldwind.common.util.XMLUtil;

public class LayerTreePersistance
{
	public final static String LAYERS_ROOT_ELEMENT_NAME = "LayerList";

	public static INode readFromXML(Object source) throws MalformedURLException
	{
		XPath xpath = XMLUtil.makeXPath();
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			INode root = new FolderNode(null, null, null, true);

			if (elem.getNodeName().equals(LAYERS_ROOT_ELEMENT_NAME))
			{
				addRelevant(elem, root, xpath);
			}
			else
			{
				Element[] elements = XMLUtil.getElements(elem, LAYERS_ROOT_ELEMENT_NAME, xpath);
				if (elements == null)
					return null;

				for (Element element : elements)
				{
					addRelevant(element, root, xpath);
				}
			}
			return root;
		}
		return null;
	}

	protected static void addRelevant(Element element, INode parent, XPath xpath)
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

	protected static void addFolder(Element element, INode parent, XPath xpath)
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

	protected static void addLayer(Element element, INode parent, XPath xpath)
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
		Document document = saveToDocument(root);
		XMLUtil.saveDocumentToFormattedFile(document, output.getAbsolutePath());
	}

	public static Document saveToDocument(INode root)
	{
		DocumentBuilder db = XMLUtil.createDocumentBuilder(false);
		Document document = db.newDocument();
		saveToNode(root, document, document);
		return document;
	}

	public static void saveToNode(INode root, Document document, Node parent)
	{
		Element element = document.createElement(LAYERS_ROOT_ELEMENT_NAME);
		parent.appendChild(element);
		for (int i = 0; i < root.getChildCount(); i++)
		{
			INode child = root.getChild(i);
			saveNodeToElement(child, document, element);
		}
	}

	protected static void saveNodeToElement(INode node, Document document, Element element)
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
			if (layer.isEnabled())
				XMLUtil.setBooleanAttribute(current, "enabled", layer.isEnabled());
			if (layer.getOpacity() != 1.0)
				XMLUtil.setDoubleAttribute(current, "opacity", layer.getOpacity());
			if (layer.getExpiryTime() != null)
				XMLUtil.setLongAttribute(current, "expiry", layer.getExpiryTime());
		}

		if (current != null)
		{
			element.appendChild(current);
			if (node.getName() != null && node.getName().length() > 0)
				current.setAttribute("name", node.getName());
			if (node.getInfoURL() != null)
				current.setAttribute("info", node.getInfoURL().toExternalForm());
			if (node.getIconURL() != null)
				current.setAttribute("icon", node.getIconURL().toExternalForm());
			if (node.isExpanded())
				XMLUtil.setBooleanAttribute(current, "expanded", node.isExpanded());

			for (int i = 0; i < node.getChildCount(); i++)
			{
				INode child = node.getChild(i);
				saveNodeToElement(child, document, current);
			}
		}
	}
}
