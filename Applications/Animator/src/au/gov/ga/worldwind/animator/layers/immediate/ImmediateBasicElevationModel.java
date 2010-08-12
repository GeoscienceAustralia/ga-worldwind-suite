package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.animator.layers.elevation.perpixel.ExtendedBasicElevationModel;

public class ImmediateBasicElevationModel extends ExtendedBasicElevationModel
{
	private boolean elevationWasRequested;

	public ImmediateBasicElevationModel(LevelSet levels)
	{
		super(levels);
	}

	public ImmediateBasicElevationModel(AVList params)
	{
		super(params);
	}

	public ImmediateBasicElevationModel(String stateInXml)
	{
		super(stateInXml);
	}

	@Override
	public double getElevations(Sector sector, List<? extends LatLon> latlons,
			double targetResolution, double[] buffer)
	{
		if (!ImmediateMode.isImmediate())
		{
			return super.getElevations(sector, latlons, targetResolution,
					buffer);
		}

		double value;
		do
		{
			elevationWasRequested = false;
			value = super.getElevations(sector, latlons, targetResolution,
					buffer);
		} while (elevationWasRequested);
		return value;
	}

	@Override
	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		if (!ImmediateMode.isImmediate())
		{
			return super.getUnmappedElevation(latitude, longitude);
		}

		double value;
		do
		{
			elevationWasRequested = false;
			value = super.getUnmappedElevation(latitude, longitude);
		} while (elevationWasRequested);
		return value;
	}

	@Override
	public void requestTile(TileKey key)
	{
		if (!ImmediateMode.isImmediate())
		{
			super.requestTile(key);
			return;
		}

		elevationWasRequested = true;

		// check to ensure load is still needed
		if (areElevationsInMemory(key))
			return;

		try
		{
			ElevationTile tile = createTile(key);
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

	private boolean loadTileFromStore(ElevationTile tile) throws IOException
	{
		// from BasicElevationModel.requestTask.run()

		final URL url = getDataFileStore().findFile(tile.getPath(), false);
		if (url != null && !isFileExpired(tile, url, getDataFileStore()))
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
				getDataFileStore().removeFile(url);
				getLevels().markResourceAbsent(tile);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", url);
				Logging.logger().info(message);
			}
		}
		return false;
	}
}
