package au.gov.ga.worldwind.animator.animation.layer;

import static au.gov.ga.worldwind.animator.util.Util.isBlank;

import gov.nasa.worldwind.layers.Layer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

import au.gov.ga.worldwind.animator.util.Util;
import au.gov.ga.worldwind.common.util.AVKeyMore;

/**
 * A factory class for obtaining layer identifiers
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class LayerIdentifierFactory
{
	private static final String LAYER_IDENTIFIER_KEY_PREFIX = "layer.";

	/**
	 * Read layer identifiers from a property bundle with the given name.
	 * <p/>
	 * layer identifiers should have the form <code>layer.[index].[Layer_Name] = [Location]</code> where '_' characters will be replaced by spaces ' '.
	 * 
	 * @param baseName The name of the property bundle to read from
	 * 
	 * @return The list of layer identifiers read from the property bundle
	 */
	public static List<LayerIdentifier> readFromPropertiesFile(String baseName)
	{
		ResourceBundle bundle = ResourceBundle.getBundle(baseName);
		
		TreeMap<Integer, LayerIdentifier> result = new TreeMap<Integer, LayerIdentifier>();
		
		Enumeration<String> bundleKeys = bundle.getKeys();
		while (bundleKeys.hasMoreElements())
		{
			String key = bundleKeys.nextElement();
			
			if (isLayerIdentifierKey(key))
			{
				Integer layerIndex = getLayerIndexFromKey(key);
				String layerName = getLayerNameFromKey(key);
				String layerLocation = bundle.getString(key);
				result.put(layerIndex, new LayerIdentifierImpl(layerName, layerLocation));
			}
		}
		
		return new ArrayList<LayerIdentifier>(result.values());
	}

	private static Integer getLayerIndexFromKey(String key)
	{
		int indexOfFirstSeparator = key.indexOf('.');
		int indexOfLastSeparator = key.indexOf('.', indexOfFirstSeparator+1);
		return Integer.parseInt(key.substring(indexOfFirstSeparator+1, indexOfLastSeparator));
	}

	private static String getLayerNameFromKey(String key)
	{
		int indexOfLastSeparator = key.indexOf('.', key.indexOf('.') + 1);
		return key.substring(indexOfLastSeparator + 1).replace('_', ' ');
	}

	private static boolean isLayerIdentifierKey(String key)
	{
		return !Util.isBlank(key) && key.startsWith(LAYER_IDENTIFIER_KEY_PREFIX);
	}

	/**
	 * Creates a new {@link LayerIdentifier} that identifies the provided layer.
	 * 
	 * @param layer The layer to create the identifier from
	 * 
	 * @return A layer identifier created from the provided layer
	 */
	public static LayerIdentifier createFromLayer(Layer layer)
	{
		if (layer == null)
		{
			return null;
		}
		String layerName = layer.getName();
		if (isBlank(layerName))
		{
			return null;
		}
		
		URL layerUrl = (URL)layer.getValue(AVKeyMore.CONTEXT_URL);
		if (layerUrl == null)
		{
			return null;
		}
		
		return new LayerIdentifierImpl(layerName, layerUrl.toExternalForm());
	}
	
}
