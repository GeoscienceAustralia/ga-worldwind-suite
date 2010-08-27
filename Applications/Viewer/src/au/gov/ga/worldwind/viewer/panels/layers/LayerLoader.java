package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

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
		Exception exception = null;

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
				//if (exception == null)
				exception = e;
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
				if (exception == null)
					exception = e;
			}
		}

		if (o == null)
		{
			if (exception != null)
				throw exception;
			throw new Exception("Error reading file");
		}

		LoadedLayer loaded = new LoadedLayer(o, params);
		loaded.setLegendURL(legend);
		loaded.setQueryURL(query);
		return loaded;
	}
}
