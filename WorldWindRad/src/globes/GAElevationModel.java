/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package globes;

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
public class GAElevationModel extends BasicElevationModel
{
	private static double HEIGHT_OF_MT_EVEREST = 8850d; // meters
	private static double DEPTH_OF_MARIANAS_TRENCH = -11000d; // meters

	public GAElevationModel()
	{
		super(makeLevels(), DEPTH_OF_MARIANAS_TRENCH, HEIGHT_OF_MT_EVEREST);
		this.setNumExpectedValuesPerTile(150 * 150);
		/*String extremesFileName =
		    Configuration.getStringValue("gov.nasa.worldwind.avkey.ExtremeElevations.SRTM30Plus.FileName");
		if (extremesFileName != null)
		    this.loadExtremeElevations(extremesFileName);*/
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 150);
		params.setValue(AVKey.TILE_HEIGHT, 150);
		params.setValue(AVKey.DATA_CACHE_NAME,
				"GA/Radiometry/srtmbathyzip");
		params.setValue(AVKey.SERVICE, "http://localhost/wwtiles/srtm.php");
		params.setValue(AVKey.DATASET_NAME, "srtmbathyzip");
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		params.setValue(AVKey.NUM_LEVELS, 9);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		/*params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-52.0049824), Angle
				.fromDegreesLatitude(-7.9950000), Angle
				.fromDegreesLongitude(101.9950000), Angle
				.fromDegreesLongitude(172.0049720)));*/
		
		params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
		params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS,
				new LevelSet.SectorResolution[] {
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-52.0049824, -7.9950000, 101.9950000, 172.0049720), 8),
						new LevelSet.SectorResolution(Sector.FULL_SPHERE, 0) });

		return new LevelSet(params);
	}
}
