package au.gov.ga.worldwind.viewer.layers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.viewer.layers.screenoverlay.ScreenOverlayLayerFactory;

/**
 * An extension of the common {@link LayerFactory} that is able to instantiate
 * layers specific to the Viewer tool
 */
public class ViewerLayerFactory extends LayerFactory
{

	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		String layerType = WWXML.getText(domElement, "@layerType");
		if ("ScreenOverlayLayer".equals(layerType))
		{
			return ScreenOverlayLayerFactory.createScreenOverlayLayer(domElement, params);
		}
		return super.createFromLayerDocument(domElement, params);
	}
	
}
