package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LayerLoader
{
	public static Object load(Object source) throws Exception
	{
		Document document = WWXML.openDocument(source);
		Element element = document.getDocumentElement();

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
