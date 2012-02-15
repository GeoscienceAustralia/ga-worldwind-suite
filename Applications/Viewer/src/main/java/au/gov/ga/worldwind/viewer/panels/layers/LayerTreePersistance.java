package au.gov.ga.worldwind.viewer.panels.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.worldwind.common.util.URLUtil;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Allows the application layer tree to be persisted to-and-from an XML file.
 * <p/>
 * This is used primarily to persist the user's layer tree between invocations
 * of the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreePersistance
{
	private final static String LAYERS_ROOT_ELEMENT_NAME = "LayerList";
	private static final String LAYER_ELEMENT_NAME = "Layer";
	private static final String FOLDER_ELEMENT_NAME = "Folder";

	private static final String WMS_NODE_TYPE = "wms";
	private static final String WMS_ROOT_FOLDER_TYPE = "wmsRoot";

	private static final String CHILD_NODE_SELECTOR_XPATH = FOLDER_ELEMENT_NAME + "|" + LAYER_ELEMENT_NAME;

	/**
	 * Read a layer tree structure from the provided source.
	 * <p/>
	 * The source object may be one of:
	 * <ul>
	 * <li>DOM Element
	 * <li>DOM Document
	 * <li>URL
	 * <li>File
	 * <li>InputStream
	 * <li>String representation of a URL
	 * </ul>
	 * 
	 * @return The root node of the layer tree
	 */
	public static INode readFromXML(Object source) throws MalformedURLException
	{
		URL context = URLUtil.fromObject(source);

		return readFromXML(source, context);
	}

	/**
	 * Read a layer tree structure from the provided source.
	 * <p/>
	 * The source object may be one of:
	 * <ul>
	 * <li>DOM Element
	 * <li>DOM Document
	 * <li>URL
	 * <li>File
	 * <li>InputStream
	 * <li>String representation of a URL
	 * </ul>
	 * <p/>
	 * An optional context URL can be provided to support relative layer URLs.
	 * 
	 * @return The root node of the layer tree
	 */
	public static INode readFromXML(Object source, URL context) throws MalformedURLException
	{
		XPath xpath = XMLUtil.makeXPath();
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem == null)
		{
			return null;
		}

		INode root = new FolderNode(null, null, null, true);

		if (elem.getNodeName().equals(LAYERS_ROOT_ELEMENT_NAME))
		{
			addChildNodesFromElement(elem, root, xpath, context);
		}
		else
		{
			Element[] elements = XMLUtil.getElements(elem, LAYERS_ROOT_ELEMENT_NAME, xpath);
			if (elements == null)
			{
				return null;
			}

			for (Element element : elements)
			{
				addChildNodesFromElement(element, root, xpath, context);
			}
		}
		return root;
	}

	private static void addChildNodesFromElement(Element element, INode parent, XPath xpath, URL context)
			throws MalformedURLException
	{
		Element[] elements = XMLUtil.getElements(element, CHILD_NODE_SELECTOR_XPATH, xpath);
		if (elements == null)
		{
			return;
		}
		for (Element e : elements)
		{
			if (e.getNodeName().equals(FOLDER_ELEMENT_NAME))
			{
				addFolderNodeFromElement(e, parent, xpath, context);
			}
			else if (e.getNodeName().equals(LAYER_ELEMENT_NAME))
			{
				addLayerNodeFromElement(e, parent, xpath, context);
			}
		}
	}

	private static void addFolderNodeFromElement(Element element, INode parent, XPath xpath, URL context)
			throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name", xpath);
		URL info = XMLUtil.getURL(element, "@info", context, xpath);
		URL icon = XMLUtil.getURL(element, "@icon", context, xpath);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false, xpath);

		String type = XMLUtil.getText(element, "@type", xpath);

		FolderNode node;
		if (WMS_NODE_TYPE.equals(type))
		{
			URL capabilities = XMLUtil.getURL(element, "@serverUrl", context, xpath);
			node = new WmsServerNode(name, icon, expanded, capabilities);
		}
		else if (WMS_ROOT_FOLDER_TYPE.equals(type))
		{
			node = new WmsRootNode(name, icon, expanded);
		}
		else
		{
			node = new FolderNode(name, info, icon, expanded);
		}

		parent.addChild(node);
		addChildNodesFromElement(element, node, xpath, context);
	}

	private static void addLayerNodeFromElement(Element element, INode parent, XPath xpath, URL context)
			throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name", xpath);
		URL icon = XMLUtil.getURL(element, "@icon", context, xpath);
		boolean expanded = XMLUtil.getBoolean(element, "@expanded", false, xpath);
		URL layer = XMLUtil.getURL(element, "@layer", context, xpath);
		URL info = XMLUtil.getURL(element, "@info", context, xpath);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", false, xpath);
		double opacity = XMLUtil.getDouble(element, "@opacity", 1.0, xpath);
		Long expiryTime = XMLUtil.getLong(element, "@expiry", xpath);

		String type = XMLUtil.getText(element, "@type", xpath);

		LayerNode node;
		if (WMS_NODE_TYPE.equals(type))
		{
			URL legend = XMLUtil.getURL(element, "@legend", context, xpath);
			String id = XMLUtil.getText(element, "@layerId", xpath);
			node = new WmsLayerNode(name, info, icon, expanded, layer, enabled, opacity, expiryTime, legend, id);
		}
		else
		{
			node = new LayerNode(name, info, icon, expanded, layer, enabled, opacity, expiryTime);
		}

		parent.addChild(node);
		addChildNodesFromElement(element, node, xpath, context);
	}

	/**
	 * Save the provided layer tree node and it's sub-tree to an XML file at the
	 * location provided.
	 */
	public static void saveToXML(INode root, File output)
	{
		if (root == null)
		{
			return;
		}
		Validate.notNull(output, "An output file is required");

		Document document = saveToDocument(root);
		XMLUtil.saveDocumentToFormattedFile(document, output.getAbsolutePath());
	}

	/**
	 * Save the provided node and it's sub-tree to a generated XML Document
	 * object.
	 * 
	 * @return An XML Document object with the layer tree persisted as XML
	 */
	public static Document saveToDocument(INode root)
	{
		if (root == null)
		{
			return null;
		}

		DocumentBuilder db = XMLUtil.createDocumentBuilder(false);
		Document document = db.newDocument();
		saveToNode(root, document, document);
		return document;
	}

	/**
	 * Save the provided layer tree node and it's sub-tree to the provided XML
	 * DOM node
	 */
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

	private static void saveNodeToElement(INode node, Document document, Element parentElement)
	{
		Element childElement = null;

		if (node instanceof FolderNode)
		{
			childElement = createFolderNodeElement((FolderNode) node, document);
		}
		else if (node instanceof LayerNode)
		{
			childElement = createLayerNodeElement((LayerNode) node, document);
		}

		if (childElement != null)
		{
			parentElement.appendChild(childElement);
			for (int i = 0; i < node.getChildCount(); i++)
			{
				INode child = node.getChild(i);
				saveNodeToElement(child, document, childElement);
			}
		}
	}

	private static Element createFolderNodeElement(FolderNode folder, Document document)
	{
		if (folder.isTransient())
			return null;

		Element folderElement = document.createElement(FOLDER_ELEMENT_NAME);

		if (folder instanceof WmsServerNode)
		{
			folderElement.setAttribute("type", WMS_NODE_TYPE);
			folderElement.setAttribute("serverUrl", ((WmsServerNode) folder).getServerCapabilitiesUrl());
		}
		else if (folder instanceof WmsRootNode)
		{
			folderElement.setAttribute("type", WMS_ROOT_FOLDER_TYPE);
		}

		addCommonAttributesToNodeElement(folder, document, folderElement);
		return folderElement;
	}

	private static Element createLayerNodeElement(LayerNode layer, Document document)
	{
		if (layer.isTransient())
			return null;

		Element layerElement = document.createElement(LAYER_ELEMENT_NAME);

		if (layer.getLayerURL() != null)
		{
			layerElement.setAttribute("layer", layer.getLayerURL().toExternalForm());
		}
		if (layer.isEnabled())
		{
			XMLUtil.setBooleanAttribute(layerElement, "enabled", layer.isEnabled());
		}
		if (layer.getOpacity() != 1.0)
		{
			XMLUtil.setDoubleAttribute(layerElement, "opacity", layer.getOpacity());
		}
		if (layer.getExpiryTime() != null)
		{
			XMLUtil.setLongAttribute(layerElement, "expiry", layer.getExpiryTime());
		}

		if (layer instanceof WmsLayerNode)
		{
			layerElement.setAttribute("type", WMS_NODE_TYPE);
			if (((WmsLayerNode) layer).getLegendURL() != null)
			{
				layerElement.setAttribute("legend", ((WmsLayerNode) layer).getLegendURL().toExternalForm());
			}
			layerElement.setAttribute("layerId", ((WmsLayerNode) layer).getLayerId());
		}

		addCommonAttributesToNodeElement(layer, document, layerElement);

		return layerElement;
	}

	private static void addCommonAttributesToNodeElement(INode node, Document document, Element current)
	{
		if (current == null)
		{
			return;
		}
		if (node.getName() != null && node.getName().length() > 0)
		{
			current.setAttribute("name", node.getName());
		}
		if (node.getInfoURL() != null)
		{
			current.setAttribute("info", node.getInfoURL().toExternalForm());
		}
		if (node.getIconURL() != null)
		{
			current.setAttribute("icon", node.getIconURL().toExternalForm());
		}
		if (node.isExpanded())
		{
			XMLUtil.setBooleanAttribute(current, "expanded", node.isExpanded());
		}
	}
}
