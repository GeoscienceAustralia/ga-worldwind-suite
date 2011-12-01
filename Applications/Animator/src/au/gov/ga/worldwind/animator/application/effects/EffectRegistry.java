package au.gov.ga.worldwind.animator.application.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class EffectRegistry
{
	private static SortedSet<Class<? extends Effect>> effects = new TreeSet<Class<? extends Effect>>(
			new EffectComparator());

	private static class EffectComparator implements Comparator<Class<? extends Effect>>
	{
		@Override
		public int compare(Class<? extends Effect> o1, Class<? extends Effect> o2)
		{
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			return getEffectName(o1).compareTo(getEffectName(o2));
		}
	}

	public static SortedSet<Class<? extends Effect>> getEffects()
	{
		return Collections.unmodifiableSortedSet(effects);
	}

	public static void registerEffect(Class<? extends Effect> effect)
	{
		try
		{
			effect.getDeclaredConstructor();
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Effect class '" + effect + "' does not have a default constructor");
		}
		effects.add(effect);
	}

	public static String getEffectName(Class<? extends Effect> effect)
	{
		//first try a static class method named "getEffectName()"
		try
		{
			Method method = effect.getMethod("getEffectName");
			if (Modifier.isStatic(method.getModifiers()))
			{
				String s = (String) method.invoke(null);
				if (s != null)
				{
					return s;
				}
			}
		}
		catch (Exception e)
		{
		}

		//next try the "getName()" method of a new instance
		try
		{
			Constructor<? extends Effect> constructor = effect.getDeclaredConstructor();
			constructor.setAccessible(true);
			Effect effectInstance = constructor.newInstance();
			String s = effectInstance.getName();
			if (s != null)
			{
				return s;
			}
		}
		catch (Exception e)
		{
		}

		//finally just return the effect's class name
		return effect.getCanonicalName();
	}
}
