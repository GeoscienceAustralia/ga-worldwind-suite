package au.gov.ga.worldwind.dataset;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DatasetReader
{
	public static IDataset read(Object source)
			throws ParserConfigurationException, SAXException, IOException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		IDataset root = new Dataset(null);

		Document doc = WWXML.openDocument(source);
		Element[] elements = WWXML.getElements(doc.getDocumentElement(),
				"//DatasetList", null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addRelevant(element, root);
			}
		}

		return root;
	}

	private static void addRelevant(Element element, IDataset parent)
			throws MalformedURLException
	{
		Element[] elements = WWXML.getElements(element, "Dataset|Link|Layer",
				null);
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
	{
		String name = WWXML.getText(element, "@name");
		IDataset dataset = new Dataset(name);
		parent.getDatasets().add(dataset);
		return dataset;
	}

	private static void addLink(Element element, IDataset parent)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		String url = WWXML.getText(element, "@url");
		IDataset dataset = new LazyDataset(name, new URL(url));
		parent.getDatasets().add(dataset);
	}

	private static void addLayer(Element element, IDataset parent)
			throws MalformedURLException
	{
		String name = WWXML.getText(element, "@name");
		String url = WWXML.getText(element, "@url");
		ILayerDefinition layer = new LayerDefinition(name, new URL(url));
		parent.getLayers().add(layer);
	}
}
