package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.avlist.AVList;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A factory class for creating {@link LayerParameter}s from XML elements
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class LayerParameterFactory
{
	
	/** A map of element name -> instance for use as factories in creating {@link LayerParameter}s */
	private static Map<String, LayerParameter> factoryMap = new HashMap<String, LayerParameter>();
	static
	{
		// Add additional LayerParameters here as they are created
		factoryMap.put(LayerParameter.Type.OPACITY.name().toLowerCase(), instantiate(LayerOpacityParameter.class));
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
