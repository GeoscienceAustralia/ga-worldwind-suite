package au.gov.ga.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.borehole.BoreholeLayerFactory;
import au.gov.ga.worldwind.common.layers.crust.CrustLayer;
import au.gov.ga.worldwind.common.layers.curtain.BasicTiledCurtainLayer;
import au.gov.ga.worldwind.common.layers.curtain.delegate.DelegatorTiledCurtainLayer;
import au.gov.ga.worldwind.common.layers.earthquakes.HistoricEarthquakesLayer;
import au.gov.ga.worldwind.common.layers.geometry.GeometryLayerFactory;
import au.gov.ga.worldwind.common.layers.kml.KMLLayer;
import au.gov.ga.worldwind.common.layers.model.ModelLayerFactory;
import au.gov.ga.worldwind.common.layers.point.PointLayerFactory;
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
		if ("PointLayer".equals(layerType))
		{
			return PointLayerFactory.createPointLayer(domElement, params);
		}
		if ("KMLLayer".equals(layerType))
		{
			return new KMLLayer(domElement, params);
		}
		if ("CurtainImageLayer".equals(layerType))
		{
			return createTiledCurtainLayer(domElement, params);
		}
		if ("HistoricEarthquakesLayer".equals(layerType))
		{
			return new HistoricEarthquakesLayer(domElement, params);
		}
		if ("CrustLayer".equals(layerType))
		{
			return new CrustLayer(domElement, params);
		}
		if ("GeometryLayer".equalsIgnoreCase(layerType))
		{
			return GeometryLayerFactory.createGeometryLayer(domElement, params);
		}
		if ("BoreholeLayer".equalsIgnoreCase(layerType))
		{
			return BoreholeLayerFactory.createBoreholeLayer(domElement, params);
		}
		if ("ModelLayer".equalsIgnoreCase(layerType))
		{
			return ModelLayerFactory.createModelLayer(domElement, params);
		}

		return super.createFromLayerDocument(domElement, params);
	}

	protected Layer createTiledCurtainLayer(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName))
		{
			layer = new DelegatorTiledCurtainLayer(domElement, params);
		}
		else
		{
			layer = new BasicTiledCurtainLayer(domElement, params);
		}

		params = TimedExpirationHandler.getExpirationParams(domElement, params);
		TimedExpirationHandler.registerLayer(layer, params);

		return layer;
	}

	@Override
	protected Layer createTiledImageLayer(Element domElement, AVList params)
	{
		//overridden to allow extra service names for the TiledImageLayer type

		if (params == null)
			params = new AVListImpl();

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName) || OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
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
