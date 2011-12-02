package au.gov.ga.worldwind.animator.application.effects;

import gov.nasa.worldwind.avlist.AVList;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.AnimatableFactory;
import au.gov.ga.worldwind.animator.animation.AnimatableFactoryRegistry;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;

/**
 * Singleton with which {@link Effect}s are registered. To be able to use an
 * effect in the Animator, it must first be registered with this class. All
 * registered effects are displayed by the {@link EffectDialog}. This class is
 * also used for effect deserialization.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum EffectRegistry implements AnimatableFactory
{
	/**
	 * The {@link EffectRegistry} singleton
	 */
	instance;

	private final Map<String, Class<? extends Effect>> elementToEffectMap =
			new HashMap<String, Class<? extends Effect>>();
	private final SortedSet<Class<? extends Effect>> effects = new TreeSet<Class<? extends Effect>>(
			new EffectComparator());

	private EffectRegistry()
	{
		//register this as an AnimatableFactory, so that it can deserialize effect instances from XML
		AnimatableFactoryRegistry.instance.registerFactory(this);
	}

	/**
	 * Simple {@link Comparator} that sorts {@link Effect}s by name.
	 */
	private class EffectComparator implements Comparator<Class<? extends Effect>>
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

	/**
	 * @return The list of effects registered, sorted by name.
	 */
	public SortedSet<Class<? extends Effect>> getEffects()
	{
		return Collections.unmodifiableSortedSet(effects);
	}

	/**
	 * Register an effect
	 * 
	 * @param effect
	 *            Effect to register
	 */
	public void registerEffect(Class<? extends Effect> effect)
	{
		Effect effectInstance = AnimatableInstanciator.instantiate(effect);
		effects.add(effect);
		elementToEffectMap
				.put(effectInstance.getXmlElementName(AnimationFileVersion.VERSION020.getConstants()), effect);
	}

	/**
	 * Helper method to get an effect's default name from it's class
	 * 
	 * @param effect
	 *            Effect class to get the default name for
	 * @return Default name of the given effect
	 */
	public String getEffectName(Class<? extends Effect> effect)
	{
		Effect effectInstance = AnimatableInstanciator.instantiate(effect);
		String name = effectInstance.getName();
		if (name == null)
		{
			name = effectInstance.getDefaultName();
		}
		if (name == null)
		{
			name = effect.getCanonicalName();
		}
		return name;
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Class<? extends Effect> effectClass = elementToEffectMap.get(element.getNodeName());
		if (effectClass == null)
		{
			return null;
		}
		return AnimatableInstanciator.instantiate(effectClass).fromXml(element, version, context);
	}
}
