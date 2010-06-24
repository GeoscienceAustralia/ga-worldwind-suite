package au.gov.ga.worldwind.panels.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.XMLUtil;

public class LayerTreePersistance
{
	public static INode readFromXML(Object source) throws MalformedURLException
	{
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			INode root = new FolderNode(null, null, null, true);
			Element[] elements = XMLUtil.getElements(elem, "//LayerList", null);
			if (elements != null)
			{
				for (Element element : elements)
				{
					addRelevant(element, root);
				}
			}
			return root;
		}
		return null;
	}

	private static void addRelevant(Element element, INode parent) throws MalformedURLException
	{
		Element[] elements = XMLUtil.getElements(element, "Folder|Layer", null);
		if (elements != null)
		{
			for (Element e : elements)
			{
				if (e.getNodeName().equals("Folder"))
				{
					addFolder(e, parent);
				}
				else if (e.getNodeName().equals("Layer"))
				{
					addLayer(e, parent);
				}
			}
		}
	}

	private static void addFolder(Element element, INode parent) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", null);
		URL icon = XMLUtil.getURL(element, "@icon", null);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false);
		FolderNode node = new FolderNode(name, info, icon, expanded);
		parent.addChild(node);
		addRelevant(element, node);
	}

	private static void addLayer(Element element, INode parent) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL icon = XMLUtil.getURL(element, "@icon", null);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false);
		URL layer = XMLUtil.getURL(element, "@layer", null);
		URL info = XMLUtil.getURL(element, "@info", null);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", false);
		double opacity = XMLUtil.getDouble(element, "@opacity", 1.0);
		LayerNode node = new LayerNode(name, info, icon, expanded, layer, enabled, opacity);
		parent.addChild(node);
		addRelevant(element, node);
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
