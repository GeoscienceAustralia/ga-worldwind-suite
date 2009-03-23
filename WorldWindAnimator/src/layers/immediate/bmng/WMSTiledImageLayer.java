/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package layers.immediate.bmng;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.wms.BoundingBox;
import gov.nasa.worldwind.wms.Capabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import layers.immediate.ImmediateBasicTiledImageLayer;

import org.w3c.dom.Element;

/**
 * @author tag
 * @version $Id: WMSTiledImageLayer.java 8441 2009-01-14 17:00:10Z dcollins $
 */
public class WMSTiledImageLayer extends ImmediateBasicTiledImageLayer
{
    private AVList creationParams;

    public static AVList xmlStateToParams(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        AVList params = new AVListImpl();

        String s = rs.getStateValueAsString(AVKey.IMAGE_FORMAT);
        if (s != null)
            params.setValue(AVKey.IMAGE_FORMAT, s);

        s = rs.getStateValueAsString(AVKey.DATA_CACHE_NAME);
        if (s != null)
            params.setValue(AVKey.DATA_CACHE_NAME, s);

        s = rs.getStateValueAsString(AVKey.SERVICE);
        if (s != null)
            params.setValue(AVKey.SERVICE, s);

        s = rs.getStateValueAsString(AVKey.TITLE);
        if (s != null)
            params.setValue(AVKey.TITLE, s);

        s = rs.getStateValueAsString(AVKey.DATASET_NAME);
        if (s != null)
            params.setValue(AVKey.DATASET_NAME, s);

        s = rs.getStateValueAsString(AVKey.FORMAT_SUFFIX);
        if (s != null)
            params.setValue(AVKey.FORMAT_SUFFIX, s);

        s = rs.getStateValueAsString(AVKey.LAYER_NAMES);
        if (s != null)
            params.setValue(AVKey.LAYER_NAMES, s);

        s = rs.getStateValueAsString(AVKey.STYLE_NAMES);
        if (s != null)
            params.setValue(AVKey.STYLE_NAMES, s);

        Integer i = rs.getStateValueAsInteger(AVKey.NUM_EMPTY_LEVELS);
        if (i != null)
            params.setValue(AVKey.NUM_EMPTY_LEVELS, i);

        i = rs.getStateValueAsInteger(AVKey.NUM_LEVELS);
        if (i != null)
            params.setValue(AVKey.NUM_LEVELS, i);

        i = rs.getStateValueAsInteger(AVKey.TILE_WIDTH);
        if (i != null)
            params.setValue(AVKey.TILE_WIDTH, i);

        i = rs.getStateValueAsInteger(AVKey.TILE_HEIGHT);
        if (i != null)
            params.setValue(AVKey.TILE_HEIGHT, i);

        Double d = rs.getStateValueAsDouble(AVKey.EXPIRY_TIME);
        if (d != null)
            params.setValue(AVKey.EXPIRY_TIME, Math.round(d));

        Double lat = rs.getStateValueAsDouble(AVKey.LEVEL_ZERO_TILE_DELTA + ".Latitude");
        Double lon = rs.getStateValueAsDouble(AVKey.LEVEL_ZERO_TILE_DELTA + ".Longitude");
        if (lat != null && lon != null)
            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(lat, lon));

        Double minLat = rs.getStateValueAsDouble(AVKey.SECTOR + ".MinLatitude");
        Double minLon = rs.getStateValueAsDouble(AVKey.SECTOR + ".MinLongitude");
        Double maxLat = rs.getStateValueAsDouble(AVKey.SECTOR + ".MaxLatitude");
        Double maxLon = rs.getStateValueAsDouble(AVKey.SECTOR + ".MaxLongitude");
        if (minLat != null && minLon != null && maxLat != null && maxLon != null)
            params.setValue(AVKey.SECTOR, Sector.fromDegrees(minLat, maxLat, minLon, maxLon));

        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(rs.getStateValueAsString("wms.Version"), params));

        return params;
    }

    public static WMSTiledImageLayer fromRestorableState(String stateInXml)
    {
        return new WMSTiledImageLayer(stateInXml);
    }

    public WMSTiledImageLayer(String stateInXml)
    {
        this(xmlStateToParams(stateInXml));

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        String s = rs.getStateValueAsString("Layer.Name");
        if (s != null)
            this.setName(s);

        Boolean b = rs.getStateValueAsBoolean("Layer.Enabled");
        if (b != null)
            this.setEnabled(b);

        Double d = rs.getStateValueAsDouble("Layer.Opacity");
        if (d != null)
            this.setOpacity(d);

        d = rs.getStateValueAsDouble("Layer.MinActiveAltitude");
        if (d != null)
            this.setMinActiveAltitude(d);

        d = rs.getStateValueAsDouble("Layer.MaxActiveAltitude");
        if (d != null)
            this.setMaxActiveAltitude(d);

        b = rs.getStateValueAsBoolean("Layer.NetworkRetrievalEnabled");
        if (b != null)
            this.setNetworkRetrievalEnabled(b);

        b = rs.getStateValueAsBoolean("TiledImageLayer.UseTransparentTextures");
        if (b != null)
            this.setUseTransparentTextures(b);
        
        RestorableSupport.StateObject so = rs.getStateObject("avlist");
        if (so != null)
        {
            RestorableSupport.StateObject[] avpairs = rs.getAllStateObjects(so, "");
            for (RestorableSupport.StateObject avp : avpairs)
            {
                if (avp != null)
                    this.setValue(avp.getName(), avp.getValue());
            }
        }
    }

    public WMSTiledImageLayer(AVList params)
    {
        super(params);

        this.creationParams = params.copy();
    }

    public WMSTiledImageLayer(Capabilities caps, AVList params)
    {
        super(initParams(caps, params));

        this.setUseTransparentTextures(true);
        this.setName(
            makeTitle(caps, params.getStringValue(AVKey.LAYER_NAMES), params.getStringValue(AVKey.STYLE_NAMES)));
        this.creationParams = params.copy();
    }

    private static AVList initParams(Capabilities caps, AVList params)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.LayerConfigParams");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
        if (layerNames == null || layerNames.length() == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] names = layerNames.split(",");
        if (names == null || names.length == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String name : names)
        {
            if (caps.getLayerByName(name) == null)
            {
                String message = Logging.getMessage("WMS.LayerNameMissing", name);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        params.setValue(AVKey.DATASET_NAME, layerNames);

        String mapRequestURIString = caps.getGetMapRequestGetURL();
        mapRequestURIString = fixGetMapString(mapRequestURIString);
        if (params.getValue(AVKey.SERVICE) == null)
            params.setValue(AVKey.SERVICE, mapRequestURIString);
        mapRequestURIString = params.getStringValue(AVKey.SERVICE);
        if (mapRequestURIString == null || mapRequestURIString.length() == 0)
        {
            Logging.logger().severe("WMS.RequestMapURLMissing");
            throw new IllegalArgumentException(Logging.getMessage("WMS.RequestMapURLMissing"));
        }

        String styleNames = params.getStringValue(AVKey.STYLE_NAMES);
        if (params.getValue(AVKey.DATA_CACHE_NAME) == null)
        {
            try
            {
                URI mapRequestURI = new URI(mapRequestURIString);
                String cacheName = WWIO.formPath(mapRequestURI.getAuthority(), mapRequestURI.getPath(), layerNames,
                    styleNames);
                params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            }
            catch (URISyntaxException e)
            {
                String message = Logging.getMessage("WMS.RequestMapURLBad", mapRequestURIString);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        // Determine image format to request.
        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            String imageFormat = chooseImageFormat(caps);
            params.setValue(AVKey.IMAGE_FORMAT, imageFormat);
        }

        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            Logging.logger().severe("WMS.NoImageFormats");
            throw new IllegalArgumentException(Logging.getMessage("WMS.NoImageFormats"));
        }

        // Determine bounding sector.
        Sector sector = (Sector) params.getValue(AVKey.SECTOR);
        if (sector == null)
        {
            for (String name : names)
            {
                BoundingBox bb = caps.getLayerGeographicBoundingBox(caps.getLayerByName(name));
                if (bb == null)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, "WMS.NoGeographicBoundingBoxForLayer", name);
                    continue;
                }

                sector = Sector.union(sector, Sector.fromDegrees(
                    clamp(bb.getMiny(), -90d, 90d),
                    clamp(bb.getMaxy(), -90d, 90d),
                    clamp(bb.getMinx(), -180d, 180d),
                    clamp(bb.getMaxx(), -180d, 180d)));
            }

            if (sector == null)
            {
                Logging.logger().severe("WMS.NoGeographicBoundingBox");
                throw new IllegalArgumentException(Logging.getMessage("WMS.NoGeographicBoundingBox"));
            }
            params.setValue(AVKey.SECTOR, sector);
        }

        if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
        {
            Angle delta = Angle.fromDegrees(36);
            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
        }

        if (params.getValue(AVKey.TILE_WIDTH) == null)
            params.setValue(AVKey.TILE_WIDTH, 512);
        if (params.getValue(AVKey.TILE_HEIGHT) == null)
            params.setValue(AVKey.TILE_HEIGHT, 512);
        if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
            params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        if (params.getValue(AVKey.NUM_LEVELS) == null)
            params.setValue(AVKey.NUM_LEVELS, 19); // approximately 0.1 meters per pixel
        if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);

        // TODO: adjust for subsetable, fixedimage, etc.

        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(caps.getVersion(), params));

        return params;
    }

    private static double clamp(double v, double min, double max)
    {
        return v < min ? min : v > max ? max : v;
    }

    private static String fixGetMapString(String gms)
    {
        gms = gms.trim();
        int qMarkIndex = gms.indexOf("?");
        if (qMarkIndex < 0)
            gms += "?";
        else if (qMarkIndex != gms.length() - 1)
            if (gms.lastIndexOf("&") != gms.length() - 1)
                gms += "&";

        return gms;
    }

    private static String makeTitle(Capabilities caps, String layerNames, String styleNames)
    {
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            Element layer = caps.getLayerByName(layerName);
            String layerTitle = caps.getLayerTitle(layer);
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            Element style = caps.getLayerStyleByName(layer, styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = caps.getStyleTitle(layer, style);
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    private static final String[] formatOrderPreference = new String[]
        {
            "image/dds", "image/png", "image/jpeg"
        };

    private static String chooseImageFormat(Capabilities caps)
    {
        String[] formats = caps.getGetMapFormats();
        if (formats == null || formats.length == 0)
            return null;

        for (String s : formatOrderPreference)
        {
            for (String f : formats)
            {
                if (f.equalsIgnoreCase(s))
                    return f;
            }
        }

        return formats[0]; // none recognized; just use the first in the caps list
    }

    private RestorableSupport makeRestorableState(AVList params)
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (rs == null)
            return null;

        for (Map.Entry<String, Object> p : params.getEntries())
        {
            if (p.getValue() == null)
                continue;

            if (p.getValue() instanceof LatLon)
            {
                rs.addStateValueAsDouble(p.getKey() + ".Latitude", ((LatLon) p.getValue()).getLatitude().degrees);
                rs.addStateValueAsDouble(p.getKey() + ".Longitude", ((LatLon) p.getValue()).getLongitude().degrees);
            }
            else if (p.getValue() instanceof Sector)
            {
                rs.addStateValueAsDouble(p.getKey() + ".MinLatitude", ((Sector) p.getValue()).getMinLatitude().degrees);
                rs.addStateValueAsDouble(p.getKey() + ".MaxLatitude", ((Sector) p.getValue()).getMaxLatitude().degrees);
                rs.addStateValueAsDouble(p.getKey() + ".MinLongitude",
                    ((Sector) p.getValue()).getMinLongitude().degrees);
                rs.addStateValueAsDouble(p.getKey() + ".MaxLongitude",
                    ((Sector) p.getValue()).getMaxLongitude().degrees);
            }
            else if (p.getValue() instanceof URLBuilder)
            {
                rs.addStateValueAsString("wms.Version", ((URLBuilder) p.getValue()).wmsVersion);
                rs.addStateValueAsString("wms.Crs", ((URLBuilder) p.getValue()).crs);
            }
            else
            {
                rs.addStateValueAsString(p.getKey(), p.getValue().toString());
            }
        }

        rs.addStateValueAsBoolean("Layer.Enabled", this.isEnabled());
        rs.addStateValueAsString("Layer.Name", this.getName());
        rs.addStateValueAsDouble("Layer.Opacity", this.getOpacity());
        rs.addStateValueAsDouble("Layer.MinActiveAltitude", this.getMinActiveAltitude());
        rs.addStateValueAsDouble("Layer.MaxActiveAltitude", this.getMaxActiveAltitude());
        rs.addStateValueAsBoolean("Layer.NetworkRetrievalEnabled", this.isNetworkRetrievalEnabled());
        rs.addStateValueAsBoolean("TiledImageLayer.UseTransparentTextures", this.isUseTransparentTextures());

        RestorableSupport.StateObject so = rs.addStateObject("avlist");
        for (Map.Entry<String, Object> p : this.getEntries())
        {
            if (p.getValue() == null)
                continue;
            
            if (p.getKey().equals(AVKey.CONSTRUCTION_PARAMETERS))
                continue;

            rs.addStateValueAsString(so, p.getKey(), p.getValue().toString());
        }

        return rs;
    }

    public String getRestorableState()
    {
        return this.makeRestorableState(this.creationParams).getStateAsXml();
    }

    public void restoreState(String stateInXml)
    {
        String message = Logging.getMessage("RestorableSupport.RestoreRequiresConstructor");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }

    private static class URLBuilder implements TileUrlBuilder
    {
        private static final String MAX_VERSION = "1.3.0";

        private final String layerNames;
        private final String styleNames;
        private final String imageFormat;
        private final String wmsVersion;
        private final String crs;
        public String URLTemplate = null;

        private URLBuilder(String version, AVList params)
        {
            this.layerNames = params.getStringValue(AVKey.LAYER_NAMES);
            this.styleNames = params.getStringValue(AVKey.STYLE_NAMES);
            this.imageFormat = params.getStringValue(AVKey.IMAGE_FORMAT);

            if (version == null || version.compareTo(MAX_VERSION) >= 0)
            {
                this.wmsVersion = MAX_VERSION;
                this.crs = "&crs=CRS:84";
            }
            else
            {
                this.wmsVersion = version;
                this.crs = "&srs=EPSG:4326";
            }
        }

        public URL getURL(Tile tile, String altImageFormat) throws MalformedURLException
        {
            StringBuffer sb;
            if (this.URLTemplate == null)
            {
                sb = new StringBuffer(tile.getLevel().getService());

                if (!sb.toString().toLowerCase().contains("service=wms"))
                    sb.append("service=WMS");
                sb.append("&request=GetMap");
                sb.append("&version=");
                sb.append(this.wmsVersion);
                sb.append(this.crs);
                sb.append("&layers=");
                sb.append(this.layerNames);
                sb.append("&styles=");
                sb.append(this.styleNames != null ? this.styleNames : "");
                sb.append("&width=");
                sb.append(tile.getLevel().getTileWidth());
                sb.append("&height=");
                sb.append(tile.getLevel().getTileHeight());
                sb.append("&format=");
                if (altImageFormat == null)
                    sb.append(this.imageFormat);
                else
                    sb.append(altImageFormat);
                sb.append("&transparent=TRUE");
                sb.append("&bgcolor=0x000000");

                this.URLTemplate = sb.toString();
            }
            else
            {
                sb = new StringBuffer(this.URLTemplate);
            }

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());
            sb.append("&"); // terminate the query string

            return new java.net.URL(sb.toString().replace(" ", "%20"));
        }
//
//        private URL getImageTileURL(WMSImageTile tile) throws MalformedURLException
//        {
//            StringBuffer sb = new StringBuffer(tile.getLevel().getService());
//
//            if (!sb.toString().toLowerCase().contains("service=wms"))
//                sb.append("service=WMS");
//            sb.append("&request=GetMap");
//            sb.append("&version=");
//            sb.append(this.wmsVersion);
//            sb.append(this.crs);
//            sb.append("&layers=");
//            sb.append(this.layerNames);
//            sb.append("&styles=");
//            sb.append(this.styleNames != null ? this.styleNames : "default");
//            sb.append("&width=");
//            sb.append(tile.imageWidth);
//            sb.append("&height=");
//            sb.append(tile.imageHeight);
//            sb.append("&format=");
//            sb.append(tile.imageFormat);
//            sb.append("&transparent=TRUE");
//            sb.append("&bgcolor=0x000000");
//
//            sb.append("&bbox=");
//            sb.append(tile.getSector().getMinLongitude().getDegrees());
//            sb.append(",");
//            sb.append(tile.getSector().getMinLatitude().getDegrees());
//            sb.append(",");
//            sb.append(tile.getSector().getMaxLongitude().getDegrees());
//            sb.append(",");
//            sb.append(tile.getSector().getMaxLatitude().getDegrees());
//            sb.append("&"); // terminate the query string
//
//            return new java.net.URL(sb.toString().replace(" ", "%20"));
//        }
    }
//
//    @Override
//    protected URL getImageURL(TextureTile tile) throws MalformedURLException
//    {
//        StringBuffer sb = new StringBuffer(tile.getLevel().getService());
//
//        if (!sb.toString().toLowerCase().contains("service=wms"))
//            sb.append("service=WMS");
//        sb.append("&request=GetMap");
//        sb.append("&version=");
//        sb.append(this.wmsVersion);
//        sb.append(this.crs);
//        sb.append("&layers=");
//        sb.append(this.layerNames);
//        sb.append("&styles=");
//        sb.append(this.styleNames != null ? this.styleNames : "default");
//        sb.append("&width=");
//        sb.append(tile.imageWidth);
//        sb.append("&height=");
//        sb.append(tile.imageHeight);
//        sb.append("&format=");
//        sb.append(tile.imageFormat);
//        sb.append("&transparent=TRUE");
//        sb.append("&bgcolor=0x000000");
//
//        sb.append("&bbox=");
//        sb.append(tile.getSector().getMinLongitude().getDegrees());
//        sb.append(",");
//        sb.append(tile.getSector().getMinLatitude().getDegrees());
//        sb.append(",");
//        sb.append(tile.getSector().getMaxLongitude().getDegrees());
//        sb.append(",");
//        sb.append(tile.getSector().getMaxLatitude().getDegrees());
//        sb.append("&"); // terminate the query string
//
//        return new java.net.URL(sb.toString().replace(" ", "%20"));
//    }
//
//    private static class WMSImageTile extends TextureTile
//    {
//        private final String imageFormat;
//        private final int imageWidth;
//        private final int imageHeight;
//
//        private WMSImageTile(Sector sector, Level level, String imageFormat, int imageWidth, int imageHeight)
//        {
//            super(sector, level, 0, 0);
//
//            this.imageFormat = imageFormat;
//            this.imageWidth = imageWidth;
//            this.imageHeight = imageHeight;
//        }
//    }
//
//    public BufferedImage getImage(Sector sector, int imageSize, String imageFormat) throws Exception
//    {
//        if (sector == null)
//        {
//            String message = Logging.getMessage("nullValue.SectorIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalStateException(message);
//        }
//
//        if (imageSize <= 0)
//        {
//            String message = Logging.getMessage("generic.InvalidImageSize", imageSize, imageSize);
//            Logging.logger().severe(message);
//            throw new IllegalStateException(message);
//        }
//
//        if (imageFormat == null)
//        {
//            String message = Logging.getMessage("nullValue.ImageFomat");
//            Logging.logger().severe(message);
//            throw new IllegalStateException(message);
//        }
//
//        WMSImageTile tile =
//            new WMSImageTile(sector, this.getLevels().getLastLevel(), imageFormat, imageSize, imageSize);
//
//        File imageFile = File.createTempFile("LandPrintPreviewImage", makeSuffixFromContentType(imageFormat));
//        imageFile.deleteOnExit();
//
//        // Retrieve it from the wms server.
//        this.downloadImage(tile.getResourceURL(), imageFile);
//
//        // Try to read from disk after retrieving it from the server.
//        if (!imageFile.exists())
//        {
//            String message =
//                Logging.getMessage("layers.TiledImageLayer.ImageUnavailable", tile.getResourceURL().toString());
//            Logging.logger().warning(message);
//            return null;
//        }
//
//        BufferedImage image = ImageIO.read(imageFile);
//        if (image == null)
//        {
//            String message = Logging.getMessage("generic.ImageFormatUnsupported", imageFormat);
//            throw new RuntimeException(message);
//        }
//
//        return image;
//    }
//
//    public static String makeSuffixFromContentType(String contentType)
//    {
//        if (contentType == null)
//        {
//            String message = Logging.getMessage("nullValue.ImageFomat");
//            Logging.logger().severe(message);
//            throw new IllegalStateException(message);
//        }
//
//        if (!contentType.contains("/") || contentType.endsWith("/"))
//        {
//            String message = Logging.getMessage("generic.InvalidImageFormat");
//            Logging.logger().severe(message);
//            throw new IllegalStateException(message);
//        }
//
//        String suffix = contentType.substring(contentType.lastIndexOf("/") + 1);
//
//        return "." + suffix.replaceFirst("jpeg", "jpg");
//    }
//
//    private void downloadImage(URL imageURL, File imageFile) throws Exception
//    {
//        if (!"http".equalsIgnoreCase(imageURL.getProtocol()))
//        {
//            String message = Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", imageURL);
//            throw new RuntimeException(message);
//        }
//
//        Retriever retriever = new HTTPRetriever(imageURL, new HttpRetrievalPostProcessor(imageFile));
//        retriever.setConnectTimeout(10000);
//        retriever.setReadTimeout(20000);
//        retriever.call();
//    }
//
//    private class HttpRetrievalPostProcessor implements RetrievalPostProcessor
//    {
//        private File imageFile;
//
//        public HttpRetrievalPostProcessor(File imageFile)
//        {
//            this.imageFile = imageFile;
//        }
//
//        public ByteBuffer run(Retriever retriever)
//        {
//            if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
//                return null;
//
//            HTTPRetriever htr = (HTTPRetriever) retriever;
//            if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
//                return null;
//
//            if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
//                return null;
//
//            URLRetriever r = (URLRetriever) retriever;
//            ByteBuffer buffer = r.getBuffer();
//
//            try
//            {
//                WWIO.saveBuffer(buffer, this.imageFile);
//                return buffer;
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace(); // TODO: log error
//                return null;
//            }
//        }
//    }
}
