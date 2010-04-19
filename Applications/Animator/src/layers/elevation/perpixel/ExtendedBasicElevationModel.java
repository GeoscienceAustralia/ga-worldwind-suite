package layers.elevation.perpixel;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.terrain.BasicElevationModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtendedBasicElevationModel extends BasicElevationModel implements ExtendedElevationModel
{
	public ExtendedBasicElevationModel(AVList params)
	{
		super(params);
	}

	public ExtendedBasicElevationModel(Document dom, AVList params)
	{
		super(dom, params);
	}

	public ExtendedBasicElevationModel(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	public ExtendedBasicElevationModel(String restorableStateInXml)
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
