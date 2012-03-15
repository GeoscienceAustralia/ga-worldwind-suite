package au.gov.ga.worldwind.animator.layers.other;

import gov.nasa.worldwind.util.Logging;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateWMSTiledImageLayer;

/**
 * @author tag
 * @version $Id: LandsatI3WMSLayer.java 9690 2009-03-26 19:10:26Z dcollins $
 */
public class ImmediateLandsatI3WMSLayer extends ImmediateWMSTiledImageLayer
{
    public ImmediateLandsatI3WMSLayer()
    {
        super(makeXmlState());

        this.setAvailableImageFormats(new String[] {"image/png", "image/dds"});
    }

    private static String makeXmlState()
    {
        //noinspection RedundantStringConstructorCall
        return new String(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<restorableState>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.NumEmptyLevels\">0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ImageFormat\">image/dds</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.DataCacheNameKey\">Earth/NASA LandSat I3 WMS All</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ServiceURLKey\">http://www.nasa.network.com/wms?SERVICE=WMS&amp;</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.Title\">i-cubed Landsat</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.NumLevels\">10</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.FormatSuffixKey\">.dds</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Latitude\">36.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LevelZeroTileDelta.Longitude\">36.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.DatasetNameKey\">esat</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.TileHeightKey\">512</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.TileWidthKey\">512</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avkey.LayerNames\">esat</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLatitude\">-90.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLatitude\">90.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MinLongitude\">-180.0</stateObject>"
                + "<stateObject name=\"gov.nasa.worldwind.avKey.Sector.MaxLongitude\">180.0</stateObject>"
                // Thu, 26 Mar 2009 00:00:00 GMT
                + "<stateObject name=\"gov.nasa.worldwind.avkey.ExpiryTime\">1238025600000</stateObject>"
                + "<stateObject name=\"Layer.Name\">i-cubed Landsat</stateObject>"
                + "<stateObject name=\"Layer.Enabled\">true</stateObject>"
                + "<stateObject name=\"TiledImageLayer.UseTransparentTextures\">true</stateObject>"
                + "<stateObject name=\"avlist\">"
                    + "<stateObject name=\"gov.nasa.worldwind.avkey.URLReadTimeout\">20000</stateObject>"
                + "</stateObject>"
            + "</restorableState>"
        );
    }

    public String toString()
    {
        return Logging.getMessage("layers.Earth.LandsatI3Layer.Name");
    }
}
