package au.gov.ga.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.shapefile.surfaceshape.SurfaceShapeShapefileLayerFactory;
import au.gov.ga.worldwind.layers.tiled.image.delegate.DelegateKit;
import au.gov.ga.worldwind.layers.tiled.image.delegate.DelegatorTiledImageLayer;
import au.gov.ga.worldwind.layers.tiled.image.delegate.LocalRequesterDelegate;
import au.gov.ga.worldwind.layers.tiled.image.delegate.MaskImageReaderDelegate;
import au.gov.ga.worldwind.layers.tiled.image.delegate.nearestneighbor.NearestNeighborTextureTileFactoryDelegate;
import au.gov.ga.worldwind.util.XMLUtil;

public class LayerFactory extends BasicLayerFactory
{
	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		//overridden to allow extra layer types

		String layerType = WWXML.getText(domElement, "@layerType");
		if ("SurfaceShapeShapefileLayer".equals(layerType))
		{
			return SurfaceShapeShapefileLayerFactory.createLayer(domElement, params);
		}

		return super.createFromLayerDocument(domElement, params);
	}

	@Override
	protected Layer createTiledImageLayer(Element domElement, AVList params)
	{
		//overridden to allow extra service names for the TiledImageLayer type

		if (params == null)
			params = new AVListImpl();

		DelegateKit kit = DelegateKit.defaultDelegateKit();
		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");
		if ("WWTileService".equals(serviceName) || "TileService".equals(serviceName))
		{
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("MaskedTileService".equals(serviceName))
		{
			kit.setReaderDelegate(new MaskImageReaderDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("FileTileService".equals(serviceName))
		{
			kit.setRequesterDelegate(new LocalRequesterDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("MaskedFileTileService".equals(serviceName))
		{
			kit.setReaderDelegate(new MaskImageReaderDelegate());
			kit.setRequesterDelegate(new LocalRequesterDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("NearestNeighborTileService".equals(serviceName))
		{
			kit.setFactoryDelegate(new NearestNeighborTextureTileFactoryDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("NearestNeighborMaskedTileService".equals(serviceName))
		{
			kit.setReaderDelegate(new MaskImageReaderDelegate());
			kit.setFactoryDelegate(new NearestNeighborTextureTileFactoryDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("NearestNeighborFileTileService".equals(serviceName))
		{
			kit.setRequesterDelegate(new LocalRequesterDelegate());
			kit.setFactoryDelegate(new NearestNeighborTextureTileFactoryDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
		}
		else if ("NearestNeighborMaskedFileTileService".equals(serviceName))
		{
			kit.setRequesterDelegate(new LocalRequesterDelegate());
			kit.setReaderDelegate(new MaskImageReaderDelegate());
			kit.setFactoryDelegate(new NearestNeighborTextureTileFactoryDelegate());
			layer = new DelegatorTiledImageLayer(domElement, params, kit);
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
