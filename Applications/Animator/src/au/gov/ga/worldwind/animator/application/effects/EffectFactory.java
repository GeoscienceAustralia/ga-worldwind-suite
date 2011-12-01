package au.gov.ga.worldwind.animator.application.effects;

import java.lang.reflect.Constructor;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * Simple helper class for instanciating new {@link Effect} instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EffectFactory
{
	/**
	 * Create a new effect associated with the given {@link Animation}
	 * 
	 * @param c
	 *            Effect class to instanciate
	 * @param animation
	 *            Animation to associate Effect with
	 * @return New Effect instance
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Effect> E createEffect(Class<E> c, Animation animation)
	{
		try
		{
			Constructor<E> constructor = c.getDeclaredConstructor();
			constructor.setAccessible(true);
			Effect effectInstance = constructor.newInstance();
			return (E) effectInstance.createWithAnimation(animation);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not instanciate new instance of Effect subclass '"
					+ c.getCanonicalName() + "': " + e.getLocalizedMessage());
		}
	}
}
