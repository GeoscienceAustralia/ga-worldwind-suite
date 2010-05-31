package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.XMLUtil;

public class LayerLoader
{
	public static Object load(URL sourceURL, Object source) throws Exception
	{
		Element element = XMLUtil.getElementFromSource(source);

		AVList params = null;
		if (sourceURL != null)
		{
			params = new AVListImpl();
			params.setValue(AVKey.URL, sourceURL);
		}

		try
		{
			Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
			Layer layer = (Layer) factory.createFromConfigSource(element, params);
			if (layer != null)
				return layer;
		}
		catch (Exception e)
		{
		}

		try
		{
			Factory factory =
					(Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
			ElevationModel elevationModel =
					(ElevationModel) factory.createFromConfigSource(element, params);
			if (elevationModel != null)
				return elevationModel;
		}
		catch (Exception e)
		{
		}

		throw new Exception("Error reading file");
	}
}
