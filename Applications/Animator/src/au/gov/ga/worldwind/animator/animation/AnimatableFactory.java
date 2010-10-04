package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.avlist.AVList;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.camera.CameraImpl;
import au.gov.ga.worldwind.animator.animation.elevation.DefaultAnimatableElevation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.layer.DefaultAnimatableLayer;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A factory class for de-serialising {@link Animatable} objects from XML
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatableFactory
{

	/** A map of element name -> instance for use as factories in creating animatables */
	private static Map<String, Animatable> factoryMap = new HashMap<String, Animatable>();
	static
	{
		// Add additional Animatables here as they are created
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getCameraElementName(), instantiate(CameraImpl.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getAnimatableLayerName(), instantiate(DefaultAnimatableLayer.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getAnimatableElevationName(), instantiate(DefaultAnimatableElevation.class));
	}
	
	/**
	 * Create an instance of the {@link Animatable} that corresponds to the provided XML element in the 
	 * given file version.
	 * 
	 * @param element The element to de-serialise from
	 * @param version The version the provided element is in
	 * @param context The context needed to de-serialise the object.
	 * 
	 * @return an instance of the {@link Animatable} that corresponds to the provided XML element, or <code>null</code>
	 * if one cannot be found
	 */
	public static Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version is required");
		Validate.notNull(context, "A context is required");
		
		Animatable animatableFactory = factoryMap.get(element.getNodeName());
		
		if (animatableFactory == null)
		{
			return null;
		}
		
		return animatableFactory.fromXml(element, version, context);
	}

	/**
	 * Instantiate the provided class using the default constructor.
	 * 
	 * @param clazz The class to instantiate
	 * 
	 * @return The instantiated object
	 */
	private static Animatable instantiate(Class<? extends Animatable> clazz)
	{
		Validate.notNull(clazz, "A class must be provided");
		
		try
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return (Animatable)constructor.newInstance();
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
