package au.gov.ga.worldwind.panels.dataset;

import gov.nasa.worldwind.util.WWXML;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatasetReader
{
	public static IDataset read(Object source, URL context) throws MalformedURLException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		IDataset root = new Dataset(null, null, null, true);

		Document document = WWXML.openDocument(source);
		Element[] elements =
				WWXML.getElements(document.getDocumentElement(), "//DatasetList", null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addRelevant(element, root, context);
			}
		}

		return root;
	}

	private static void addRelevant(Element element, IDataset parent, URL context)
			throws MalformedURLException
	{
		Element[] elements = WWXML.getElements(element, "Dataset|Link|Layer", null);
		if (elements != null)
		{
			for (Element e : elements)
			{
				if (e.getNodeName().equals("Dataset"))
				{
					IDataset dataset = addDataset(e, parent, context);
					addRelevant(e, dataset, context);
				}
				else if (e.getNodeName().equals("Link"))
				{
					addLink(e, parent, context);
				}
				else if (e.getNodeName().equals("Layer"))
				{
					addLayer(e, parent, context);
				}
			}
		}
	}

	private static IDataset addDataset(Element element, IDataset parent, URL context)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description", context);
		URL icon = getURL(element, "@icon", context);
		boolean root = getBoolean(element, "@root", false);
		IDataset dataset = new Dataset(name, description, icon, root);
		parent.getDatasets().add(dataset);
		return dataset;
	}

	private static void addLink(Element element, IDataset parent, URL context)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description", context);
		URL icon = getURL(element, "@icon", context);
		URL url = getURL(element, "@url", context);
		boolean root = getBoolean(element, "@root", false);
		IDataset dataset = new LazyDataset(name, url, description, icon, root);
		parent.getDatasets().add(dataset);
	}

	private static void addLayer(Element element, IDataset parent, URL context)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description", context);
		URL icon = getURL(element, "@icon", context);
		URL url = getURL(element, "@url", context);
		boolean root = getBoolean(element, "@root", false);
		ILayerDefinition layer = new LayerDefinition(name, url, description, icon, root);
		parent.getLayers().add(layer);
	}

	private static URL getURL(Element element, String path, URL context)
			throws MalformedURLException
	{
		String text = WWXML.getText(element, path);
		if (text == null || text.length() == 0)
			return null;
		if (context == null)
			return new URL(text);
		return new URL(context, text);
	}

	private static boolean getBoolean(Element context, String path, boolean def)
	{
		Boolean b = WWXML.getBoolean(context, path, null);
		if (b == null)
			return def;
		return b;
	}

	/*private static String getDescription(Element element)
	{
		if (element == null)
			return null;
		String content = element.getTextContent();
		if (content == null)
			return null;
		return content.trim();
	}*/
}
