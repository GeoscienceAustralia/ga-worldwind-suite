package au.gov.ga.worldwind.viewer.layers.shapefile;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.cache.TextureCache;
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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.render.LightingModel;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.OutlinedShape;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.render.SurfaceObjectRenderer;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.render.TextRendererCache;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.PickPointFrustumList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.texture.TextureCoords;

public class FakeDrawContext implements DrawContext
{
	private Globe globe;

	public FakeDrawContext()
	{
		globe =
				new EllipsoidalGlobe(Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS,
						Earth.WGS84_ES, new ZeroElevationModel());
	}


	@Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPickPointFrustum()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPickedObject(PickedObject pickedObject)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPickedObjects(PickedObjectList pickedObjects)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addScreenCredit(ScreenCredit credit)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void disablePickingMode()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawUnitQuad()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawUnitQuad(TextureCoords texCoords)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void enablePickingMode()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AnnotationRenderer getAnnotationRenderer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getClearColor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layer getCurrentLayer()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDrawableHeight()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDrawableWidth()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getFrameTimeStamp()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public GL getGL()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GLContext getGLContext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GLDrawable getGLDrawable()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GLRuntimeCapabilities getGLRuntimeCapabilities()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GLU getGLU()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model getModel()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getPerFrameStatisticsKeys()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PickPointFrustumList getPickFrustums()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getPickPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getPickPointFrustumDimension()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PickedObjectList getPickedObjects()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRedrawRequested()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<ScreenCredit, Long> getScreenCredits()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SectorGeometryList getSurfaceGeometry()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextRendererCache getTextRendererCache()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextureCache getTextureCache()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getUniquePickColor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getVerticalExaggeration()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Position getViewportCenterPosition()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getViewportCenterScreenPoint()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sector getVisibleSector()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sector> getVisibleSectors(double[] resolutions, long timeLimit, Sector searchSector)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(GLContext glContext)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPickingMode()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void popProjectionOffest()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void pushProjectionOffest(Double offset)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setAnnotationRenderer(AnnotationRenderer annotationRenderer)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentLayer(Layer layer)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setFrameTimeStamp(long frameTimeStamp)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setGLContext(GLContext glContext)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setModel(Model model)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPerFrameStatistic(String key, String displayName, Object statistic)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPerFrameStatistics(Collection<PerformanceStatistic> stats)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPerFrameStatisticsKeys(Set<String> statKeys,
			Collection<PerformanceStatistic> stats)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPickPoint(Point pickPoint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPickPointFrustumDimension(Dimension dim)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setRedrawRequested(int redrawRequested)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSurfaceGeometry(SectorGeometryList surfaceGeometry)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setTextRendererCache(TextRendererCache textRendererCache)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setTextureCache(TextureCache textureCache)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticalExaggeration(double verticalExaggeration)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setView(View view)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setViewportCenterPosition(Position viewportCenterPosition)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setViewportCenterScreenPoint(Point viewportCenterPoint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisibleSector(Sector s)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public AVList clearList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AVList copy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringValue(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValue(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> getValues()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasKey(String key)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object removeKey(String key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object setValue(String key, Object value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AVList setValues(AVList avList)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addRenderingException(Throwable t)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vec4 getPointOnTerrain(Angle latitude, Angle longitude)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<Throwable> getRenderingExceptions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOrderedRenderingMode()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOrderedRenderingMode(boolean tf)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRenderingExceptions(Collection<Throwable> exceptions)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOrderedSurfaceRenderable(OrderedRenderable orderedRenderable)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Queue<OrderedRenderable> getOrderedSurfaceRenderables()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawUnitQuadOutline()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public MemoryCache getSurfaceObjectRendererCache()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSurfaceObjectRendererCache(MemoryCache cache)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSurfaceObjectRenderer(Object key, SurfaceObjectRenderer renderer)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SurfaceObjectRenderer getSurfaceObjectRenderer(Object key)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawOutlinedShape(OutlinedShape renderer, Object shape)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginStandardLighting()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endStandardLighting()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public LightingModel getStandardLightingModel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStandardLightingModel(LightingModel standardLighting)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vec4 computeTerrainPoint(Angle lat, Angle lon, double offset,
			boolean applyVerticalExaggeration)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSmall(Extent extent, int numPixels)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawNormals(float length, FloatBuffer vBuf, FloatBuffer nBuf)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public OrderedRenderable peekOrderedRenderables()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderedRenderable pollOrderedRenderables()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
