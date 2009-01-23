/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.render;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.AnnotationRenderer;
import gov.nasa.worldwind.render.BasicAnnotationRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.render.TextRendererCache;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;

import java.awt.Color;
import java.awt.Point;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

import nasa.worldwind.layers.multitexture.GeographicSurfaceMultiTileRenderer;
import nasa.worldwind.layers.multitexture.SurfaceMultiTileRenderer;

import com.sun.opengl.util.texture.TextureCoords;

/**
 * @author Tom Gaskins
 * @version $Id: DrawContextImpl.java 5113 2008-04-21 23:46:50Z tgaskins $
 */
public class DrawContextImpl extends WWObjectImpl implements DrawContext
{
    private GLContext glContext;
    private GLU glu = new GLU();
    private View view;
    private Model model;
    private Globe globe;
    private double verticalExaggeration = 1d;
    private Sector visibleSector;
    private SectorGeometryList surfaceGeometry;
    private PickedObjectList pickedObjects = new PickedObjectList();
    private int uniquePickNumber = 0;
    private Color clearColor = new Color(0, 0, 0, 0);
    private boolean isPickingMode = false;
    private Point pickPoint = null;
    private Point viewportCenterScreenPoint = null;
    private Position viewportCenterPosition = null;
    private Vec4 viewportCenterSurfacePoint = null;
    private Vec4 viewportCenterGlobePoint = null;
    private int numTextureUnits = -1;
    private SurfaceMultiTileRenderer geographicSurfaceTileRenderer = new GeographicSurfaceMultiTileRenderer();
    private AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();
    private TextureCache textureCache;
    private TextRendererCache textRendererCache;
    private Set<String> perFrameStatisticsKeys;
    private Collection<PerformanceStatistic> perFrameStatistics;

    PriorityQueue<OrderedRenderable> orderedRenderables =
        new PriorityQueue<OrderedRenderable>(100, new Comparator<OrderedRenderable>()
        {
            public int compare(OrderedRenderable orA, OrderedRenderable orB)
            {
                double eA = orA.getDistanceFromEye();
                double eB = orB.getDistanceFromEye();

                return eA > eB ? -1 : eA == eB ? 0 : 1;
            }
        });

    public void reinitialize()
    {
        if (this.geographicSurfaceTileRenderer != null)
            this.geographicSurfaceTileRenderer.dispose();
        this.geographicSurfaceTileRenderer = new GeographicSurfaceMultiTileRenderer();

        this.pickedObjects.clear();
        this.perFrameStatistics.clear();
        this.perFrameStatisticsKeys.clear();
        this.annotationRenderer = new BasicAnnotationRenderer();
    }

    /**
     * Free internal resources held by this draw context.
     * A GL context must be current when this method is called.
     *
     * @throws javax.media.opengl.GLException - If an OpenGL context is not current when this method is called.
     */
    public void dispose()
    {
        this.geographicSurfaceTileRenderer.dispose();
    }

    public final GL getGL()
    {
        return this.getGLContext().getGL();
    }

    public final GLU getGLU()
    {
        return this.glu;
    }

    public final GLContext getGLContext()
    {
        return this.glContext;
    }

    public final int getDrawableHeight()
    {
        return this.getGLDrawable().getHeight();
    }

    public final int getDrawableWidth()
    {
        return this.getGLDrawable().getWidth();
    }

    public final GLDrawable getGLDrawable()
    {
        return this.getGLContext().getGLDrawable();
    }

    public final void initialize(GLContext glContext)
    {
        if (glContext == null)
        {
            String message = Logging.getMessage("nullValue.GLContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.glContext = glContext;
        this.visibleSector = null;
        if (this.surfaceGeometry != null)
            this.surfaceGeometry.clear();
        this.surfaceGeometry = null;

        this.pickedObjects.clear();
        this.orderedRenderables.clear();
        this.uniquePickNumber = 0;

        if (this.numTextureUnits < 1)
            this.numTextureUnits = queryMaxTextureUnits(glContext);
    }

    private static int queryMaxTextureUnits(GLContext glContext)
    {
        int[] mtu = new int[1];
        glContext.getGL().glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, mtu, 0);
        return mtu[0];
    }

    public final void setModel(Model model)
    {
        this.model = model;
        if (this.model == null)
            return;

        Globe g = this.model.getGlobe();
        if (g != null)
            this.globe = g;
    }

    public final Model getModel()
    {
        return this.model;
    }

    public final LayerList getLayers()
    {
        return this.model.getLayers();
    }

    public final Sector getVisibleSector()
    {
        return this.visibleSector;
    }

    public final void setVisibleSector(Sector s)
    {
        // don't check for null - it is possible that no globe is active, no view is active, no sectors visible, etc.
        this.visibleSector = s;
    }

    public void setSurfaceGeometry(SectorGeometryList surfaceGeometry)
    {
        this.surfaceGeometry = surfaceGeometry;
    }

    public SectorGeometryList getSurfaceGeometry()
    {
        return surfaceGeometry;
    }

    public final Globe getGlobe()
    {
        return this.globe != null ? this.globe : this.model.getGlobe();
    }

    public final void setView(View view)
    {
        this.view = view;
    }

    public final View getView()
    {
        return this.view;
    }

    public final void setGLContext(GLContext glContext)
    {
        if (glContext == null)
        {
            String message = Logging.getMessage("nullValue.GLContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.glContext = glContext;
    }

    public final double getVerticalExaggeration()
    {
        return verticalExaggeration;
    }

    public final void setVerticalExaggeration(double verticalExaggeration)
    {
        this.verticalExaggeration = verticalExaggeration;
    }

    public TextureCache getTextureCache()
    {
        return textureCache;
    }

    public void setTextureCache(TextureCache textureCache)
    {
        if (textureCache == null)
        {
            String msg = Logging.getMessage("nullValue.TextureCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.textureCache = textureCache;
    }

    public TextRendererCache getTextRendererCache()
    {
        return textRendererCache;
    }

    public void setTextRendererCache(TextRendererCache textRendererCache)
    {
        if (textRendererCache == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.textRendererCache = textRendererCache;
    }

    public AnnotationRenderer getAnnotationRenderer()
    {
        return annotationRenderer;
    }

    public void setAnnotationRenderer(AnnotationRenderer ar)
    {
        if (ar == null)
        {
            String msg = Logging.getMessage("nullValue.AnnotationRendererIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        annotationRenderer = ar;
    }

    public Point getPickPoint()
    {
        return pickPoint;
    }

    public void setPickPoint(Point pickPoint)
    {
        this.pickPoint = pickPoint;
    }

    public Point getViewportCenterScreenPoint()
    {
        return viewportCenterScreenPoint;
    }

    public void setViewportCenterScreenPoint(Point viewportCenterScreenPoint)
    {
        this.viewportCenterScreenPoint = viewportCenterScreenPoint;
    }

    public Position getViewportCenterPosition()
    {
        return viewportCenterPosition;
    }

    public void setViewportCenterPosition(Position viewportCenterPosition)
    {
        this.viewportCenterPosition = viewportCenterPosition;
        this.viewportCenterGlobePoint = null;
        this.viewportCenterSurfacePoint = null;
        if (viewportCenterPosition != null)
        {
            if (this.getGlobe() != null)
                this.viewportCenterGlobePoint = this.getGlobe().computePointFromPosition(
                    this.viewportCenterPosition.getLatitude(), this.viewportCenterPosition.getLongitude(), 0d);
            
            if (this.getSurfaceGeometry() != null)
                this.viewportCenterSurfacePoint =
                    this.getSurfaceGeometry().getSurfacePoint(this.viewportCenterPosition);
        }
    }

    public Vec4 getViewportCenterSurfacePoint()
    {
        return viewportCenterSurfacePoint;
    }

    public Vec4 getViewportCenterGlobePoint()
    {
        return viewportCenterGlobePoint;
    }

    /**
     * Add picked objects to the current list of picked objects.
     *
     * @param pickedObjects the list of picked objects to add
     * @throws IllegalArgumentException if <code>pickedObjects is null</code>
     */
    public void addPickedObjects(PickedObjectList pickedObjects)
    {
        if (pickedObjects == null)
        {
            String msg = Logging.getMessage("nullValue.PickedObjectList");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.pickedObjects == null)
        {
            this.pickedObjects = pickedObjects;
            return;
        }

        for (PickedObject po : pickedObjects)
        {
            this.pickedObjects.add(po);
        }
    }

    /**
     * Adds a single insatnce of the picked object to the current picked-object list
     *
     * @param pickedObject the object to add
     * @throws IllegalArgumentException if <code>picked Object is null</code>
     */
    public void addPickedObject(PickedObject pickedObject)
    {
        if (null == pickedObject)
        {
            String msg = Logging.getMessage("nullValue.PickedObject");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (null == this.pickedObjects)
            this.pickedObjects = new PickedObjectList();

        this.pickedObjects.add(pickedObject);
    }

    public PickedObjectList getPickedObjects()
    {
        return this.pickedObjects;
    }

    public Color getUniquePickColor()
    {
        this.uniquePickNumber++;
        int clearColorCode = this.getClearColor().getRGB();

        if (clearColorCode == this.uniquePickNumber)
            this.uniquePickNumber++;

        if (this.uniquePickNumber >= 0x00FFFFFF)
        {
            this.uniquePickNumber = 1;  // no black, no white
            if (clearColorCode == this.uniquePickNumber)
                this.uniquePickNumber++;
        }

        return new Color(this.uniquePickNumber, true); // has alpha
    }

    public Color getClearColor()
    {
        return this.clearColor;
    }

    /**
     * Returns true if the Picking mode is active, otherwise return false
     *
     * @return true for Picking mode, otherwise false
     */
    public boolean isPickingMode()
    {
        return this.isPickingMode;
    }

    /**
     * Enables color picking mode
     */
    public void enablePickingMode()
    {
        this.isPickingMode = true;
    }

    /**
     * Disables color picking mode
     */
    public void disablePickingMode()
    {
        this.isPickingMode = false;
    }

    public void addOrderedRenderable(OrderedRenderable orderedRenderable)
    {
        if (null == orderedRenderable)
        {
            String msg = Logging.getMessage("nullValue.OrderedRenderable");
            Logging.logger().warning(msg);
            return; // benign event
        }

        this.orderedRenderables.add(orderedRenderable);
    }

    public java.util.Queue<OrderedRenderable> getOrderedRenderables()
    {
        return this.orderedRenderables;
    }

    public void drawUnitQuad()
    {
        GL gl = this.getGL();

        gl.glBegin(GL.GL_QUADS); // TODO: use a vertex array or vertex buffer
        gl.glVertex2d(0d, 0d);
        gl.glVertex2d(1, 0d);
        gl.glVertex2d(1, 1);
        gl.glVertex2d(0d, 1);
        gl.glEnd();
    }

    public void drawUnitQuad(TextureCoords texCoords)
    {
        GL gl = this.getGL();

        gl.glBegin(GL.GL_QUADS); // TODO: use a vertex array or vertex buffer
        gl.glTexCoord2d(texCoords.left(), texCoords.bottom());
        gl.glVertex2d(0d, 0d);
        gl.glTexCoord2d(texCoords.right(), texCoords.bottom());
        gl.glVertex2d(1, 0d);
        gl.glTexCoord2d(texCoords.right(), texCoords.top());
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(texCoords.left(), texCoords.top());
        gl.glVertex2d(0d, 1);
        gl.glEnd();
    }

    public int getNumTextureUnits()
    {
        return numTextureUnits;
    }

    public void setNumTextureUnits(int numTextureUnits)
    {
        // TODO: validate arg for >= 1
        this.numTextureUnits = numTextureUnits;
    }

    public Vec4 getPointOnGlobe(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.getVisibleSector() == null)
            return null;

        if (!this.getVisibleSector().contains(latitude, longitude))
            return null;

        SectorGeometryList sectorGeometry = this.getSurfaceGeometry();
        if (sectorGeometry != null)
        {
            Vec4 p = sectorGeometry.getSurfacePoint(latitude, longitude);
            if (p != null)
                return p;
        }

        return null;
    }

    public SurfaceTileRenderer getGeographicSurfaceTileRenderer()
    {
        return this.geographicSurfaceTileRenderer;
    }

    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.perFrameStatistics;
    }

    public void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats)
    {
        this.perFrameStatisticsKeys = statKeys;
        this.perFrameStatistics = stats;
    }

    public Set<String> getPerFrameStatisticsKeys()
    {
        return perFrameStatisticsKeys;
    }

    public void setPerFrameStatistic(String key, String displayName, Object value)
    {
        if (this.perFrameStatistics == null || this.perFrameStatisticsKeys == null)
            return;

        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull=Key is null");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (displayName == null)
        {
            String message = Logging.getMessage("nullValue.DisplayNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.perFrameStatisticsKeys.contains(key) || this.perFrameStatisticsKeys.contains(PerformanceStatistic.ALL))
            this.perFrameStatistics.add(new PerformanceStatistic(key, displayName, value));
    }

    public void setPerFrameStatistics(Collection<PerformanceStatistic> stats)
    {
        if (this.perFrameStatistics == null || this.perFrameStatisticsKeys == null)
            return;

        if (stats == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull=Key is null"); // TODO
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (PerformanceStatistic stat : stats)
            this.perFrameStatistics.add(stat);
    }
}
