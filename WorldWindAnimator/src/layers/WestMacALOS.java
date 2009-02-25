package layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import au.gov.ga.worldwind.layers.ga.GALayer;
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
		this.setSplitScale(GALayer.getSplitScale());
	}

	private static LevelSet makeLevels()
	{
		//TODO
		
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/WestMac ALOS");
		params.setValue(AVKey.SERVICE, GALayer.getTilesScriptUrl());
		params.setValue(AVKey.DATASET_NAME, "alos");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
		params.setValue(AVKey.NUM_LEVELS, 13);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-23.198856), Angle
				.fromDegreesLatitude(-5.5648340), Angle
				.fromDegreesLongitude(132.022453), Angle
				.fromDegreesLongitude(170.5036628)));
		params.setValue(AVKey.TILE_URL_BUILDER, MaskTiledImageLayer
				.createDefaultUrlBuilder("tiles/gravity/image",
						"tiles/gravity/mask", ".jpg", ".png"));

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "WestMac ALOS imagery";
	}
}
