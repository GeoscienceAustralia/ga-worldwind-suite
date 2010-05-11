package au.gov.ga.worldwind.panels.dataset;

import gov.nasa.worldwind.util.WWXML;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatasetReader
{
	public static IDataset read(Object source) throws MalformedURLException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		IDataset root = new Dataset(null, null, null);

		Document document = WWXML.openDocument(source);
		Element[] elements =
				WWXML.getElements(document.getDocumentElement(), "//DatasetList", null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addRelevant(element, root);
			}
		}

		return root;
	}

	private static void addRelevant(Element element, IDataset parent) throws MalformedURLException
	{
		Element[] elements = WWXML.getElements(element, "Dataset|Link|Layer", null);
		if (elements != null)
		{
			for (Element e : elements)
			{
				if (e.getNodeName().equals("Dataset"))
				{
					IDataset dataset = addDataset(e, parent);
					addRelevant(e, dataset);
				}
				else if (e.getNodeName().equals("Link"))
				{
					addLink(e, parent);
				}
				else if (e.getNodeName().equals("Layer"))
				{
					addLayer(e, parent);
				}
			}
		}
	}

	private static IDataset addDataset(Element element, IDataset parent)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description");
		URL icon = getURL(element, "@icon");
		IDataset dataset = new Dataset(name, description, icon);
		parent.getDatasets().add(dataset);
		return dataset;
	}

	private static void addLink(Element element, IDataset parent) throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description");
		URL icon = getURL(element, "@icon");
		URL url = getURL(element, "@url");
		IDataset dataset = new LazyDataset(name, url, description, icon);
		parent.getDatasets().add(dataset);
	}

	private static void addLayer(Element element, IDataset parent) throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		URL description = getURL(element, "@description");
		URL icon = getURL(element, "@icon");
		URL url = getURL(element, "@url");
		ILayerDefinition layer = new LayerDefinition(name, url, description, icon);
		parent.getLayers().add(layer);
	}

	private static URL getURL(Element element, String path) throws MalformedURLException
	{
		String text = WWXML.getText(element, path);
		if (text == null || text.length() == 0)
			return null;
		return new URL(text);
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
