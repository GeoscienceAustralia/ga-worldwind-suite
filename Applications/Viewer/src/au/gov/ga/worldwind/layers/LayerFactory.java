package au.gov.ga.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.file.FileTiledImageLayer;
import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;
import au.gov.ga.worldwind.layers.nearestneighbor.NearestNeighborMaskTiledImageLayer;
import au.gov.ga.worldwind.layers.nearestneighbor.NearestNeighborFileTiledImageLayer;
import au.gov.ga.worldwind.layers.nearestneighbor.NearestNeighborTiledImageLayer;
import au.gov.ga.worldwind.layers.shapefile.surfaceshape.SurfaceShapeShapefileLayerFactory;
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

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");
		if ("WWTileService".equals(serviceName) || "TileService".equals(serviceName))
		{
			layer = new ExtendedTiledImageLayer(domElement, params);
		}
		else if ("MaskedTileService".equals(serviceName))
		{
			layer = new MaskTiledImageLayer(domElement, params);
		}
		else if ("FileTileService".equals(serviceName))
		{
			layer = new FileTiledImageLayer(domElement, params, false);
		}
		else if ("MaskedFileTileService".equals(serviceName))
		{
			layer = new FileTiledImageLayer(domElement, params, true);
		}
		else if ("NearestNeighborTileService".equals(serviceName))
		{
			layer = new NearestNeighborTiledImageLayer(domElement, params);
		}
		else if ("NearestNeighborMaskedTileService".equals(serviceName))
		{
			layer = new NearestNeighborMaskTiledImageLayer(domElement, params);
		}
		else if ("NearestNeighborFileTileService".equals(serviceName))
		{
			layer = new NearestNeighborFileTiledImageLayer(domElement, params, false);
		}
		else if ("NearestNeighborMaskedFileTileService".equals(serviceName))
		{
			layer = new NearestNeighborFileTiledImageLayer(domElement, params, true);
		}
		/*else if ("CombineMaskedTileService".equals(serviceName))
		{
			layer = new CombineMaskTiledImageLayer(domElement, params);
		}*/
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
