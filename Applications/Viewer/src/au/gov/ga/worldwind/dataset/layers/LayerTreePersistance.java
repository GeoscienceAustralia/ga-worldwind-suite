package au.gov.ga.worldwind.dataset.layers;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LayerTreePersistance
{
	public static INode readFromXML(Object source) throws MalformedURLException
	{
		INode root = new FolderNode(null, null, true);

		Document document = WWXML.openDocument(source);
		Element[] elements = WWXML.getElements(document.getDocumentElement(), "//LayerList", null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addRelevant(element, root);
			}
		}

		return root;
	}

	private static void addRelevant(Element element, INode parent) throws MalformedURLException
	{
		Element[] elements = WWXML.getElements(element, "Folder|Layer", null);
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
		String name = WWXML.getText(element, "@name");
		URL icon = getURL(element, "@icon");
		boolean expanded = getBoolean(element, "@expanded");
		FolderNode node = new FolderNode(name, icon, expanded);
		parent.addChild(node);
		addRelevant(element, node);
	}

	private static void addLayer(Element element, INode parent) throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL icon = getURL(element, "@icon");
		boolean expanded = getBoolean(element, "@expanded");
		URL layer = getURL(element, "@layer");
		URL description = getURL(element, "@description");
		boolean enabled = getBoolean(element, "@enabled");
		LayerNode node = new LayerNode(name, icon, expanded, layer, description, enabled);
		parent.addChild(node);
		addRelevant(element, node);
	}

	private static URL getURL(Element element, String path) throws MalformedURLException
	{
		String text = WWXML.getText(element, path);
		if (text == null || text.length() == 0)
			return null;
		return new URL(text);
	}

	private static boolean getBoolean(Element element, String path)
	{
		Boolean b = WWXML.getBoolean(element, path, null);
		if (b == null)
			return false;
		return b;
	}

	public static void saveToXML(INode root, File output)
	{
		DocumentBuilder db = WWXML.createDocumentBuilder(false);
		Document document = db.newDocument();
		Element element = document.createElement("LayerList");
		document.appendChild(element);
		for (int i = 0; i < root.getChildCount(); i++)
		{
			INode child = root.getChild(i);
			saveNodeToElement(document, element, child);
		}
		WWXML.saveDocumentToFile(document, output.getAbsolutePath());
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
			WWXML.setBooleanAttribute(current, "enabled", layer.isEnabled());
		}

		if (current != null)
		{
			element.appendChild(current);
			current.setAttribute("name", node.getName());
			if (node.getIconURL() != null)
				current.setAttribute("icon", node.getIconURL().toExternalForm());
			WWXML.setBooleanAttribute(current, "expanded", node.isExpanded());

			for (int i = 0; i < node.getChildCount(); i++)
			{
				INode child = node.getChild(i);
				saveNodeToElement(document, current, child);
			}
		}
	}
}
