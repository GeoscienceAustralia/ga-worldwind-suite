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

public enum EffectRegistry implements AnimatableFactory
{
	instance;

	private final Map<String, Class<? extends Effect>> elementToEffectMap =
			new HashMap<String, Class<? extends Effect>>();
	private final SortedSet<Class<? extends Effect>> effects = new TreeSet<Class<? extends Effect>>(
			new EffectComparator());
	
	private EffectRegistry()
	{
		AnimatableFactoryRegistry.instance.registerFactory(this);
	}

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

	public SortedSet<Class<? extends Effect>> getEffects()
	{
		return Collections.unmodifiableSortedSet(effects);
	}

	public void registerEffect(Class<? extends Effect> effect)
	{
		Effect effectInstance = AnimatableInstanciator.instantiate(effect);
		effects.add(effect);
		elementToEffectMap
				.put(effectInstance.getXmlElementName(AnimationFileVersion.VERSION020.getConstants()), effect);
	}

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
