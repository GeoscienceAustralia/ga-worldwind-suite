package layers.radiometry;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import layers.mask.MaskTiledImageLayer;

public class TernaryLayer extends MaskTiledImageLayer
{
	public TernaryLayer()
	{
		super(makeLevels());
		this.setForceLevelZeroLoads(true);
		this.setRetainLevelZeroTiles(true);
		this.setUseMipMaps(true);
		this.setUseTransparentTextures(true);
		//this.setOpacity(0.5);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params
				.setValue(AVKey.DATA_CACHE_NAME,
						"GA/Radiometrics/KThU_100m_he_rgb");
		params.setValue(AVKey.SERVICE,
				"http://sandpit:8500/map/web3d/worldwind/scripts/tiles.php");
		params.setValue(AVKey.DATASET_NAME, "radio_KThU_100m_he_rgb");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
		params.setValue(AVKey.NUM_LEVELS, 7);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(36d), Angle.fromDegrees(36d)));
		params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-43.7605000), Angle
				.fromDegreesLatitude(-8.9995000), Angle
				.fromDegreesLongitude(112.8995000), Angle
				.fromDegreesLongitude(153.6705000)));
		params.setValue(AVKey.TILE_URL_BUILDER, MaskTiledImageLayer
				.createDefaultUrlBuilder("tiles/KThU_100m_he_rgb",
						"tiles/radio_mask", ".jpg", ".png"));

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "Ternary";
	}
}
