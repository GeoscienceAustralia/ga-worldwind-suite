package layers.radiometry;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import mask.MaskLevelSet;
import mask.MaskTiledImageLayer;

public class ThoriumLayer extends MaskTiledImageLayer
{
	public ThoriumLayer()
	{
		super(makeLevels());
		this.setForceLevelZeroLoads(true);
		this.setRetainLevelZeroTiles(true);
		this.setUseMipMaps(true);
		this.setUseTransparentTextures(true);
		//this.setOpacity(0.5);
	}

	private static MaskLevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, "GA/Radiometry/Th_100m_he_rgb");
		params.setValue(AVKey.SERVICE, "http://localhost/wwtiles/tiles.php");
		params.setValue(AVKey.DATASET_NAME, "Th_100m_he_rgb");
		params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
		params.setValue(AVKey.NUM_LEVELS, 8);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(45d), Angle.fromDegrees(45d)));
		params.setValue(AVKey.SECTOR, new Sector(Angle
				.fromDegreesLatitude(-43.7605000), Angle
				.fromDegreesLatitude(-8.9995000), Angle
				.fromDegreesLongitude(112.8995000), Angle
				.fromDegreesLongitude(153.6705000)));
		
		params.setValue(MaskLevelSet.MASK_CACHE_NAME, "GA/Radiometry/mask");
		params.setValue(MaskLevelSet.MASK_FORMAT_SUFFIX, ".png");
		params.setValue(MaskLevelSet.MASK_SERVICE, "http://localhost/wwtiles/mask.php");

		return new MaskLevelSet(params);
	}
	
	@Override
	public String toString()
	{
		return "GA Radiometry - Thorium";
	}
}
