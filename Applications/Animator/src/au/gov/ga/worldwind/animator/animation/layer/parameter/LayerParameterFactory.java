package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A factory class for creating {@link LayerParameter}s from XML elements
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
@SuppressWarnings("unchecked")
public class LayerParameterFactory
{
	
	/** A map of element name -> instance for use as factories in creating {@link LayerParameter}s */
	private static Map<String, LayerParameter> factoryMap = new HashMap<String, LayerParameter>();
	static
	{
		// Add additional LayerParameters here as they are created
		factoryMap.put(LayerParameter.Type.OPACITY.name().toLowerCase(), instantiate(LayerOpacityParameter.class));
	}
	
	private static Map<Class<? extends LayerParameter>, Class<Layer>[]> parameterTypeMap = new HashMap<Class<? extends LayerParameter>, Class<Layer>[]>();
	static
	{
		// Add additional parameter types here as they are created
		parameterTypeMap.put(LayerOpacityParameter.class, new Class[]{TiledImageLayer.class, SkyGradientLayer.class, StarsLayer.class});
	}
	
	/**
	 * Create an instance of the {@link LayerParameter} that corresponds to the provided XML element in the 
	 * given file version.
	 * 
	 * @param element The element to de-serialise from
	 * @param version The version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return an instance of the {@link LayerParameter} that corresponds to the provided XML element, or <code>null</code>
	 * if one cannot be found
	 */
	public static LayerParameter fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version is required");
		Validate.notNull(context, "A context is required");
		
		LayerParameter layerFactory = factoryMap.get(element.getNodeName());
		
		if (layerFactory == null)
		{
			return null;
		}
		
		return (LayerParameter)layerFactory.fromXml(element, version, context);
	}

	/**
	 * @return The list of {@link LayerParameter}s that can be applied to the provided layer in the provided animation
	 */
	public static LayerParameter[] createDefaultParametersForLayer(Animation animation, Layer targetLayer)
	{
		if (animation == null || targetLayer == null)
		{
			return new LayerParameter[0];
		}
		
		List<LayerParameter> result = new ArrayList<LayerParameter>();
		if (supportsOpacityParameter(targetLayer))
		{
			result.add(createOpacityParameter(animation, targetLayer));
		}
		
		return result.toArray(new LayerParameter[0]);
	}
	
	private static boolean supportsOpacityParameter(Layer targetLayer)
	{
		return layerTypeInList(targetLayer, parameterTypeMap.get(LayerOpacityParameter.class));
	}

	private static boolean layerTypeInList(Layer targetLayer, Class<Layer>[] layerTypes)
	{
		for (Class<Layer> layerType : layerTypes)
		{
			if (layerType.isAssignableFrom(targetLayer.getClass()))
			{
				return true;
			}
		}
		return false;
	}

	private static LayerOpacityParameter createOpacityParameter(Animation animation, Layer targetLayer)
	{
		return new LayerOpacityParameter(animation, targetLayer);
	}
	
	/**
	 * Instantiate the provided class using the default constructor.
	 * 
	 * @param clazz The class to instantiate
	 * 
	 * @return The instantiated object
	 */
	private static LayerParameter instantiate(Class<? extends LayerParameter> clazz)
	{
		Validate.notNull(clazz, "A class must be provided");
		
		try
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return (LayerParameter)constructor.newInstance();
		} 
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " does not declare a no-arg constructor.");
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Exception while instantiating class " + clazz.getSimpleName() + ".", e.getCause());
		}
	}

}
