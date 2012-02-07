package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

/**
 * A interface for classes that can load an animation {@link Layer} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 *
 */
public interface AnimationLayerLoader
{
	
	/**
	 * Load the layer identified by the provided identifier.
	 * 
	 * @param identifier The identifier that specifies the layer to open
	 * 
	 * @return The layer identified by the provided identifier, or <code>null</code> if an error occurred during layer load
	 */
	public Layer loadLayer(LayerIdentifier identifier);
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL.
	 */
	public Layer loadLayer(String url);
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL, or <code>null</code> if an error occurred during layer loading.
	 */
	public Layer loadLayer(URL url);

}
