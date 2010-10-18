package au.gov.ga.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.kml.KMLLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerUtils;
import au.gov.ga.worldwind.common.layers.shapefile.surfaceshape.ShapefileLayerFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTiledImageLayer;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Extension to World Wind's {@link BasicLayerFactory} which allows creation of
 * extra layer types.
 * 
 * @author Michael de Hoog
 */
public class LayerFactory extends BasicLayerFactory
{
	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		//overridden to allow extra layer types

		String layerType = WWXML.getText(domElement, "@layerType");
		if ("SurfaceShapeShapefileLayer".equals(layerType))
		{
			return ShapefileLayerFactory.createLayer(domElement, params);
		}
		else if ("PointLayer".equals(layerType))
		{
			return PointLayerUtils.createPointLayer(domElement, params);
		}
		else if ("KMLLayer".equals(layerType))
		{
			return new KMLLayer(domElement, params);
		}

		return super.createFromLayerDocument(domElement, params);
	}

	@Override
	protected Layer createTiledImageLayer(Element domElement, AVList params)
	{
		//overridden to allow extra service names for the TiledImageLayer type

		if (params == null)
			params = new AVListImpl();

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName)
				|| OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			layer = new DelegatorTiledImageLayer(domElement, params);
		}
		else
		{
			layer = super.createTiledImageLayer(domElement, params);
			if (params.getValue(AVKey.SECTOR) != null)
				layer.setValue(AVKey.SECTOR, params.getValue(AVKey.SECTOR));
		}

		params = TimedExpirationHandler.getExpirationParams(domElement, params);
		TimedExpirationHandler.registerLayer(layer, params);

		return layer;
	}
}
