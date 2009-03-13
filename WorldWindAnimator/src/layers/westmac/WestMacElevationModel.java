package layers.westmac;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;

import java.io.File;

import terrain.FileBasicElevationModel;

public class WestMacElevationModel extends FileBasicElevationModel
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
		params.setValue(AVKey.SERVICE, null);
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
		params.setValue(AVKey.TILE_URL_BUILDER, new FileUrlBuilder(new File(
				"F:/West Macs Imagery/wwtiles/dem150")));

		return new LevelSet(params);
	}
}
