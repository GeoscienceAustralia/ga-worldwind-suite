package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;

public class LayerLoader
{
	public static Object load(Object source)
	{
		return new BMNGWMSLayer();
	}
}
