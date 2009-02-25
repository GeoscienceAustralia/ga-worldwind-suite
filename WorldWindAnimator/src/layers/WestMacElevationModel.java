/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package layers;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.BasicElevationModel;
import gov.nasa.worldwind.util.LevelSet;

/**
 * @author Tom Gaskins
 * @version $Id: EarthElevationModel.java 2664 2007-08-23 22:17:25Z tgaskins $
 */
public class WestMacElevationModel extends BasicElevationModel
{
	private static double HEIGHT_OF_MT_EVEREST = 8850d; // meters
	private static double DEPTH_OF_MARIANAS_TRENCH = -11000d; // meters

	public WestMacElevationModel()
	{
		super(makeLevels(), DEPTH_OF_MARIANAS_TRENCH, HEIGHT_OF_MT_EVEREST);
		this.setNumExpectedValuesPerTile(22500);
		String extremesFileName = Configuration
				.getStringValue("gov.nasa.worldwind.avkey.ExtremeElevations.SRTM30Plus.FileName");
		if (extremesFileName != null)
			this.loadExtremeElevations(extremesFileName);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 150);
		params.setValue(AVKey.TILE_HEIGHT, 150);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/WestMac DEM");
		params.setValue(AVKey.SERVICE, "http://localhost/tiles/westmac.php");
		params.setValue(AVKey.DATASET_NAME, "westmac");
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		params.setValue(AVKey.NUM_LEVELS, 11);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
		params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS,
				new LevelSet.SectorResolution[] {
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-25.0001389, -23.0001389, 131.9998611,
								133.9998611), 10), //WESTMAC
						new LevelSet.SectorResolution(Sector.FULL_SPHERE, 0) // SRTM30Plus
				});

		return new LevelSet(params);
	}
}
