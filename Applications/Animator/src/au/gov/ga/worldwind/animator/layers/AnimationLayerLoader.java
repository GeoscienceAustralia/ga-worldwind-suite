package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageDelegateFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageLocalRequesterDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.ImageURLRequesterDelegate;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A factory class that can load a {@link Layer} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 *
 */
public class AnimationLayerLoader
{
	/** 
	 * The layer factory instance to use.
	 * <p/>
	 * Defaults to an instance of the {@link LayerFactory}.
	 * <p/>
	 * Can be overriden with the {@link #setLayerFactory(BasicLayerFactory)} method.
	 */
	private static Factory layerFactory;
	
	static
	{
		ImageDelegateFactory.get().registerDelegate(ImmediateURLRequesterDelegate.class);
		ImageDelegateFactory.get().registerDelegate(ImmediateLocalRequesterDelegate.class);
		ImageDelegateFactory.get().registerReplacementClass(ImageURLRequesterDelegate.class, ImmediateURLRequesterDelegate.class);
		ImageDelegateFactory.get().registerReplacementClass(ImageLocalRequesterDelegate.class, ImmediateLocalRequesterDelegate.class);
	}
	
	/**
	 * @return The layer factory instance to use for creating layers
	 */
	public static Factory getLayerFactory()
	{
		if (layerFactory == null)
		{
			layerFactory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
		}
		return layerFactory;
	}
	
	/**
	 * @param layerFactory The layer factory instance to use for creating layers
	 */
	public static void setLayerFactory(BasicLayerFactory layerFactory)
	{
		AnimationLayerLoader.layerFactory = layerFactory;
	}
	
	/**
	 * Load the layer identified by the provided identifier.
	 * 
	 * @param identifier The identifier that specifies the layer to open
	 * 
	 * @return The layer identified by the provided identifier, or <code>null</code> if an error occurred during layer load
	 */
	public static Layer loadLayer(LayerIdentifier identifier)
	{
		try
		{
			URL sourceUrl = null;
			try
			{
				sourceUrl = new URL(identifier.getLocation());
			}
			catch (MalformedURLException e)
			{
				throw new IllegalArgumentException("Unable to locate Layer '"+ identifier.getName() +"' at provided location '" + identifier.getLocation() + "'", e);
			}
			Layer loadedLayer = loadLayer(sourceUrl);
			if (loadedLayer == null)
			{
				return loadedLayer;
			}
			loadedLayer.setName(identifier.getName());
			return loadedLayer;
		}
		catch (Throwable t)
		{
			ExceptionLogger.logException(t);
			return null;
		}
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL.
	 */
	public static Layer loadLayer(String url)
	{
		URL sourceUrl = null;
		try
		{
			sourceUrl = new URL(url);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Unable to locate Layer at provided location '" + url + "'", e);
		}
		return loadLayer(sourceUrl);
	}
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL, or <code>null</code> if an error occurred during layer loading.
	 */
	public static Layer loadLayer(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		try
		{
			Element element = XMLUtil.getElementFromSource(url);
			
			AVList params = new AVListImpl();
			params.setValue(AVKeyMore.CONTEXT_URL, url);
			
			Layer result = (Layer)getLayerFactory().createFromConfigSource(element, params);
			result.setValue(AVKeyMore.CONTEXT_URL, url);
			result.setEnabled(true);
			
			return (Layer)result;
		}
		catch (Throwable e)
		{
			ExceptionLogger.logException(e);
			return null;
		}
	}

}
