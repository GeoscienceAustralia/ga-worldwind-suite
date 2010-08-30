/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.retrieve.AbstractRetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLTextRenderer;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.opengl.util.j2d.TextRenderer;

/**
 * @author tag
 * @version $Id: TiledImageLayer.java 13363 2010-05-03 19:36:53Z dcollins $
 */
public abstract class TiledImageLayer extends AbstractLayer
{
    // Infrastructure
    private static final LevelComparer levelComparer = new LevelComparer();
    private final LevelSet levels;
    private ArrayList<TextureTile> topLevels;
    private boolean forceLevelZeroLoads = false;
    private boolean levelZeroLoaded = false;
    private boolean retainLevelZeroTiles = false;
    private String tileCountName;
    private double splitScale = 0.9;
    private boolean compressTextures = false;
    private boolean useMipMaps = true;
    private boolean useTransparentTextures = false;
    private ArrayList<String> supportedImageFormats = new ArrayList<String>();

    // Diagnostic flags
    private boolean showImageTileOutlines = false;
    private boolean drawTileBoundaries = false;
    private boolean drawTileIDs = false;
    private boolean drawBoundingVolumes = false;

    // Stuff computed each frame
    private ArrayList<TextureTile> currentTiles = new ArrayList<TextureTile>();
    private TextureTile currentResourceTile;
    private boolean atMaxResolution = false;
    private PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(200);

    abstract protected void requestTexture(DrawContext dc, TextureTile tile);

    abstract protected void forceTextureLoad(TextureTile tile);

    public TiledImageLayer(LevelSet levelSet)
    {
        if (levelSet == null)
        {
            String message = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.

//        this.createTopLevelTiles();

        this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
        this.tileCountName = this.getName() + " Tiles";
    }

    @Override
    public Object setValue(String key, Object value)
    {
        // Offer it to the level set
        if (this.getLevels() != null)
            this.getLevels().setValue(key, value);

        return super.setValue(key, value);
    }

    @Override
    public Object getValue(String key)
    {
        Object value = super.getValue(key);

        return value != null ? value : this.getLevels().getValue(key); // see if the level set has it
    }

    @Override
    public void setName(String name)
    {
        super.setName(name);
        this.tileCountName = this.getName() + " Tiles";
    }

    public boolean isForceLevelZeroLoads()
    {
        return this.forceLevelZeroLoads;
    }

    public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
    {
        this.forceLevelZeroLoads = forceLevelZeroLoads;
    }

    public boolean isRetainLevelZeroTiles()
    {
        return retainLevelZeroTiles;
    }

    public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
    {
        this.retainLevelZeroTiles = retainLevelZeroTiles;
    }

    public boolean isDrawTileIDs()
    {
        return drawTileIDs;
    }

    public void setDrawTileIDs(boolean drawTileIDs)
    {
        this.drawTileIDs = drawTileIDs;
    }

    public boolean isDrawTileBoundaries()
    {
        return drawTileBoundaries;
    }

    public void setDrawTileBoundaries(boolean drawTileBoundaries)
    {
        this.drawTileBoundaries = drawTileBoundaries;
    }

    public boolean isShowImageTileOutlines()
    {
        return showImageTileOutlines;
    }

    public void setShowImageTileOutlines(boolean showImageTileOutlines)
    {
        this.showImageTileOutlines = showImageTileOutlines;
    }

    public boolean isDrawBoundingVolumes()
    {
        return drawBoundingVolumes;
    }

    public void setDrawBoundingVolumes(boolean drawBoundingVolumes)
    {
        this.drawBoundingVolumes = drawBoundingVolumes;
    }

    /**
     * Sets the parameter controlling a layer's displayed resolution as distance changes between the globe's surface and
     * the eye point. Higher resolution is displayed as the split scale increases from 1.0. Lower resolution is
     * displayed as the split scale decreases from 1.0. The default value is specified in the layer's configuration, or
     * is 0.9 if not specified there.
     *
     * @param splitScale a value near 1.0 that controls the image resolution as the distance between the globe's surface
     *                   and the eye point change. Increasing values select higher resolution, decreasing values select
     *                   lower resolution. Typical values range between 0.8 and 1.2.
     */
    public void setSplitScale(double splitScale)
    {
        this.splitScale = splitScale;
    }

    /**
     * Returns the split scale value controlling image resolution relative to the distance between the globe's surface
     * at the image position and the eye point.
     *
     * @return the current split scale.
     *
     * @see #setSplitScale(double)
     */
    public double getSplitScale()
    {
        return this.splitScale;
    }

    protected LevelSet getLevels()
    {
        return levels;
    }

    protected PriorityBlockingQueue<Runnable> getRequestQ()
    {
        return requestQ;
    }

    public boolean isMultiResolution()
    {
        return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
    }

    public boolean isAtMaxResolution()
    {
        return this.atMaxResolution;
    }

    /**
     * Returns true if image texture tiles are compressed before rendering, and false otherwise.
     *
     * @return true if this compresses texture tiles before rendering; false if this creates textures from the tile
     *         source without compression.
     */
    public boolean isCompressTextures()
    {
        return this.compressTextures;
    }

    /**
     * Specifies if texture tiles should be compressed before rendering. If true, this compresses tile textures in a
     * non-rendering thread, without modifying the original tile source. Otherwise, this creates textures from the tile
     * sources without compression. This has no effect on tile sources which are alread in a compressed texture format,
     * such as DDS.
     *
     * @param compressTextures true to compress texture tiles before rendering; false to create textures directly from
     *                         the tile sources.
     */
    public void setCompressTextures(boolean compressTextures)
    {
        this.compressTextures = compressTextures;
    }

    public boolean isUseMipMaps()
    {
        return useMipMaps;
    }

    public void setUseMipMaps(boolean useMipMaps)
    {
        this.useMipMaps = useMipMaps;
    }

    public boolean isUseTransparentTextures()
    {
        return this.useTransparentTextures;
    }

    public void setUseTransparentTextures(boolean useTransparentTextures)
    {
        this.useTransparentTextures = useTransparentTextures;
    }

    /**
     * Specifies the time of the layer's most recent dataset update, beyond which cached data is invalid. If greater
     * than zero, the layer ignores and eliminates any in-memory or on-disk cached data older than the time specified,
     * and requests new information from the data source. If zero, the default, the layer applies any expiry times
     * associated with its individual levels, but only for on-disk cached data. In-memory cached data is expired only
     * when the expiry time is specified with this method and is greater than zero. This method also overwrites the
     * expiry times of the layer's individual levels if the value specified to the method is greater than zero.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch. The
     *                   default expiry time is zero.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    public void setExpiryTime(long expiryTime) // Override this method to use intrinsic level-specific expiry times
    {
        super.setExpiryTime(expiryTime);

        if (expiryTime > 0)
            this.levels.setExpiryTime(expiryTime); // remove this in sub-class to use level-specific expiry times
    }

    public ArrayList<TextureTile> getTopLevels()
    {
        if (this.topLevels == null)
            this.createTopLevelTiles();

        return topLevels;
    }

    private void createTopLevelTiles()
    {
        Sector sector = this.levels.getSector();

        Level level = levels.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();
        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        // Determine the row and column offset from the common World Wind global tiling origin.
        int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
        int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
        int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
        int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        this.topLevels = new ArrayList<TextureTile>(nLatTiles * nLonTiles);

        Angle p1 = Tile.computeRowLatitude(firstRow, dLat, latOrigin);
        for (int row = firstRow; row <= lastRow; row++)
        {
            Angle p2;
            p2 = p1.add(dLat);

            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
            for (int col = firstCol; col <= lastCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);

                this.topLevels.add(createTile(new Sector(p1, p2, t1, t2), level, row, col));
                t1 = t2;
            }
            p1 = p2;
        }
    }

    private void loadAllTopLevelTextures(DrawContext dc)
    {
        for (TextureTile tile : this.getTopLevels())
        {
            if (!tile.isTextureInMemory(dc.getTextureCache()))
                this.forceTextureLoad(tile);
        }

        this.levelZeroLoaded = true;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    private void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();

        for (TextureTile tile : this.getTopLevels())
        {
            if (this.isTileVisible(dc, tile))
            {
                this.currentResourceTile = null;
                this.addTileOrDescendants(dc, tile);
            }
        }
    }

    private void addTileOrDescendants(DrawContext dc, TextureTile tile)
    {
        if (this.meetsRenderCriteria(dc, tile))
        {
            this.addTile(dc, tile);
            return;
        }

        // The incoming tile does not meet the rendering criteria, so it must be subdivided and those
        // subdivisions tested against the criteria.

        // All tiles that meet the selection criteria are drawn, but some of those tiles will not have
        // textures associated with them either because their texture isn't loaded yet or because they
        // are finer grain than the layer has textures for. In these cases the tiles use the texture of
        // the closest ancestor that has a texture loaded. This ancestor is called the currentResourceTile.
        // A texture transform is applied during rendering to align the sector's texture coordinates with the
        // appropriate region of the ancestor's texture.

        TextureTile ancestorResource = null;

        try
        {
            // TODO: Revise this to reflect that the parent layer is only requested while the algorithm continues
            // to search for the layer matching the criteria.
            // At this point the tile does not meet the render criteria but it may have its texture in memory.
            // If so, register this tile as the resource tile. If not, then this tile will be the next level
            // below a tile with texture in memory. So to provide progressive resolution increase, add this tile
            // to the draw list. That will cause the tile to be drawn using its parent tile's texture, and it will
            // cause it's texture to be requested. At some future call to this method the tile's texture will be in
            // memory, it will not meet the render criteria, but will serve as the parent to a tile that goes
            // through this same process as this method recurses. The result of all this is that a tile isn't rendered
            // with its own texture unless all its parents have their textures loaded. In addition to causing
            // progressive resolution increase, this ensures that the parents are available as the user zooms out, and
            // therefore the layer remains visible until the user is zoomed out to the point the layer is no longer
            // active.
            if (tile.isTextureInMemory(dc.getTextureCache()) || tile.getLevelNumber() == 0)
            {
                ancestorResource = this.currentResourceTile;
                this.currentResourceTile = tile;
            }
            else if (!tile.getLevel().isEmpty())
            {
//                this.addTile(dc, tile);
//                return;

                // Issue a request for the parent before descending to the children.
//                if (tile.getLevelNumber() < this.levels.getNumLevels())
//                {
//                    // Request only tiles with data associated at this level
//                    if (!this.levels.isResourceAbsent(tile))
//                        this.requestTexture(dc, tile);
//                }
            }

            TextureTile[] subTiles = tile.createSubTiles(this.levels.getLevel(tile.getLevelNumber() + 1));
            for (TextureTile child : subTiles)
            {
                if (this.isTileVisible(dc, child))
                    this.addTileOrDescendants(dc, child);
            }
        }
        finally
        {
            if (ancestorResource != null) // Pop this tile as the currentResource ancestor
                this.currentResourceTile = ancestorResource;
        }
    }

    protected void addTile(DrawContext dc, TextureTile tile)
    {
        tile.setFallbackTile(null);

        if (tile.isTextureInMemory(dc.getTextureCache()))
        {
            this.addTileToCurrent(tile);
            return;
        }

        // Level 0 loads may be forced
        if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads && !tile.isTextureInMemory(dc.getTextureCache()))
        {
            this.forceTextureLoad(tile);
            if (tile.isTextureInMemory(dc.getTextureCache()))
            {
                this.addTileToCurrent(tile);
                return;
            }
        }

        // Tile's texture isn't available, so request it
        if (tile.getLevelNumber() < this.levels.getNumLevels())
        {
            // Request only tiles with data associated at this level
            if (!this.levels.isResourceAbsent(tile))
                this.requestTexture(dc, tile);
        }

        // Set up to use the currentResource tile's texture
        if (this.currentResourceTile != null)
        {
            if (this.currentResourceTile.getLevelNumber() == 0 && this.forceLevelZeroLoads &&
                !this.currentResourceTile.isTextureInMemory(dc.getTextureCache()) &&
                !this.currentResourceTile.isTextureInMemory(dc.getTextureCache()))
                this.forceTextureLoad(this.currentResourceTile);

            if (this.currentResourceTile.isTextureInMemory(dc.getTextureCache()))
            {
                tile.setFallbackTile(currentResourceTile);
                this.addTileToCurrent(tile);
            }
        }
    }

    protected void addTileToCurrent(TextureTile tile)
    {
        this.currentTiles.add(tile);
    }

    private boolean isTileVisible(DrawContext dc, TextureTile tile)
    {
//        if (!(tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates())
//            && (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()))))
//            return false;
//
//        Position eyePos = dc.getView().getEyePosition();
//        LatLon centroid = tile.getSector().getCentroid();
//        Angle d = LatLon.greatCircleDistance(eyePos, centroid);
//        if ((!tile.getLevelName().equals("0")) && d.compareTo(tile.getSector().getDeltaLat().multiply(2.5)) == 1)
//            return false;
//
//        return true;
//
        return tile.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates()) &&
            (dc.getVisibleSector() == null || dc.getVisibleSector().intersects(tile.getSector()));
    }
//
//    private boolean meetsRenderCriteria2(DrawContext dc, TextureTile tile)
//    {
//        if (this.levels.isFinalLevel(tile.getLevelNumber()))
//            return true;
//
//        Sector sector = tile.getSector();
//        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe());
//        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe());
//
//        View view = dc.getView();
//        double d1 = view.getEyePoint().distanceTo3(corners[0]);
//        double d2 = view.getEyePoint().distanceTo3(corners[1]);
//        double d3 = view.getEyePoint().distanceTo3(corners[2]);
//        double d4 = view.getEyePoint().distanceTo3(corners[3]);
//        double d5 = view.getEyePoint().distanceTo3(centerPoint);
//
//        double minDistance = d1;
//        if (d2 < minDistance)
//            minDistance = d2;
//        if (d3 < minDistance)
//            minDistance = d3;
//        if (d4 < minDistance)
//            minDistance = d4;
//        if (d5 < minDistance)
//            minDistance = d5;
//
//        double r = 0;
//        if (minDistance == d1)
//            r = corners[0].getLength3();
//        if (minDistance == d2)
//            r = corners[1].getLength3();
//        if (minDistance == d3)
//            r = corners[2].getLength3();
//        if (minDistance == d4)
//            r = corners[3].getLength3();
//        if (minDistance == d5)
//            r = centerPoint.getLength3();
//
//        double texelSize = tile.getLevel().getTexelSize(r);
//        double pixelSize = dc.getView().computePixelSizeAtDistance(minDistance);
//
//        return 2 * pixelSize >= texelSize;
//    }

    private boolean meetsRenderCriteria(DrawContext dc, TextureTile tile)
    {
        return this.levels.isFinalLevel(tile.getLevelNumber()) || !needToSplit(dc, tile.getSector());
    }

    private boolean needToSplit(DrawContext dc, Sector sector)
    {
        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        View view = dc.getView();
        double d1 = view.getEyePoint().distanceTo3(corners[0]);
        double d2 = view.getEyePoint().distanceTo3(corners[1]);
        double d3 = view.getEyePoint().distanceTo3(corners[2]);
        double d4 = view.getEyePoint().distanceTo3(corners[3]);
        double d5 = view.getEyePoint().distanceTo3(centerPoint);

        double minDistance = d1;
        if (d2 < minDistance)
            minDistance = d2;
        if (d3 < minDistance)
            minDistance = d3;
        if (d4 < minDistance)
            minDistance = d4;
        if (d5 < minDistance)
            minDistance = d5;

        double cellSize = (Math.PI * sector.getDeltaLatRadians() * dc.getGlobe().getRadius()) / 20; // TODO

        return !(Math.log10(cellSize) <= (Math.log10(minDistance) - this.getSplitScale()));
    }

    private boolean atMaxLevel(DrawContext dc)
    {
        Position vpc = dc.getViewportCenterPosition();
        if (dc.getView() == null || this.getLevels() == null || vpc == null)
            return false;

        if (!this.getLevels().getSector().contains(vpc.getLatitude(), vpc.getLongitude()))
            return true;

        Level nextToLast = this.getLevels().getNextToLastLevel();
        if (nextToLast == null)
            return true;

        Sector centerSector = nextToLast.computeSectorForPosition(vpc.getLatitude(), vpc.getLongitude(),
            this.levels.getTileOrigin());
        return this.needToSplit(dc, centerSector);
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    @Override
    public void render(DrawContext dc)
    {
        this.atMaxResolution = this.atMaxLevel(dc);
        super.render(dc);
    }

    @Override
    protected final void doRender(DrawContext dc)
    {
        if (this.forceLevelZeroLoads && !this.levelZeroLoaded)
            this.loadAllTopLevelTextures(dc);
        if (dc.getSurfaceGeometry() == null || dc.getSurfaceGeometry().size() < 1)
            return;

        dc.getGeographicSurfaceTileRenderer().setShowImageTileOutlines(this.showImageTileOutlines);

        draw(dc);
    }

    private void draw(DrawContext dc)
    {
        this.assembleTiles(dc); // Determine the tiles to draw.

        if (this.currentTiles.size() >= 1)
        {
            if (this.getScreenCredit() != null)
            {
                dc.addScreenCredit(this.getScreenCredit());
            }

            TextureTile[] sortedTiles = new TextureTile[this.currentTiles.size()];
            sortedTiles = this.currentTiles.toArray(sortedTiles);
            Arrays.sort(sortedTiles, levelComparer);

            GL gl = dc.getGL();

            if (this.isUseTransparentTextures() || this.getOpacity() < 1)
            {
                gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_CURRENT_BIT);
                this.setBlendingFunction(dc);
            }
            else
            {
                gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);
            }

            gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT, this.tileCountName,
                this.currentTiles.size());
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.currentTiles);

            gl.glPopAttrib();

            if (this.drawTileIDs)
                this.drawTileIDs(dc, this.currentTiles);

            if (this.drawBoundingVolumes)
                this.drawBoundingVolumes(dc, this.currentTiles);

            // Check texture expiration. Memory-cached textures are checked for expiration only when an explicit,
            // non-zero expiry time has been set for the layer. If none has been set, the expiry times of the layer's
            // individual levels are used, but only for images in the local file cache, not textures in memory. This is
            // to avoid incurring the overhead of checking expiration of in-memory textures, a very rarely used feature.
            if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis())
                this.checkTextureExpiration(dc, this.currentTiles);

            this.currentTiles.clear();
        }

        this.sendRequests();
        this.requestQ.clear();
    }

    private void checkTextureExpiration(DrawContext dc, List<TextureTile> tiles)
    {
        for (TextureTile tile : tiles)
        {
            if (tile.isTextureExpired())
                this.requestTexture(dc, tile);
        }
    }

    protected void setBlendingFunction(DrawContext dc)
    {
        // Set up a premultiplied-alpha blending function. Any texture read by JOGL will have alpha-premultiplied color
        // components, as will any DDS file created by World Wind or the World Wind WMS. We'll also set up the base
        // color as a premultiplied color, so that any incoming premultiplied color will be properly combined with the
        // base color.

        GL gl = dc.getGL();

        double alpha = this.getOpacity();
        gl.glColor4d(alpha, alpha, alpha, alpha);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void sendRequests()
    {
        Runnable task = this.requestQ.poll();
        while (task != null)
        {
            if (!WorldWind.getTaskService().isFull())
            {
                WorldWind.getTaskService().addTask(task);
            }
            task = this.requestQ.poll();
        }
    }

    public boolean isLayerInView(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (dc.getView() == null)
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return !(dc.getVisibleSector() != null && !this.levels.getSector().intersects(dc.getVisibleSector()));
    }

    private Vec4 computeReferencePoint(DrawContext dc)
    {
        if (dc.getViewportCenterPosition() != null)
            return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());

        java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
        int x = (int) viewport.getWidth() / 2;
        for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
        {
            Position pos = dc.getView().computePositionFromScreenPoint(x, y);
            if (pos == null)
                continue;

            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
        }

        return null;
    }

    protected Vec4 getReferencePoint(DrawContext dc)
    {
        return this.computeReferencePoint(dc);
    }

    private static class LevelComparer implements Comparator<TextureTile>
    {
        public int compare(TextureTile ta, TextureTile tb)
        {
            int la = ta.getFallbackTile() == null ? ta.getLevelNumber() : ta.getFallbackTile().getLevelNumber();
            int lb = tb.getFallbackTile() == null ? tb.getLevelNumber() : tb.getFallbackTile().getLevelNumber();

            return la < lb ? -1 : la == lb ? 0 : 1;
        }
    }

    private void drawTileIDs(DrawContext dc, ArrayList<TextureTile> tiles)
    {
        java.awt.Rectangle viewport = dc.getView().getViewport();
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            java.awt.Font.decode("Arial-Plain-13"));

        dc.getGL().glDisable(GL.GL_DEPTH_TEST);
        dc.getGL().glDisable(GL.GL_BLEND);
        dc.getGL().glDisable(GL.GL_TEXTURE_2D);

        textRenderer.beginRendering(viewport.width, viewport.height);
        textRenderer.setColor(java.awt.Color.YELLOW);
        for (TextureTile tile : tiles)
        {
            String tileLabel = tile.getLabel();

            if (tile.getFallbackTile() != null)
                tileLabel += "/" + tile.getFallbackTile().getLabel();

            LatLon ll = tile.getSector().getCentroid();
            Vec4 pt = dc.getGlobe().computePointFromPosition(ll.getLatitude(), ll.getLongitude(),
                dc.getGlobe().getElevation(ll.getLatitude(), ll.getLongitude()));
            pt = dc.getView().project(pt);
            textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
        }
        textRenderer.setColor(java.awt.Color.WHITE);
        textRenderer.endRendering();
    }

    private void drawBoundingVolumes(DrawContext dc, ArrayList<TextureTile> tiles)
    {
        float[] previousColor = new float[4];
        dc.getGL().glGetFloatv(GL.GL_CURRENT_COLOR, previousColor, 0);
        dc.getGL().glColor3d(0, 1, 0);

        for (TextureTile tile : tiles)
        {
            if (tile.getExtent(dc) instanceof Renderable)
                ((Renderable) tile.getExtent(dc)).render(dc);
        }

        Box c = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this.levels.getSector());
        dc.getGL().glColor3d(1, 1, 0);
        c.render(dc);

        dc.getGL().glColor4fv(previousColor, 0);
    }

    //**************************************************************//
    //********************  Configuration  *************************//
    //**************************************************************//

    /**
     * Creates a configuration document for a TiledImageLayer described by the specified params. The returned document
     * may be used as a construction parameter to {@link gov.nasa.worldwind.layers.BasicTiledImageLayer}.
     *
     * @param params parameters describing the TiledImageLayer.
     *
     * @return a configuration document for the TiledImageLayer.
     */
    public static Document createTiledImageLayerConfigDocument(AVList params)
    {
        Document doc = WWXML.createDocumentBuilder(true).newDocument();

        Element root = WWXML.setDocumentElement(doc, "Layer");
        WWXML.setIntegerAttribute(root, "version", 1);
        WWXML.setTextAttribute(root, "layerType", "TiledImageLayer");

        createTiledImageLayerConfigElements(params, root);

        return doc;
    }

    /**
     * Appends TiledImageLayer configuration parameters as elements to the specified context. This appends elements for
     * the following parameters: <table> <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> <tr><td>{@link
     * AVKey#SERVICE_NAME}</td><td>Service/@serviceName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#IMAGE_FORMAT}</td><td>ImageFormat</td><td>String</td></tr> <tr><td>{@link
     * AVKey#AVAILABLE_IMAGE_FORMATS}</td><td>AvailableImageFormats/ImageFormat</td><td>String array</td></tr>
     * <tr><td>{@link AVKey#FORCE_LEVEL_ZERO_LOADS}</td><td>ForceLevelZeroLoads</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#RETAIN_LEVEL_ZERO_TILES}</td><td>RetainLevelZeroTiles</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#COMPRESS_TEXTURES}</td><td>CompressTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_MIP_MAPS}</td><td>UseMipMaps</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_TRANSPARENT_TEXTURES}</td><td>UseTransparentTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#URL_CONNECT_TIMEOUT}</td><td>RetrievalTimeouts/ConnectTimeout/Time</td><td>Integer milliseconds</td></tr>
     * <tr><td>{@link AVKey#URL_READ_TIMEOUT}</td><td>RetrievalTimeouts/ReadTimeout/Time</td><td>Integer
     * milliseconds</td></tr> <tr><td>{@link AVKey#RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT}</td><td>RetrievalTimeouts/StaleRequestLimit/Time</td><td>Integer
     * milliseconds</td></tr> </table> This also writes common layer and LevelSet configuration parameters by invoking
     * {@link gov.nasa.worldwind.layers.AbstractLayer#createLayerConfigElements(gov.nasa.worldwind.avlist.AVList,
     * org.w3c.dom.Element)} and {@link DataConfigurationUtils#createLevelSetConfigElements(gov.nasa.worldwind.avlist.AVList,
     * org.w3c.dom.Element)}.
     *
     * @param params  the key-value pairs which define the TiledImageLayer configuration parameters.
     * @param context the XML document root on which to append TiledImageLayer configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createTiledImageLayerConfigElements(AVList params, Element context)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        // Common layer properties.
        AbstractLayer.createLayerConfigElements(params, context);

        // LevelSet properties.
        DataConfigurationUtils.createLevelSetConfigElements(params, context);

        // Service properties.
        // Try to get the SERVICE_NAME property, but default to "WWTileService".
        String s = AVListImpl.getStringValue(params, AVKey.SERVICE_NAME, "WWTileService");
        if (s != null && s.length() > 0)
        {
            // The service element may already exist, in which case we want to append to it.
            Element el = WWXML.getElement(context, "Service", xpath);
            if (el == null)
                el = WWXML.appendElementPath(context, "Service");
            WWXML.setTextAttribute(el, "serviceName", s);
        }

        WWXML.checkAndAppendBooleanElement(params, AVKey.RETRIEVE_PROPERTIES_FROM_SERVICE, context,
            "RetrievePropertiesFromService");

        // Image format properties.
        WWXML.checkAndAppendTextElement(params, AVKey.IMAGE_FORMAT, context, "ImageFormat");

        Object o = params.getValue(AVKey.AVAILABLE_IMAGE_FORMATS);
        if (o != null && o instanceof String[])
        {
            String[] strings = (String[]) o;
            if (strings.length > 0)
            {
                // The available image formats element may already exists, in which case we want to append to it, rather
                // than create entirely separate paths.
                Element el = WWXML.getElement(context, "AvailableImageFormats", xpath);
                if (el == null)
                    el = WWXML.appendElementPath(context, "AvailableImageFormats");
                WWXML.appendTextArray(el, "ImageFormat", strings);
            }
        }

        // Optional behavior properties.
        WWXML.checkAndAppendBooleanElement(params, AVKey.FORCE_LEVEL_ZERO_LOADS, context, "ForceLevelZeroLoads");
        WWXML.checkAndAppendBooleanElement(params, AVKey.RETAIN_LEVEL_ZERO_TILES, context, "RetainLevelZeroTiles");
        WWXML.checkAndAppendBooleanElement(params, AVKey.COMPRESS_TEXTURES, context, "CompressTextures");
        WWXML.checkAndAppendBooleanElement(params, AVKey.USE_MIP_MAPS, context, "UseMipMaps");
        WWXML.checkAndAppendBooleanElement(params, AVKey.USE_TRANSPARENT_TEXTURES, context, "UseTransparentTextures");
        WWXML.checkAndAppendDoubleElement(params, AVKey.SPLIT_SCALE, context, "SplitScale");

        // Retrieval properties.
        if (params.getValue(AVKey.URL_CONNECT_TIMEOUT) != null ||
            params.getValue(AVKey.URL_READ_TIMEOUT) != null ||
            params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT) != null)
        {
            Element el = WWXML.getElement(context, "RetrievalTimeouts", xpath);
            if (el == null)
                el = WWXML.appendElementPath(context, "RetrievalTimeouts");

            WWXML.checkAndAppendTimeElement(params, AVKey.URL_CONNECT_TIMEOUT, el, "ConnectTimeout/Time");
            WWXML.checkAndAppendTimeElement(params, AVKey.URL_READ_TIMEOUT, el, "ReadTimeout/Time");
            WWXML.checkAndAppendTimeElement(params, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, el,
                "StaleRequestLimit/Time");
        }

        return context;
    }

    /**
     * Parses TiledImageLayer configuration parameters from the specified DOM document. This writes output as key-value
     * pairs to params. If a parameter from the XML document already exists in params, that parameter is ignored.
     * Supported key and parameter names are: <table> <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr>
     * <tr><td>{@link AVKey#SERVICE_NAME}</td><td>Service/@serviceName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#IMAGE_FORMAT}</td><td>ImageFormat</td><td>String</td></tr> <tr><td>{@link
     * AVKey#AVAILABLE_IMAGE_FORMATS}</td><td>AvailableImageFormats/ImageFormat</td><td>String array</td></tr>
     * <tr><td>{@link AVKey#FORCE_LEVEL_ZERO_LOADS}</td><td>ForceLevelZeroLoads</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#RETAIN_LEVEL_ZERO_TILES}</td><td>RetainLevelZeroTiles</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#COMPRESS_TEXTURES}</td><td>CompressTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_MIP_MAPS}</td><td>UseMipMaps</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_TRANSPARENT_TEXTURES}</td><td>UseTransparentTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#URL_CONNECT_TIMEOUT}</td><td>RetrievalTimeouts/ConnectTimeout/Time</td><td>Integer milliseconds</td></tr>
     * <tr><td>{@link AVKey#URL_READ_TIMEOUT}</td><td>RetrievalTimeouts/ReadTimeout/Time</td><td>Integer
     * milliseconds</td></tr> <tr><td>{@link AVKey#RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT}</td><td>RetrievalTimeouts/StaleRequestLimit/Time</td><td>Integer
     * milliseconds</td></tr> </table> This also parses common layer and LevelSet configuration parameters by invoking
     * {@link gov.nasa.worldwind.layers.AbstractLayer#getLayerConfigParams(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)} and {@link gov.nasa.worldwind.util.DataConfigurationUtils#getLevelSetConfigParams(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)}.
     *
     * @param domElement the XML document root to parse for TiledImageLayer configuration parameters.
     * @param params     the output key-value pairs which recieve the TiledImageLayer configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getTiledImageLayerConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        XPath xpath = WWXML.makeXPath();

        // Common layer properties.
        AbstractLayer.getLayerConfigParams(domElement, params);

        // LevelSet properties.
        DataConfigurationUtils.getLevelSetConfigParams(domElement, params);

        // Service properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE_NAME, "Service/@serviceName", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETRIEVE_PROPERTIES_FROM_SERVICE,
            "RetrievePropertiesFromService", xpath);

        // Image format properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.IMAGE_FORMAT, "ImageFormat", xpath);
        WWXML.checkAndSetUniqueStringsParam(domElement, params, AVKey.AVAILABLE_IMAGE_FORMATS,
            "AvailableImageFormats/ImageFormat", xpath);

        // Optional behavior properties.
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.FORCE_LEVEL_ZERO_LOADS, "ForceLevelZeroLoads", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETAIN_LEVEL_ZERO_TILES, "RetainLevelZeroTiles", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.COMPRESS_TEXTURES, "CompressTextures",
            xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_MIP_MAPS, "UseMipMaps", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_TRANSPARENT_TEXTURES, "UseTransparentTextures",
            xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.SPLIT_SCALE, "SplitScale", xpath);
        WWXML.checkAndSetColorArrayParam(domElement, params, AVKey.TRANSPARENCY_COLORS, "TransparencyColors/Color",
            xpath);

        // Retrieval properties. Convert the Long time values to Integers, because BasicTiledImageLayer is expecting
        // Integer values.
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_CONNECT_TIMEOUT,
            "RetrievalTimeouts/ConnectTimeout/Time", xpath);
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_READ_TIMEOUT,
            "RetrievalTimeouts/ReadTimeout/Time", xpath);
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT,
            "RetrievalTimeouts/StaleRequestLimit/Time", xpath);

        return params;
    }

    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //

    public ArrayList<String> getAvailableImageFormats()
    {
        return new ArrayList<String>(this.supportedImageFormats);
    }

    public boolean isImageFormatAvailable(String imageFormat)
    {
        return imageFormat != null && this.supportedImageFormats.contains(imageFormat);
    }

    public String getDefaultImageFormat()
    {
        return this.supportedImageFormats.size() > 0 ? this.supportedImageFormats.get(0) : null;
    }

    protected void setAvailableImageFormats(String[] formats)
    {
        this.supportedImageFormats.clear();

        if (formats != null)
            this.supportedImageFormats.addAll(Arrays.asList(formats));
    }

    protected BufferedImage requestImage(TextureTile tile, String mimeType)
        throws URISyntaxException, InterruptedIOException, MalformedURLException
    {
        String pathBase = tile.getPathBase();
        String suffix = WWIO.makeSuffixForMimeType(mimeType);
        String path = pathBase + suffix;
        File f = new File(path);
        URL url;
        if (f.isAbsolute() && f.exists())
            url = f.toURI().toURL();
        else
            url = this.getDataFileStore().findFile(path, false);

        if (url == null) // image is not local
            return null;

        if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
        {
            // The file has expired. Delete it.
            this.getDataFileStore().removeFile(url);
            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.logger().fine(message);
        }
        else
        {
            try
            {
                File imageFile = new File(url.toURI());
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null)
                {
                    String message = Logging.getMessage("generic.ImageReadFailed", imageFile);
                    throw new RuntimeException(message);
                }

                this.levels.unmarkResourceAbsent(tile);
                return image;
            }
            catch (InterruptedIOException e)
            {
                throw e;
            }
            catch (IOException e)
            {
                // Assume that something's wrong with the file and delete it.
                this.getDataFileStore().removeFile(url);
                this.levels.markResourceAbsent(tile);
                String message = Logging.getMessage("generic.DeletedCorruptDataFile", url);
                Logging.logger().info(message);
            }
        }

        return null;
    }

    protected void downloadImage(final TextureTile tile, String mimeType, int timeout) throws Exception
    {
        final URL resourceURL = tile.getResourceURL(mimeType); // TODO: check for null
        Retriever retriever;

        String protocol = resourceURL.getProtocol();

        if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))
        {
            retriever = new HTTPRetriever(resourceURL, new CompositionRetrievalPostProcessor(tile));
        }
        else
        {
            String message = Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", resourceURL);
            throw new RuntimeException(message);
        }

        Logging.logger().log(java.util.logging.Level.FINE, "Retrieving " + resourceURL.toString());
        retriever.setConnectTimeout(10000);
        retriever.setReadTimeout(timeout);
        retriever.call();
    }

    public int computeLevelForResolution(Sector sector, double resolution)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Find the first level exceeding the desired resolution
        double texelSize;
        Level targetLevel = this.levels.getLastLevel();
        for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (this.levels.isLevelEmpty(i))
                continue;

            texelSize = this.levels.getLevel(i).getTexelSize();
            if (texelSize > resolution)
                continue;

            targetLevel = this.levels.getLevel(i);
            break;
        }

        // Choose the level closest to the resolution desired
        if (targetLevel.getLevelNumber() != 0 && !this.levels.isLevelEmpty(targetLevel.getLevelNumber() - 1))
        {
            Level nextLowerLevel = this.levels.getLevel(targetLevel.getLevelNumber() - 1);
            double dless = Math.abs(nextLowerLevel.getTexelSize() - resolution);
            double dmore = Math.abs(targetLevel.getTexelSize() - resolution);
            if (dless < dmore)
                targetLevel = nextLowerLevel;
        }

        Logging.logger().fine(Logging.getMessage("layers.TiledImageLayer.LevelSelection",
            targetLevel.getLevelNumber(), Double.toString(targetLevel.getTexelSize())));
        return targetLevel.getLevelNumber();
    }

    /**
     * Create an image for the portion of this layer lying within a specified sector. The image is created at a
     * specified aspect ratio within a canvas of a specified size. This returns the specified image if this layer has no
     * content in the specified sector.
     *
     * @param sector       the sector of interest.
     * @param canvasWidth  the width of the canvas.
     * @param canvasHeight the height of the canvas.
     * @param aspectRatio  the aspect ratio, width/height, of the window. If the aspect ratio is greater or equal to
     *                     one, the full width of the canvas is used for the image; the height used is proportional to
     *                     the inverse of the aspect ratio. If the aspect ratio is less than one, the full height of the
     *                     canvas is used, and the width used is proportional to the aspect ratio.
     * @param levelNumber  the target level of the tiled image layer.
     * @param mimeType     the type of image to create, e.g., "png" and "jpg".
     * @param abortOnError indicates whether to stop assembling the image if an error occurs. If false, processing
     *                     continues until all portions of the layer that intersect the specified sector have been added
     *                     to the image. Portions for which an error occurs will be blank.
     * @param image        if non-null, a {@link BufferedImage} in which to place the image. If null, a new buffered
     *                     image is created. The image must be the width and height specified in the
     *                     <code>canvasWidth</code> and <code>canvasHeight</code> arguments.
     * @param timeout      The amount of time to allow for reading the image from the server.
     *
     * @return image        the assembled image, of size indicated by the <code>canvasWidth</code> and
     *         <code>canvasHeight</code>. If the specified aspect ratio is one, all pixels contain values. If the aspect
     *         ratio is greater than one, a full-width segment along the top of the canvas is blank. If the aspect ratio
     *         is less than one, a full-height segment along the right side of the canvase is blank. If the
     *         <code>image</code> argument was non-null, that buffered image is returned.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null.
     * @see ImageUtil#mergeImage ;
     */
    public BufferedImage composeImageForSector(Sector sector, int canvasWidth, int canvasHeight, double aspectRatio,
        int levelNumber, String mimeType, boolean abortOnError, BufferedImage image, int timeout) throws Exception
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.levels.getSector().intersects(sector))
        {
            Logging.logger().severe(Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", sector,
                this.levels.getSector()));
            return image;
        }

        Sector intersection = this.levels.getSector().intersection(sector);

        if (levelNumber < 0)
        {
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }
        else if (levelNumber > this.levels.getLastLevel().getLevelNumber())
        {
            Logging.logger().warning(Logging.getMessage("generic.LevelRequestedGreaterThanMaxLevel",
                levelNumber, this.levels.getLastLevel().getLevelNumber()));
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }

        int numTiles = 0;
        TextureTile[][] tiles = this.getTilesInSector(intersection, levelNumber);
        for (TextureTile[] row : tiles)
        {
            numTiles += row.length;
        }

        if (tiles.length == 0 || tiles[0].length == 0)
        {
            Logging.logger().severe(Logging.getMessage("layers.TiledImageLayer.NoImagesAvailable"));
            return image;
        }

        if (image == null)
            image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);

        double tileCount = 0;
        for (TextureTile[] row : tiles)
        {
            for (TextureTile tile : row)
            {
                if (tile == null)
                    continue;

                BufferedImage tileImage;
                try
                {
                    tileImage = this.getImage(tile, mimeType, timeout);
                    Thread.sleep(1); // generates InterruptedException if thread has been interupted

                    if (tileImage != null)
                        ImageUtil.mergeImage(sector, tile.getSector(), aspectRatio, tileImage, image);

                    this.firePropertyChange(AVKey.PROGRESS, tileCount / numTiles, ++tileCount / numTiles);
                }
                catch (InterruptedException e)
                {
                    throw e;
                }
                catch (InterruptedIOException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    if (abortOnError)
                        throw e;

                    String message = Logging.getMessage("generic.ExceptionWhileRequestingImage", tile.getPath());
                    Logging.logger().log(java.util.logging.Level.WARNING, message, e);
                }
            }
        }

        return image;
    }

    public long countImagesInSector(Sector sector)
    {
        long count = 0;
        for (int i = 0; i <= this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (!this.levels.isLevelEmpty(i))
                count += countImagesInSector(sector, i);
        }
        return count;
    }

    public long countImagesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.levels.getTileOrigin();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        long numRows = nwRow - seRow + 1;
        long numCols = seCol - nwCol + 1;

        return numRows * numCols;
    }

    public TextureTile[][] getTilesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.levels.getTileOrigin();
        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;
        TextureTile[][] sectorTiles = new TextureTile[numRows][numCols];

        for (int row = nwRow; row >= seRow; row--)
        {
            for (int col = nwCol; col <= seCol; col++)
            {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                Sector tileSector = this.levels.computeSectorForKey(key);
                sectorTiles[nwRow - row][col - nwCol] = createTile(tileSector, targetLevel, row, col);
            }
        }

        return sectorTiles;
    }

    private BufferedImage getImage(TextureTile tile, String mimeType, int timeout) throws Exception
    {
        // Read the image from disk.
        BufferedImage image = this.requestImage(tile, mimeType);
        Thread.sleep(1); // generates InterruptedException if thread has been interupted
        if (image != null)
            return image;

        // If the level for this tile doesn't have a service, then there's nothing to download. Return null indicating
        // that no data is available for this tile.
        if (WWUtil.isEmpty(tile.getLevel().getService()))
            return null;

        // Retrieve it from the net since it's not on disk.
        this.downloadImage(tile, mimeType, timeout);

        // Try to read from disk again after retrieving it from the net.
        image = this.requestImage(tile, mimeType);
        Thread.sleep(1); // generates InterruptedException if thread has been interupted
        if (image == null)
        {
            String message =
                Logging.getMessage("layers.TiledImageLayer.ImageUnavailable", tile.getPath());
            throw new RuntimeException(message);
        }

        return image;
    }

    protected class CompositionRetrievalPostProcessor extends AbstractRetrievalPostProcessor
    {
        protected TextureTile tile;

        public CompositionRetrievalPostProcessor(TextureTile tile)
        {
            this.tile = tile;
        }

        protected File doGetOutputFile()
        {
            String suffix = WWIO.makeSuffixForMimeType(this.getRetriever().getContentType());
            if (suffix == null)
            {
                Logging.logger().severe(
                    Logging.getMessage("generic.UnknownContentType", this.getRetriever().getContentType()));
                return null;
            }

            String path = this.tile.getPathBase();
            path += suffix;

            File f = new File(path);
            final File outFile = f.isAbsolute() ? f : getDataFileStore().newFile(path);
            if (outFile == null)
                return null;

            return outFile;
        }

        @Override
        protected boolean isDeleteOnExit(File outFile)
        {
            return outFile.getPath().contains(WWIO.DELETE_ON_EXIT_PREFIX);
        }

        @Override
        protected boolean overwriteExistingFile()
        {
            return true;
        }

        protected void markResourceAbsent()
        {
            TiledImageLayer.this.levels.markResourceAbsent(tile);
        }

        protected void handleUnsuccessfulRetrieval()
        {
            // Don't mark the tile as absent because the caller may want to try again.
        }
    }
    
    protected TextureTile createTile(Sector sector, Level level, int row, int col)
    {
    	return new TextureTile(sector, level, row, col);
    }
}
