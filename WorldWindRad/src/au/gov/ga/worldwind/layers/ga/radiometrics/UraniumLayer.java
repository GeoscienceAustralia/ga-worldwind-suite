package au.gov.ga.worldwind.layers.ga.radiometrics;

import au.gov.ga.worldwind.layers.ga.GALayer;
import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;

public class UraniumLayer extends GALayer
{
	public UraniumLayer()
	{
		super(makeLevels());
		this.setForceLevelZeroLoads(true);
		this.setRetainLevelZeroTiles(true);
		this.setUseMipMaps(true);
		this.setUseTransparentTextures(true);
		this.setSplitScale(GALayer.getGASplitScale());
	}

	private static LevelSet makeLevels()
	{
		AVList params = RadioLayerUtil.makeParams();
		String layerName = "radio_U_100m_he_rgb";

		params.setValue(AVKey.DATA_CACHE_NAME, "GA/Radiometrics/" + layerName);
		params.setValue(AVKey.DATASET_NAME, layerName);
		params.setValue(AVKey.TILE_URL_BUILDER, MaskTiledImageLayer
				.createDefaultUrlBuilder("tiles/radiometrics/" + layerName,
						"tiles/radiometrics/radio_mask", ".jpg", ".png"));

		return new LevelSet(params);
	}

	@Override
	public String toString()
	{
		return "Uranium (U)";
	}
}
