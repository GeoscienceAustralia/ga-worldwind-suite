package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegateFactory;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.URLRequesterDelegate;
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
	static
	{
		DelegateFactory.registerDelegate(ImmediateRequesterDelegate.class);
		DelegateFactory.registerReplacementClass(URLRequesterDelegate.class, ImmediateRequesterDelegate.class);
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
	 * @return The layer identified by the provided URL.
	 */
	public static Layer loadLayer(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		Element element = XMLUtil.getElementFromSource(url);
		
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, url);
		
		LayerFactory factory = new LayerFactory();
		Layer result = (Layer)factory.createFromConfigSource(element, params);
		
		return (Layer)result;
	}

}
