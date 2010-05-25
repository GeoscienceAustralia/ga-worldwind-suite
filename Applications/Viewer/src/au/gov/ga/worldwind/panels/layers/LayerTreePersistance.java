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
		INode root = new FolderNode(null, null, true);

		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			Element[] elements = XMLUtil.getElements(elem, "//LayerList", null);
			if (elements != null)
			{
				for (Element element : elements)
				{
					addRelevant(element, root);
				}
			}
		}

		return root;
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
		URL icon = XMLUtil.getURL(element, "@icon", null);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false);
		FolderNode node = new FolderNode(name, icon, expanded);
		parent.addChild(node);
		addRelevant(element, node);
	}

	private static void addLayer(Element element, INode parent) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL icon = XMLUtil.getURL(element, "@icon", null);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false);
		URL layer = XMLUtil.getURL(element, "@layer", null);
		URL description = XMLUtil.getURL(element, "@description", null);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", false);
		LayerNode node = new LayerNode(name, icon, expanded, layer, description, enabled);
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
		XMLUtil.saveDocumentToFile(document, output.getAbsolutePath());
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
			if (layer.getDescriptionURL() != null)
				current.setAttribute("description", layer.getDescriptionURL().toExternalForm());
			XMLUtil.setBooleanAttribute(current, "enabled", layer.isEnabled());
		}

		if (current != null)
		{
			element.appendChild(current);
			current.setAttribute("name", node.getName());
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
