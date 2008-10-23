package layers.radiometry;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import mask.MaskLevelSet;
import mask.MaskTiledImageLayer;

public class AreasLayer extends MaskTiledImageLayer
{
	public AreasLayer()
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
		params.setValue(AVKey.DATA_CACHE_NAME,
				"GA/Radiometry/images_8areas_100m");
		params.setValue(AVKey.SERVICE, "http://localhost/wwtiles/areas.php");
		params.setValue(AVKey.DATASET_NAME, "images_8areas_100m");
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
		params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS,
				new LevelSet.SectorResolution[] {
						//central_aus_kthu_dose
						new LevelSet.SectorResolution(Sector
								.fromDegrees(-24.6384983, -22.9365,
										131.6554999, 134.3624972), 7),
						//flinders_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-32.7015, -31.2725, 137.7465, 139.6415), 7),
						//mt_isa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-21.5825, -20.0795, 139.0075, 141.3865), 7),
						//ne_tas_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-41.4675, -40.7205, 147.3005, 148.3775), 7),
						//nsw_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-34.0515, -32.3335, 147.9995, 150.0035), 7),
						//nw_sa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-27.6195, -25.7835, 129.4625, 132.1915), 7),
						//vic_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-37.4895, -36.2735, 143.3015, 145.2865), 7),
						//wa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-22.0555, -19.9995, 117.9925, 121.1775), 7),
						new LevelSet.SectorResolution(Sector.FULL_SPHERE, 0) });

		params.setValue(MaskLevelSet.MASK_CACHE_NAME,
				"GA/Radiometry/images_8areas_100m_mask");
		params.setValue(MaskLevelSet.MASK_FORMAT_SUFFIX, ".png");
		params.setValue(MaskLevelSet.MASK_SERVICE,
				"http://localhost/wwtiles/areas_mask.php");

		return new MaskLevelSet(params);
	}

	@Override
	public String toString()
	{
		return "GA Radiometry - Areas of Interest";
	}
}
