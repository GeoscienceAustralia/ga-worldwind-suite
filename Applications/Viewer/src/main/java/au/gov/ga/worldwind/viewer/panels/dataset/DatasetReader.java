package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Helper class that reads {@link IDataset}s from XML files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetReader
{
	public static IDataset read(Object source, URL context) throws MalformedURLException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		IDataset root = new Dataset(null, null, null, true);

		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			Element[] elements = XMLUtil.getElements(elem, "//DatasetList", null);
			if (elements != null)
			{
				for (Element element : elements)
				{
					addRelevant(element, root, context);
				}
			}
		}

		return root;
	}

	private static void addRelevant(Element element, IDataset parent, URL context) throws MalformedURLException
	{
		Element[] elements = XMLUtil.getElements(element, "Dataset|Link|Layer", null);
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

	private static IDataset addDataset(Element element, IDataset parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		boolean base = XMLUtil.getBoolean(element, "@base", false);
		IDataset dataset = new Dataset(name, info, icon, base);
		parent.addChild(dataset);
		return dataset;
	}

	private static void addLink(Element element, IDataset parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		URL url = XMLUtil.getURL(element, "@url", context);
		boolean base = XMLUtil.getBoolean(element, "@base", false);
		IDataset dataset = new LazyDataset(name, url, info, icon, base);
		parent.addChild(dataset);
	}

	private static void addLayer(Element element, IDataset parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		URL url = XMLUtil.getURL(element, "@url", context);
		boolean base = XMLUtil.getBoolean(element, "@base", false);
		boolean def = XMLUtil.getBoolean(element, "@default", false);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", true);
		ILayerDefinition layer = new LayerDefinition(name, url, info, icon, base, def, enabled);
		parent.addChild(layer);
	}
}
