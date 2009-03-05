/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package layers;

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
	private static double HIGHEST_POINT = 1515d; // meters
	private static double LOWEST_POINT = 308d; // meters

	public WestMacElevationModel()
	{
		super(makeLevels(), LOWEST_POINT, HIGHEST_POINT);
		this.setNumExpectedValuesPerTile(22500);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 150);
		params.setValue(AVKey.TILE_HEIGHT, 150);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/WestMac DEM");
		params.setValue(AVKey.SERVICE, "http://localhost/tiles/westmac.php");
		params.setValue(AVKey.DATASET_NAME, "dem150");
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		params.setValue(AVKey.NUM_LEVELS, 11);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, Sector.fromDegrees(-25.0001389,
				-23.0001389, 131.9998611, 133.9998611));
		/*params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
		params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS,
				new LevelSet.SectorResolution[] {
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-25.0001389, -23.0001389, 131.9998611,
								133.9998611), 10), //WESTMAC
						new LevelSet.SectorResolution(Sector.FULL_SPHERE, 0) });*/

		return new LevelSet(params);
	}
}
