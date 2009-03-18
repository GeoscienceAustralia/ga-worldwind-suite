package layers.immediate;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.io.IOException;
import java.net.URL;

import nasa.worldwind.terrain.BasicElevationModel;

public class ImmediateBasicElevationModel extends BasicElevationModel
{
	public ImmediateBasicElevationModel(LevelSet levels, double minElevation,
			double maxElevation)
	{
		super(levels, minElevation, maxElevation);
	}

	public ImmediateBasicElevationModel(AVList params, double minElevation,
			double maxElevation)
	{
		super(params, minElevation, maxElevation);
	}

	public ImmediateBasicElevationModel(String stateInXml)
	{
		super(stateInXml);
	}

	public ImmediateBasicElevationModel(AVList params,
			double[] minAndMaxElevation)
	{
		super(params, minAndMaxElevation);
	}

	@Override
	protected void requestTile(TileKey key)
	{
		if (!ImmediateMode.isImmediate())
		{
			super.requestTile(key);
			return;
		}

		// check to ensure load is still needed
		if (areElevationsInMemory(key))
			return;

		try
		{
			Tile tile = createTile(key);
			if (!loadTileFromStore(tile))
				downloadElevations(tile);
			loadTileFromStore(tile);
		}
		catch (IOException e)
		{
			String msg = Logging.getMessage(
					"ElevationModel.ExceptionRequestingElevations", key
							.toString());
			Logging.logger().log(java.util.logging.Level.FINE, msg, e);
		}
	}

	private boolean loadTileFromStore(Tile tile) throws IOException
	{
		//from BasicElevationModel.requestTask.run()
		
		final URL url = WorldWind.getDataFileStore().findFile(tile.getPath(),
				false);
		if (url != null)
		{
			if (loadElevations(tile, url))
			{
				getLevels().unmarkResourceAbsent(tile);
				firePropertyChange(AVKey.ELEVATION_MODEL, null, this);
				return true;
			}
			else
			{
				// Assume that something's wrong with the file and delete it.
				WorldWind.getDataFileStore().removeFile(url);
				getLevels().markResourceAbsent(tile);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", url);
				Logging.logger().info(message);
			}
		}
		return false;
	}
}
