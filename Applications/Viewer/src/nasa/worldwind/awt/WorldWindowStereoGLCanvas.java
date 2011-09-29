/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.awt;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowGLDrawable;
import gov.nasa.worldwind.WorldWindowImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.NoOpInputHandler;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;

import java.awt.GraphicsDevice;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;

/**
 * An implementation of {@link WorldWindow} based on the {@link WorldWindowGLCanvas} that provides support for
 * stereo rendering.
 */
public class WorldWindowStereoGLCanvas extends GLCanvas implements WorldWindow, PropertyChangeListener
{
    public static final GLCapabilities defaultCaps = new GLCapabilities();
    public static final GLCapabilities stereoCaps = new GLCapabilities();

    static
    {
        defaultCaps.setAlphaBits(8);
        defaultCaps.setRedBits(8);
        defaultCaps.setGreenBits(8);
        defaultCaps.setBlueBits(8);
        defaultCaps.setDepthBits(24);
        
        stereoCaps.setAlphaBits(8);
        stereoCaps.setRedBits(8);
        stereoCaps.setGreenBits(8);
        stereoCaps.setBlueBits(8);
        stereoCaps.setDepthBits(24);
        stereoCaps.setStereo(true);
    }

    private final WorldWindowGLDrawable wwd; // WorldWindow interface delegates to wwd

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> window on the default graphics device.
     */
    public WorldWindowStereoGLCanvas(GLCapabilities caps)
    {
        super(caps);
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> window on the default graphics device that will share graphics
     * resources with another <code>WorldWindowGLCanvas</code> window. The other window, <code>sharewith</code>, may not
     * be null
     *
     * @param shareWith a <code>WorldWindowGLCanvas</code> with which to share graphics resources.
     *
     * @throws NullPointerException if shareWith is null.
     * @see GLCanvas#GLCanvas(GLCapabilities,GLCapabilitiesChooser,GLContext,GraphicsDevice)
     */
    public WorldWindowStereoGLCanvas(WorldWindowStereoGLCanvas shareWith)
    {
        super(defaultCaps, null, shareWith.getContext(), null);
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> window that will share graphics resources with another
     * <code>WorldWindowGLCanvas</code> window. The new window is created on the specified graphics device. Neither
     * <code> shareWith</code> or <code>device</code> may be null.
     *
     * @param shareWith a <code>WorldWindowGLCanvas</code> with which to share graphics resources.
     * @param device    the <code>GraphicsDevice</code> on which to create the window.
     *
     * @throws NullPointerException     if <code>shareWith</code> is null.
     * @throws IllegalArgumentException if <code>device</code> is null.
     * @see GLCanvas#GLCanvas(GLCapabilities,GLCapabilitiesChooser,GLContext,GraphicsDevice)
     */
    public WorldWindowStereoGLCanvas(WorldWindowStereoGLCanvas shareWith, java.awt.GraphicsDevice device)
    {
        super(defaultCaps, null, shareWith.getContext(), device);

        if (device == null)
        {
            String msg = Logging.getMessage("nullValue.DeviceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Constructs a new <code>WorldWindowGLCanvas</code> window that will share graphics resources with another
     * <code>WorldWindowGLCanvas</code> window and whose capabilities are chosen via a specified {@link GLCapabilities}
     * object and a {@link javax.media.opengl.GLCapabilitiesChooser}. The new window is created on the specified
     * graphics device. Neither <code> shareWith</code> or <code>device</code> may be null.
     *
     * @param shareWith    a <code>WorldWindowGLCanvas</code> with which to share graphics resources. May not be null.
     * @param device       the <code>GraphicsDevice</code> on which to create the window.
     * @param capabilities a capabilities object indicating the OpenGL rendering context's capabilities. May be null, in
     *                     which case a default set of capabilities is used.
     * @param chooser      a chooser object that customizes the specified capabilities. May be null, in which case a
     *                     default chooser is used.
     *
     * @throws NullPointerException     if <code>shareWith</code> is null.
     * @throws IllegalArgumentException if <code>device</code> is null.
     * @see GLCanvas#GLCanvas(GLCapabilities,GLCapabilitiesChooser,GLContext,GraphicsDevice)
     */
    public WorldWindowStereoGLCanvas(WorldWindowStereoGLCanvas shareWith, java.awt.GraphicsDevice device,
        GLCapabilities capabilities, GLCapabilitiesChooser chooser)
    {
        super(capabilities, chooser, shareWith.getContext(), device);

        if (device == null)
        {
            String msg = Logging.getMessage("nullValue.DeviceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        try
        {
            this.wwd = ((WorldWindowGLDrawable) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
            this.wwd.initDrawable(this);
            this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
            this.createView();
            this.createDefaultInputHandler();
            WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
            this.wwd.endInitialization();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    @Override
	public void propertyChange(PropertyChangeEvent evt)
    {
        //noinspection StringEquality
        if (evt.getPropertyName() == WorldWind.SHUTDOWN_EVENT)
		{
			this.shutdown();
		}
    }

    @Override
	public void shutdown()
    {
        WorldWind.removePropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
        this.wwd.shutdown();
    }

    private void createView()
    {
        this.setView((View) WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME));
    }

    private void createDefaultInputHandler()
    {
        this.setInputHandler((InputHandler) WorldWind.createConfigurationComponent(AVKey.INPUT_HANDLER_CLASS_NAME));
    }

    @Override
	public InputHandler getInputHandler()
    {
        return this.wwd.getInputHandler();
    }

    @Override
	public void setInputHandler(InputHandler inputHandler)
    {
        if (this.wwd.getInputHandler() != null)
		 {
			this.wwd.getInputHandler().setEventSource(null); // remove this window as a source of events
		}

        this.wwd.setInputHandler(inputHandler != null ? inputHandler : new NoOpInputHandler());
        if (inputHandler != null)
		{
			inputHandler.setEventSource(this);
		}
    }

    @Override
	public SceneController getSceneController()
    {
        return this.wwd.getSceneController();
    }
    
    @Override
    public void setSceneController(SceneController sceneController)
    {
    	this.wwd.setSceneController(sceneController);
    }

    @Override
	public void redraw()
    {
        this.repaint();
    }

    @Override
	public void redrawNow()
    {
        this.wwd.redrawNow();
    }

    @Override
	public void setModel(Model model)
    {
        // null models are permissible
        this.wwd.setModel(model);
    }

    @Override
	public Model getModel()
    {
        return this.wwd.getModel();
    }

    @Override
	public void setView(View view)
    {
        // null views are permissible
        if (view != null)
		{
			this.wwd.setView(view);
		}
    }

    @Override
	public View getView()
    {
        return this.wwd.getView();
    }

    @Override
	public void setModelAndView(Model model, View view)
    {   // null models/views are permissible
        this.setModel(model);
        this.setView(view);
    }

    @Override
	public void addRenderingListener(RenderingListener listener)
    {
        this.wwd.addRenderingListener(listener);
    }

    @Override
	public void removeRenderingListener(RenderingListener listener)
    {
        this.wwd.removeRenderingListener(listener);
    }

    @Override
	public void addSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().addSelectListener(listener);
        this.wwd.addSelectListener(listener);
    }

    @Override
	public void removeSelectListener(SelectListener listener)
    {
        this.wwd.getInputHandler().removeSelectListener(listener);
        this.wwd.removeSelectListener(listener);
    }

    @Override
	public void addPositionListener(PositionListener listener)
    {
        this.wwd.addPositionListener(listener);
    }

    @Override
	public void removePositionListener(PositionListener listener)
    {
        this.wwd.removePositionListener(listener);
    }

    @Override
	public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.addRenderingExceptionListener(listener);
    }

    @Override
	public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        this.wwd.removeRenderingExceptionListener(listener);
    }

    @Override
	public Position getCurrentPosition()
    {
        return this.wwd.getCurrentPosition();
    }

    @Override
	public PickedObjectList getObjectsAtCurrentPosition()
    {
        return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getPickedObjectList() : null;
    }

    @Override
	public Object setValue(String key, Object value)
    {
        return this.wwd.setValue(key, value);
    }

    @Override
	public AVList setValues(AVList avList)
    {
        return this.wwd.setValues(avList);
    }

    @Override
	public Object getValue(String key)
    {
        return this.wwd.getValue(key);
    }

    @Override
	public Collection<Object> getValues()
    {
        return this.wwd.getValues();
    }

    @Override
	public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.wwd.getEntries();
    }

    @Override
	public String getStringValue(String key)
    {
        return this.wwd.getStringValue(key);
    }

    @Override
	public boolean hasKey(String key)
    {
        return this.wwd.hasKey(key);
    }

    @Override
	public Object removeKey(String key)
    {
        return this.wwd.removeKey(key);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(listener);
        this.wwd.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        super.addPropertyChangeListener(propertyName, listener);
        this.wwd.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        super.removePropertyChangeListener(listener);
        this.wwd.removePropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        super.removePropertyChangeListener(listener);
        this.wwd.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.wwd.firePropertyChange(propertyChangeEvent);
    }

    @Override
	public AVList copy()
    {
        return this.wwd.copy();
    }

    @Override
	public AVList clearList()
    {
        return this.wwd.clearList();
    }

    @Override
	public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        this.wwd.setPerFrameStatisticsKeys(keys);
    }

    @Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.wwd.getPerFrameStatistics();
    }
    
    @Override
    public GpuResourceCache getGpuResourceCache()
    {
    	return this.wwd.getGpuResourceCache();
    }
  
}
