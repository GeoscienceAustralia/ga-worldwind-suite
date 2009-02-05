package layers.ga.gravity;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import layers.ga.GALayer;
import layers.mask.MaskTiledImageLayer;

public class GravityLayer extends GALayer
{
	public GravityLayer()
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
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/Gravity");
		params.setValue(AVKey.SERVICE, GALayer.getTilesScriptUrl());
		params.setValue(AVKey.DATASET_NAME, "gravity");
		params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
		params.setValue(AVKey.NUM_LEVELS, 4);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-52.0967490), Angle
				.fromDegreesLatitude(-5.5648340), Angle
				.fromDegreesLongitude(98.4274511), Angle
				.fromDegreesLongitude(170.5036628)));
		params.setValue(AVKey.TILE_URL_BUILDER, MaskTiledImageLayer
				.createDefaultUrlBuilder("tiles/gravity/image",
						"tiles/gravity/mask", ".jpg", ".png"));

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "GA Gravity";
	}
}
