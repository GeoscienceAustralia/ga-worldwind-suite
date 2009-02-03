package layers.radioareas;

import layers.GALayerUtil;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;

public class AreasLayerUtil
{
	public static AVList makeParams()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.SERVICE, GALayerUtil.getTilesScriptUrl());
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
		params.setValue(AVKey.SECTOR_RESOLUTION_LIMITS,
				new LevelSet.SectorResolution[] {
						//central_aus_kthu_dose
						new LevelSet.SectorResolution(Sector
								.fromDegrees(-24.6384983, -22.9365,
										131.6554999, 134.3624972), 6),
						//flinders_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-32.7015, -31.2725, 137.7465, 139.6415), 6),
						//mt_isa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-21.5825, -20.0795, 139.0075, 141.3865), 6),
						//ne_tas_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-41.4675, -40.7205, 147.3005, 148.3775), 6),
						//nsw_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-34.0515, -32.3335, 147.9995, 150.0035), 6),
						//nw_sa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-27.6195, -25.7835, 129.4625, 132.1915), 6),
						//vic_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-37.4895, -36.2735, 143.3015, 145.2865), 6),
						//wa_kthu_dose
						new LevelSet.SectorResolution(Sector.fromDegrees(
								-22.0555, -19.9995, 117.9925, 121.1775), 6),
						new LevelSet.SectorResolution(Sector.FULL_SPHERE, 0) });

		return params;
	}
}
