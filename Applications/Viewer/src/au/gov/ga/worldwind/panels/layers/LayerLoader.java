package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.io.InputStream;

public class LayerLoader
{
	public static Object load(Object source) throws Exception
	{
		try
		{
			Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
			Layer layer = (Layer) factory.createFromConfigSource(source, null);
			if (layer != null)
				return layer;
		}
		catch (Exception e)
		{
		}

		try
		{
			if (source instanceof InputStream)
				((InputStream) source).reset();

			Factory factory =
					(Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
			ElevationModel elevationModel =
					(ElevationModel) factory.createFromConfigSource(source, null);
			if (elevationModel != null)
				return elevationModel;
		}
		catch (Exception e)
		{
		}

		throw new Exception("Could not read file!");
	}
}
