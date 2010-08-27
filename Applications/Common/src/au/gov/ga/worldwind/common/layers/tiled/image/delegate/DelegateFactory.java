package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.gov.ga.worldwind.common.layers.tiled.image.delegate.colortoalpha.ColorToAlphaTransformerDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.nearestneighbor.NearestNeighborTextureTileFactoryDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.transparentcolor.TransparentColorTransformerDelegate;

/**
 * Factory which creates delegates from a string definition.
 * 
 * @author Michael de Hoog
 */
public class DelegateFactory
{
	private static Set<Class<? extends Delegate>> delegateClasses =
			new HashSet<Class<? extends Delegate>>();
	private static List<Delegate> delegates = new ArrayList<Delegate>();

	/**
	 * Static constructor which registers all the default delegates. Custom
	 * delegates can be added using the registerDelegate() function.
	 */
	static
	{
		registerDelegate(URLRequesterDelegate.class);
		registerDelegate(LocalRequesterDelegate.class);

		registerDelegate(HttpRetrieverFactoryDelegate.class);
		registerDelegate(PassThroughZipRetrieverFactoryDelegate.class);

		registerDelegate(TextureTileFactoryDelegate.class);
		registerDelegate(NearestNeighborTextureTileFactoryDelegate.class);

		registerDelegate(MaskImageReaderDelegate.class);

		registerDelegate(ColorToAlphaTransformerDelegate.class);
		registerDelegate(TransparentColorTransformerDelegate.class);
	}

	/**
	 * Register a delegate class.
	 * 
	 * @param delegateClass
	 *            Class to register
	 */
	public static void registerDelegate(Class<? extends Delegate> delegateClass)
	{
		if (!delegateClasses.contains(delegateClass))
		{
			delegateClasses.add(delegateClass);
			try
			{
				Constructor<? extends Delegate> c = delegateClass.getDeclaredConstructor();
				c.setAccessible(true);
				delegates.add(c.newInstance());
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Error instanciating delegate", e);
			}
		}
	}

	/**
	 * Create a new delgate from a string definition.
	 * 
	 * @param definition
	 * @return New delegate corresponding to {@code definition}
	 */
	public static Delegate createDelegate(String definition)
	{
		for (Delegate delegate : delegates)
		{
			Delegate d = delegate.fromDefinition(definition);
			if (d != null)
				return d;
		}
		throw new IllegalArgumentException("Don't know how to create delegate from definition: "
				+ definition);
	}
}
