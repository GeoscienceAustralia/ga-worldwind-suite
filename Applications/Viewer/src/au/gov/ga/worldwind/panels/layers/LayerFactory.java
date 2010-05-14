package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;

public class LayerFactory extends BasicLayerFactory
{
	@Override
	protected Layer createTiledImageLayer(Element domElement, AVList params)
	{
		String serviceName = WWXML.getText(domElement, "Service/@serviceName");
		if (serviceName != null && serviceName.equals("MaskedTileService"))
		{
			return new MaskTiledImageLayer(domElement, params);
		}
		return super.createTiledImageLayer(domElement, params);
	}
}
