package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.XMLUtil;

public class LayerLoader
{
	public static Object load(Object source) throws Exception
	{
		Element element = XMLUtil.getElementFromSource(source);

		try
		{
			Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
			Layer layer = (Layer) factory.createFromConfigSource(element, null);
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
					(ElevationModel) factory.createFromConfigSource(element, null);
			if (elevationModel != null)
				return elevationModel;
		}
		catch (Exception e)
		{
		}

		throw new Exception("Error reading file");
	}
}
