package layers.elevation.perpixel;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.wms.Capabilities;

import org.w3c.dom.Element;

public class ExtendedWMSBasicElevationModel extends WMSBasicElevationModel implements ExtendedElevationModel
{
	public ExtendedWMSBasicElevationModel(AVList params)
    {
        super(params);
    }

    public ExtendedWMSBasicElevationModel(Element domElement, AVList params)
    {
        super(domElement, params);
    }

    public ExtendedWMSBasicElevationModel(Capabilities caps, AVList params)
    {
        super(caps, params);
    }

    public ExtendedWMSBasicElevationModel(WMSCapabilities caps, AVList params)
    {
        super(caps, params);
    }

    public ExtendedWMSBasicElevationModel(String restorableStateInXml)
    {
    	super(restorableStateInXml);
    }
    
    public BufferWrapper getElevationsFromMemory(TileKey tileKey)
	{
		ElevationTile tile = getTileFromMemory(tileKey);
		if (tile == null)
			return null;
		return tile.getElevations();
	}
    
    @Override
    public void requestTile(TileKey key)
    {
    	super.requestTile(key);
    }
}
