package au.gov.ga.worldwind.viewer.layers.shapefile;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.AnnotationRenderer;
import gov.nasa.worldwind.render.DeclutteringTextRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.render.LightingModel;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.OutlinedShape;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.render.TextRendererCache;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.ClutterFilter;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.PickPointFrustumList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.texture.TextureCoords;

public class FakeDrawContext implements DrawContext
{
	private Globe globe;

	public FakeDrawContext()
	{
		globe =
				new EllipsoidalGlobe(Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS, Earth.WGS84_ES,
						new ZeroElevationModel());
	}

	@Override
	public Object setValue(String key, Object value)
	{

		return null;
	}

	@Override
	public AVList setValues(AVList avList)
	{

		return null;
	}

	@Override
	public Object getValue(String key)
	{

		return null;
	}

	@Override
	public Collection<Object> getValues()
	{

		return null;
	}

	@Override
	public String getStringValue(String key)
	{

		return null;
	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{

		return null;
	}

	@Override
	public boolean hasKey(String key)
	{

		return false;
	}

	@Override
	public Object removeKey(String key)
	{

		return null;
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{


	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{


	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{


	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{


	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{


	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{


	}

	@Override
	public AVList copy()
	{

		return null;
	}

	@Override
	public AVList clearList()
	{

		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{


	}

	@Override
	public void dispose()
	{


	}

	@Override
	public void setGLContext(GLContext glContext)
	{


	}

	@Override
	public GLContext getGLContext()
	{

		return null;
	}

	@Override
	public GL2 getGL()
	{

		return null;
	}

	@Override
	public GLU getGLU()
	{

		return null;
	}

	@Override
	public GLDrawable getGLDrawable()
	{

		return null;
	}

	@Override
	public int getDrawableWidth()
	{

		return 0;
	}

	@Override
	public int getDrawableHeight()
	{

		return 0;
	}

	@Override
	public GLRuntimeCapabilities getGLRuntimeCapabilities()
	{

		return null;
	}

	@Override
	public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
	{


	}

	@Override
	public void initialize(GLContext glContext)
	{


	}

	@Override
	public void setView(View view)
	{


	}

	@Override
	public View getView()
	{

		return null;
	}

	@Override
	public void setModel(Model model)
	{


	}

	@Override
	public Model getModel()
	{

		return null;
	}

	@Override
	public Globe getGlobe()
	{
		return globe;
	}

	@Override
	public LayerList getLayers()
	{

		return null;
	}

	@Override
	public Sector getVisibleSector()
	{

		return null;
	}

	@Override
	public void setVisibleSector(Sector s)
	{


	}

	@Override
	public void setVerticalExaggeration(double verticalExaggeration)
	{


	}

	@Override
	public double getVerticalExaggeration()
	{

		return 0;
	}

	@Override
	public SectorGeometryList getSurfaceGeometry()
	{

		return null;
	}

	@Override
	public PickedObjectList getPickedObjects()
	{

		return null;
	}

	@Override
	public void addPickedObjects(PickedObjectList pickedObjects)
	{


	}

	@Override
	public void addPickedObject(PickedObject pickedObject)
	{


	}

	@Override
	public Color getUniquePickColor()
	{

		return null;
	}

	@Override
	public Color getClearColor()
	{

		return null;
	}

	@Override
	public void enablePickingMode()
	{


	}

	@Override
	public boolean isPickingMode()
	{

		return false;
	}

	@Override
	public void disablePickingMode()
	{


	}

	@Override
	public void setDeepPickingEnabled(boolean tf)
	{


	}

	@Override
	public boolean isDeepPickingEnabled()
	{

		return false;
	}

	@Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable)
	{


	}

	@Override
	public void addOrderedSurfaceRenderable(OrderedRenderable orderedRenderable)
	{


	}

	@Override
	public Queue<OrderedRenderable> getOrderedSurfaceRenderables()
	{

		return null;
	}

	@Override
	public void drawUnitQuad()
	{


	}

	@Override
	public void drawUnitQuad(TextureCoords texCoords)
	{


	}

	@Override
	public void drawUnitQuadOutline()
	{


	}

	@Override
	public void setSurfaceGeometry(SectorGeometryList surfaceGeometry)
	{


	}

	@Override
	public Vec4 getPointOnTerrain(Angle latitude, Angle longitude)
	{

		return null;
	}

	@Override
	public SurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{

		return null;
	}

	@Override
	public Point getPickPoint()
	{

		return null;
	}

	@Override
	public void setPickPoint(Point pickPoint)
	{


	}

	@Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
	{

		return null;
	}

	@Override
	public void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats)
	{


	}

	@Override
	public void setPerFrameStatistic(String key, String displayName, Object statistic)
	{


	}

	@Override
	public void setPerFrameStatistics(Collection<PerformanceStatistic> stats)
	{


	}

	@Override
	public Set<String> getPerFrameStatisticsKeys()
	{

		return null;
	}

	@Override
	public Point getViewportCenterScreenPoint()
	{

		return null;
	}

	@Override
	public void setViewportCenterScreenPoint(Point viewportCenterPoint)
	{


	}

	@Override
	public Position getViewportCenterPosition()
	{

		return null;
	}

	@Override
	public void setViewportCenterPosition(Position viewportCenterPosition)
	{


	}

	@Override
	public TextRendererCache getTextRendererCache()
	{

		return null;
	}

	@Override
	public void setTextRendererCache(TextRendererCache textRendererCache)
	{


	}

	@Override
	public AnnotationRenderer getAnnotationRenderer()
	{

		return null;
	}

	@Override
	public void setAnnotationRenderer(AnnotationRenderer annotationRenderer)
	{


	}

	@Override
	public long getFrameTimeStamp()
	{

		return 0;
	}

	@Override
	public void setFrameTimeStamp(long frameTimeStamp)
	{


	}

	@Override
	public List<Sector> getVisibleSectors(double[] resolutions, long timeLimit, Sector searchSector)
	{

		return null;
	}

	@Override
	public void setCurrentLayer(Layer layer)
	{


	}

	@Override
	public Layer getCurrentLayer()
	{

		return null;
	}

	@Override
	public void addScreenCredit(ScreenCredit credit)
	{


	}

	@Override
	public Map<ScreenCredit, Long> getScreenCredits()
	{

		return null;
	}

	@Override
	public int getRedrawRequested()
	{

		return 0;
	}

	@Override
	public void setRedrawRequested(int redrawRequested)
	{


	}

	@Override
	public PickPointFrustumList getPickFrustums()
	{

		return null;
	}

	@Override
	public void setPickPointFrustumDimension(Dimension dim)
	{


	}

	@Override
	public Dimension getPickPointFrustumDimension()
	{

		return null;
	}

	@Override
	public void addPickPointFrustum()
	{


	}

	@Override
	public Collection<Throwable> getRenderingExceptions()
	{

		return null;
	}

	@Override
	public void setRenderingExceptions(Collection<Throwable> exceptions)
	{
	}

	@Override
	public void addRenderingException(Throwable t)
	{
	}

	@Override
	public void pushProjectionOffest(Double offset)
	{
	}

	@Override
	public void popProjectionOffest()
	{
	}

	@Override
	public boolean isOrderedRenderingMode()
	{
		return false;
	}

	@Override
	public void setOrderedRenderingMode(boolean tf)
	{
	}

	@Override
	public void drawOutlinedShape(OutlinedShape renderer, Object shape)
	{
	}

	@Override
	public void beginStandardLighting()
	{
	}

	@Override
	public void endStandardLighting()
	{
	}

	@Override
	public LightingModel getStandardLightingModel()
	{
		return null;
	}

	@Override
	public void setStandardLightingModel(LightingModel standardLighting)
	{

	}

	@Override
	public Vec4 computeTerrainPoint(Angle lat, Angle lon, double offset)
	{
		return null;
	}

	@Override
	public boolean isSmall(Extent extent, int numPixels)
	{
		return false;
	}

	@Override
	public void drawNormals(float length, FloatBuffer vBuf, FloatBuffer nBuf)
	{
	}

	@Override
	public OrderedRenderable peekOrderedRenderables()
	{
		return null;
	}

	@Override
	public OrderedRenderable pollOrderedRenderables()
	{
		return null;
	}

	@Override
	public Terrain getTerrain()
	{
		return null;
	}

	@Override
	public boolean isPreRenderMode()
	{
		return false;
	}

	@Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable, boolean isBehind)
	{
	}

	@Override
	public void onMessage(Message msg)
	{

	}

	@Override
	public GpuResourceCache getGpuResourceCache()
	{
		return null;
	}

	@Override
	public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
	{

	}

	@Override
	public void restoreDefaultBlending()
	{

	}

	@Override
	public void restoreDefaultCurrentColor()
	{

	}

	@Override
	public void restoreDefaultDepthTesting()
	{

	}

	@Override
	public void setPreRenderMode(boolean preRenderMode)
	{

	}

	@Override
	public GpuResourceCache getTextureCache()
	{
		return null;
	}

	@Override
	public PickedObjectList getObjectsInPickRectangle()
	{
		return null;
	}

	@Override
	public void addObjectInPickRectangle(PickedObject pickedObject)
	{
	}

	@Override
	public int getPickColorAtPoint(Point point)
	{
		return 0;
	}

	@Override
	public int[] getPickColorsInRectangle(Rectangle rectangle, int[] minAndMaxColorCodes)
	{
		return null;
	}

	@Override
	public Rectangle getPickRectangle()
	{
		return null;
	}

	@Override
	public void setPickRectangle(Rectangle pickRect)
	{
	}

	@Override
	public void addPickRectangleFrustum()
	{
	}

	@Override
	public Vec4 computePointFromPosition(Position position, int altitudeMode)
	{
		return null;
	}

	@Override
	public DeclutteringTextRenderer getDeclutteringTextRenderer()
	{
		return null;
	}

	@Override
	public void applyClutterFilter()
	{
	}

	@Override
	public ClutterFilter getClutterFilter()
	{
		return null;
	}

	@Override
	public void setClutterFilter(ClutterFilter arg0)
	{
	}
}
