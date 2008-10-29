/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;

/**
 * Displays populated place names for the current view, off the GeoNames server http://www.geonames.org.
 * @author Patrick Murris
 * @version $Id$
 */

public class GeoNamesLayer extends RenderableLayer
{
    private final String GEONAMES_CITIES = "http://ws.geonames.org/cities";
    private final int MIN_DELAY_BETWEEN_REQUESTS = 2000;    // 2 sec
    private final int REQUEST_TIMEOUT_DELAY = 10000;        // 10 sec
    private final int MAX_ROWS = 100;                       // server wont go beyond 100?

    private AnnotationAttributes locAttributes;

    private Vec4 lastEyePoint;
    private Thread downloadThread;
    private long lastRequestTime;
    private Sector pendingSector;
    private Timer timer;
    private boolean isRendering = false;

    public GeoNamesLayer()
    {
        this.setName("GeoNames");
        // Init default attributes for all annotations
        locAttributes = new AnnotationAttributes();
        locAttributes.setTextColor(Color.YELLOW);
        locAttributes.setBackgroundColor(Color.BLACK);
        locAttributes.setFont(Font.decode("Arial-Bold-14"));
        locAttributes.setFrameShape(FrameFactory.SHAPE_NONE);
        locAttributes.setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
        locAttributes.setEffect(MultiLineTextRenderer.EFFECT_OUTLINE);
        locAttributes.setDrawOffset(new Point(0, 0));
        locAttributes.setInsets(new Insets(0, 0, 0, 0));
    }

    public AnnotationAttributes getAttributes()
    {
        return this.locAttributes;
    }

    public void doRender(DrawContext dc)
    {
        if (this.needsToUpdate(dc))
        {
            this.lastEyePoint = dc.getView().getEyePoint();
            update(dc);
        }
        this.isRendering = true;
        super.doRender(dc);
        this.isRendering = false;
    }

    private boolean needsToUpdate(DrawContext dc)
    {
        if (this.lastEyePoint == null)
            return true;

        View view = dc.getView();
        double altitudeAboveGround = computeAltitudeAboveGround(dc);
        if (view.getEyePoint().distanceTo3(this.lastEyePoint) > altitudeAboveGround / 10)  // 10% of AAG
            return true;

        return false;
    }

    private double computeAltitudeAboveGround(DrawContext dc)
    {
        View view = dc.getView();
        Position eyePosition = view.getEyePosition();
        Vec4 surfacePoint = getSurfacePoint(dc, eyePosition.getLatitude(), eyePosition.getLongitude());
        return view.getEyePoint().distanceTo3(surfacePoint);
    }

    private Vec4 getSurfacePoint(DrawContext dc, Angle latitude, Angle longitude)
    {
        Vec4 surfacePoint = dc.getSurfaceGeometry().getSurfacePoint(latitude, longitude);
        if (surfacePoint == null)
            surfacePoint = dc.getGlobe().computePointFromPosition(new Position(latitude, longitude,
                    dc.getGlobe().getElevation(latitude, longitude)));
        return surfacePoint;
    }

    private void update(DrawContext dc)
    {
        // TODO: compute a more constrained (and appropriate) sector around the view center
        Sector sector = dc.getVisibleSector();
        if (sector.getDeltaLon().degrees < 45)
            startDownload(sector);
    }

    //--- Geonames download and parse -------------------------------------------------------------

    // TODO: see how this would fit with the WW retrieval service
    // TODO: cache previously downloaded names up a max number
    private void startDownload(final Sector sector)
    {
        // Setup download timer if not done yet
        if (this.timer == null)
        {
            this.timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent event)
                {
                    long now = new Date().getTime();
                    if (downloadThread != null)
                    {
                        if (now - lastRequestTime > REQUEST_TIMEOUT_DELAY)
                        {
                            // Kill thread if timed out
                            try
                            {
                                downloadThread.interrupt();
                            }
                            catch (Exception e) {}
                            downloadThread = null;
                        }
                    }
                    if (downloadThread == null && pendingSector != null
                            && now - lastRequestTime > MIN_DELAY_BETWEEN_REQUESTS)
                    {
                        // If request pending, start it
                        startDownload(pendingSector);
                    }

                }
            });
            this.timer.start();
        }
        // Process download request
        long now = new Date().getTime();
        if(this.downloadThread == null && now - lastRequestTime > MIN_DELAY_BETWEEN_REQUESTS)
        {
            // Start new download thread if none running
            this.lastRequestTime = now;
            this.downloadThread = new Thread(new Runnable() {
                public void run()
                {
                    downloadAndParse(sector);
                    downloadThread = null;
                }
            }, "Geonames Download");
            this.downloadThread.start();
            this.pendingSector = null;
        }
        else
        {
            // else save requested sector
            this.pendingSector = sector;
        }
    }


    private void downloadAndParse(Sector sector)
    {
        StringBuilder query = new StringBuilder();
        query.append("?north=");
        query.append(sector.getMaxLatitude().degrees);
        query.append("&south=");
        query.append(sector.getMinLatitude().degrees);
        query.append("&east=");
        query.append(sector.getMaxLongitude().degrees);
        query.append("&west=");
        query.append(sector.getMinLongitude().degrees);
        query.append("&maxRows=");
        query.append(MAX_ROWS);
        try
        {
            // Get xml from server
            URL url = new URL(GEONAMES_CITIES + query);
            ByteBuffer bb = WWIO.readURLContentToBuffer(url);
            // Parse feed and add renderables to layer
            parseFile(this, WWIO.saveBufferToTempFile(bb, ".xml"));
        }
        catch(MalformedURLException e) {}
        catch(IOException e) {}

    }

    private void parseFile(RenderableLayer layer, File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Create document
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            // Parse document
            parseDoc(layer, doc);

        }
        catch (ParserConfigurationException e)
        {
            String message = Logging.getMessage("GeoRSS.ParserConfigurationException");
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("GeoRSS.IOExceptionParsing", file.getPath());
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
        catch (SAXException e)
        {
            String message = Logging.getMessage("GeoRSS.IOExceptionParsing", file.getPath());
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    private void parseDoc(RenderableLayer layer, Document xmlDoc)
    {
        if (xmlDoc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Process entries
        NodeList nodes = xmlDoc.getElementsByTagName("geoname");
        if (nodes != null && nodes.getLength() > 0)
        {
            // Wait for end of render if necessary - hacky
            int count = 0;
            while (count++ < 10 && isRendering)
            {
                try {Thread.sleep(20);}
                catch (Exception e) {}
            }
            layer.removeAllRenderables(); // clear current annotations
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node entry = nodes.item(i);
                Location loc = new Location(entry);
                addLocation(layer, loc);  // add annotation
            }
            layer.firePropertyChange(AVKey.LAYER, null, null);
        }
    }

    private void addLocation(RenderableLayer layer, Location location)
    {
        GlobeAnnotation ga = new GlobeAnnotation(location.name, location.position, locAttributes);
        layer.addRenderable(ga);
    }

    private static Node findChildByName(Node parent, String localName)
    {
        NodeList children = parent.getChildNodes();
        if (children == null || children.getLength() < 1)
            return null;
        for (int i = 0; i < children.getLength(); i++)
        {
            String ln = children.item(i).getNodeName();
            if (ln != null && ln.equals(localName))
                return children.item(i);
        }
        return null;
    }

    /**
     * One GeoNames location.
     */
    private class Location
    {
        public String name;
        public Position position;
        public String countryCode;
        public String countryName;
        public String type;

        public Location(Node entry)
        {
            if (entry != null)
            {
                Node node;
                node = findChildByName(entry, "name");
                if (node != null)
                    this.name = node.getTextContent();

                double lat = 0, lng = 0;
                node = findChildByName(entry, "lat");
                if (node != null)
                    lat = Double.parseDouble(node.getTextContent());
                node = findChildByName(entry, "lng");
                if (node != null)
                    lng = Double.parseDouble(node.getTextContent());
                this.position = Position.fromDegrees(lat, lng, 0);

                node = findChildByName(entry, "countryCode");
                if (node != null)
                    this.countryCode = node.getTextContent();
                node = findChildByName(entry, "countryName");
                if (node != null)
                    this.countryName = node.getTextContent();
                node = findChildByName(entry, "fcode");
                if (node != null)
                    this.type = node.getTextContent();
            }
            else
            {
                this.name = "?";
            }
        }
    }

}
