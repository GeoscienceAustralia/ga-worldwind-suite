package au.gov.ga.worldwind.common.layers.volume;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

public class VolumeLayerFactory
{
	public static Layer createModelLayer(Element domElement, AVList params)
	{
		return new BasicVolumeLayer();
	}
}
