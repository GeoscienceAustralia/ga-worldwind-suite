package au.gov.ga.worldwind.dataset.layers;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LayerTreePersistance
{
	public static NodeItem readFromXML(Object source) throws MalformedURLException
	{
		NodeItem root = new FolderNodeItem(null, null, true);

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

	private static void addRelevant(Element element, NodeItem parent) throws MalformedURLException
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

	private static void addFolder(Element element, NodeItem parent) throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL icon = getURL(element, "@icon");
		boolean expanded = WWXML.getBoolean(element, "@expanded", null);
		FolderNodeItem node = new FolderNodeItem(name, icon, expanded);
		parent.addChild(node);
		addRelevant(element, node);
	}

	private static void addLayer(Element element, NodeItem parent) throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL icon = getURL(element, "@icon");
		boolean expanded = WWXML.getBoolean(element, "@expanded", null);
		URL layer = getURL(element, "@layer");
		URL description = getURL(element, "@description");
		LayerNodeItem node = new LayerNodeItem(name, icon, expanded, layer, description);
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

	public static void saveToXML(NodeItem root, File output)
	{
		DocumentBuilder db = WWXML.createDocumentBuilder(false);
		Document document = db.newDocument();
		Element element = document.createElement("LayerList");
		document.appendChild(element);
		for (int i = 0; i < root.getChildCount(); i++)
		{
			NodeItem child = root.getChild(i);
			saveNodeToElement(document, element, child);
		}
		WWXML.saveDocumentToFile(document, output.getAbsolutePath());
	}

	private static void saveNodeToElement(Document document, Element element, NodeItem node)
	{
		Element current = null;
		if (node instanceof FolderNodeItem)
		{
			current = document.createElement("Folder");
		}
		else if (node instanceof LayerNodeItem)
		{
			current = document.createElement("Layer");

			LayerNodeItem layer = (LayerNodeItem) node;
			if (layer.getLayerURL() != null)
				current.setAttribute("layer", layer.getLayerURL().toExternalForm());
			if (layer.getDescriptionURL() != null)
				current.setAttribute("description", layer.getDescriptionURL().toExternalForm());
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
				NodeItem child = node.getChild(i);
				saveNodeToElement(document, current, child);
			}
		}
	}

	public static interface NodeItem
	{
		public String getName();

		public void setName(String name);

		public URL getIconURL();

		public void setIconURL(URL iconURL);

		public boolean isExpanded();

		public void setExpanded(boolean expanded);

		public void addChild(NodeItem child);

		public void insertChild(int index, NodeItem child);

		public void removeChild(NodeItem child);

		public int getChildCount();

		public NodeItem getChild(int index);

		public int getChildIndex(Object child);

		public NodeItem getParent();

		public void setParent(NodeItem parent);
	}

	public static abstract class AbstractNodeItem implements NodeItem
	{
		private String name;
		private URL iconURL;
		private boolean expanded;
		private NodeItem parent;
		private List<NodeItem> children = new ArrayList<NodeItem>();

		public AbstractNodeItem(String name, URL iconURL, boolean expanded)
		{
			setName(name);
			setIconURL(iconURL);
			setExpanded(expanded);
		}

		@Override
		public void addChild(NodeItem child)
		{
			children.add(child);
			child.setParent(this);
		}

		@Override
		public void insertChild(int index, NodeItem child)
		{
			if (index < 0 || index > getChildCount())
			{
				addChild(child);
				return;
			}
			children.add(index, child);
			child.setParent(this);
		}

		@Override
		public void removeChild(NodeItem child)
		{
			if (children.remove(child))
				child.setParent(null);
		}

		@Override
		public int getChildCount()
		{
			return children.size();
		}

		@Override
		public NodeItem getChild(int index)
		{
			return children.get(index);
		}

		@Override
		public int getChildIndex(Object child)
		{
			return children.indexOf(child);
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public void setName(String name)
		{
			if (name == null)
				name = "";
			this.name = name;
		}

		@Override
		public URL getIconURL()
		{
			return iconURL;
		}

		@Override
		public void setIconURL(URL iconURL)
		{
			this.iconURL = iconURL;
		}

		@Override
		public boolean isExpanded()
		{
			return expanded;
		}

		@Override
		public void setExpanded(boolean expanded)
		{
			this.expanded = expanded;
		}

		@Override
		public String toString()
		{
			return getName();
		}

		@Override
		public NodeItem getParent()
		{
			return parent;
		}

		@Override
		public void setParent(NodeItem parent)
		{
			this.parent = parent;
		}
	}

	public static class FolderNodeItem extends AbstractNodeItem
	{
		public FolderNodeItem(String name, URL iconURL, boolean expanded)
		{
			super(name, iconURL, expanded);
		}
	}

	public static class LayerNodeItem extends AbstractNodeItem
	{
		private URL layerURL;
		private URL descriptionURL;

		public LayerNodeItem(String name, URL iconURL, boolean expanded, URL layerURL,
				URL descriptionURL)
		{
			super(name, iconURL, expanded);
			setLayerURL(layerURL);
			setDescriptionURL(descriptionURL);
		}

		public URL getLayerURL()
		{
			return layerURL;
		}

		public void setLayerURL(URL layerURL)
		{
			this.layerURL = layerURL;
		}

		public URL getDescriptionURL()
		{
			return descriptionURL;
		}

		public void setDescriptionURL(URL descriptionURL)
		{
			this.descriptionURL = descriptionURL;
		}
	}
}
