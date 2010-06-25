package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.AVKeyMore;
import au.gov.ga.worldwind.util.XMLUtil;

public class LayerLoader
{
	public static LoadedLayer load(URL sourceURL, Object source) throws Exception
	{
		Element element = XMLUtil.getElementFromSource(source);

		AVList params = new AVListImpl();
		if (sourceURL != null)
		{
			params.setValue(AVKeyMore.CONTEXT_URL, sourceURL);
		}

		URL legend = XMLUtil.getURL(element, "Legend", sourceURL);
		URL query = XMLUtil.getURL(element, "Query", sourceURL);

		Object o = null;

		//if (o == null)
		{
			try
			{
				Factory factory =
						(Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
				o = factory.createFromConfigSource(element, params);
			}
			catch (Exception e)
			{
			}
		}

		if (o == null)
		{
			try
			{
				Factory factory =
						(Factory) WorldWind
								.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
				o = factory.createFromConfigSource(element, params);
			}
			catch (Exception e)
			{
			}
		}

		if (o == null)
		{
			throw new Exception("Error reading file");
		}

		LoadedLayer loaded = new LoadedLayer(o, params);
		loaded.setLegendURL(legend);
		loaded.setQueryURL(query);
		return loaded;
	}
}
