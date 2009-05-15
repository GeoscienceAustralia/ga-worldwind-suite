package layers.other;

import layers.immediate.ImmediateWMSTiledImageLayer;
import gov.nasa.worldwind.util.Logging;

public class ImmediateBMNGWMSLayer extends ImmediateWMSTiledImageLayer
{
	private static final int DEFAULT_MONTH = 5;

	public ImmediateBMNGWMSLayer()
	{
		this(DEFAULT_MONTH);
	}

	public ImmediateBMNGWMSLayer(int month)
	{
		super(makeXmlState(month));

		this.setForceLevelZeroLoads(true);
		this.setRetainLevelZeroTiles(true);
		this
				.setAvailableImageFormats(new String[] { "image/png",
						"image/dds" });
	}

	private static String makeXmlState(int month)
	{
		String m = month + "";
		String mm = String.format("%02d", month);
		String layerTitle = "BlueMarble (WMS) " + mm + "/2004";
		String layerName = "bmng2004" + mm;

		//noinspection RedundantStringConstructorCall
		return new String(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<restorableState>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.NumEmptyLevels\">0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.ImageFormat\">image/dds</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.DataCacheNameKey\">Earth/BMNGWMS/BMNG(Shaded + Bathymetry) Tiled - Version 1.1 - "
						+ m
						+ ".2004</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.ServiceURLKey\">http://www.nasa.network.com/wms?SERVICE=WMS&amp;</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.Title\">"
						+ layerTitle
						+ "</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.NumLevels\">5</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.FormatSuffixKey\">.dds</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Latitude\">36.0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Longitude\">36.0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.DatasetNameKey\">"
						+ layerName
						+ "</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.TileHeightKey\">512</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.TileWidthKey\">512</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.LayerNames\">"
						+ layerName
						+ "</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLatitude\">-90.0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLatitude\">90.0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLongitude\">-180.0</stateObject>"
						+ "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLongitude\">180.0</stateObject>"
						// Thu, 26 Mar 2009 00:00:00 GMT
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.ExpiryTime\">1238025600000</stateObject>"
						+ "<stateObject name=\"Layer.Name\">"
						+ layerTitle
						+ "</stateObject>"
						+ "<stateObject name=\"Layer.Enabled\">true</stateObject>"
						+ "<stateObject name=\"TiledImageLayer.UseTransparentTextures\">false</stateObject>"
						+ "<stateObject name=\"avlist\">"
						+ "<stateObject name=\"gov.nasa.worldwind.avkey.URLReadTimeout\">30000</stateObject>"
						+ "</stateObject>" + "</restorableState>");
	}

	public String toString()
	{
		return Logging.getMessage("layers.Earth.BlueMarbleWMSLayer.Name");
	}
}
