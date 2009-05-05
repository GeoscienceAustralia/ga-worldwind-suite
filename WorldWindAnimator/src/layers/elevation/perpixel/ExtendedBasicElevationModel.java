package layers.elevation.perpixel;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.TileKey;
import nasa.worldwind.terrain.BasicElevationModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtendedBasicElevationModel extends BasicElevationModel
{
	//private List<ElevationLoadListener> elevationLoadListeners = new ArrayList<ElevationLoadListener>();
	//private BufferWrapper lastElevations;

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
		Tile tile = getTileFromMemory(tileKey);
		if (tile == null)
			return null;
		return tile.getElevations();
	}

	@Override
	public void requestTile(TileKey key)
	{
		super.requestTile(key);
	}

	/*@Override
	protected synchronized boolean loadElevations(Tile tile, URL url)
			throws IOException
	{
		boolean loaded = super.loadElevations(tile, url);
		notifyElevationLoadListeners(tile, lastElevations);
		return loaded;
	}

	@Override
	protected synchronized BufferWrapper readElevations(URL url)
			throws IOException
	{
		lastElevations = super.readElevations(url);
		return lastElevations;
	}

	public void addElevationLoadListener(
			ElevationLoadListener elevationLoadListener)
	{
		elevationLoadListeners.add(elevationLoadListener);
	}

	public void removeElevationLoadListener(
			ElevationLoadListener elevationLoadListener)
	{
		elevationLoadListeners.remove(elevationLoadListener);
	}

	private void notifyElevationLoadListeners(Tile tile,
			BufferWrapper elevations)
	{
		for (ElevationLoadListener elevationLoadListener : elevationLoadListeners)
		{
			elevationLoadListener.elevationsLoaded(tile, elevations);
		}
	}*/
}
