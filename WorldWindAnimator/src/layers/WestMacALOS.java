package layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;

public class WestMacALOS extends MaskTiledImageLayer
{
	public WestMacALOS()
	{
		super(makeLevels());
		this.setForceLevelZeroLoads(true);
		this.setRetainLevelZeroTiles(true);
		this.setUseMipMaps(true);
		this.setUseTransparentTextures(true);
	}

	private static LevelSet makeLevels()
	{
		//TODO

		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/WestMac ALOS");
		params.setValue(AVKey.SERVICE, "http://localhost/tiles/westmac.php");
		params.setValue(AVKey.DATASET_NAME, "alosnp_4326");
		params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
		params.setValue(AVKey.NUM_LEVELS, 13);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		/*params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-24.2057779), Angle
				.fromDegreesLatitude(-23.1988560), Angle
				.fromDegreesLongitude(132.0001334), Angle
				.fromDegreesLongitude(134.1394049)));*/
		params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-24.0), Angle
				.fromDegreesLatitude(-23.433333), Angle
				.fromDegreesLongitude(132.25), Angle
				.fromDegreesLongitude(133.95)));

		params.setValue(AVKey.TILE_URL_BUILDER, MaskTiledImageLayer
				.createDefaultUrlBuilder("tiles/westmac/image",
						"tiles/westmac/mask", ".jpg", ".png"));

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "WestMac ALOS imagery";
	}
}
