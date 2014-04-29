/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
 * Singleton with which {@link AnimatableEffect}s are registered. To be able to use an
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

	private final Map<String, Class<? extends AnimatableEffect>> elementToEffectMap =
			new HashMap<String, Class<? extends AnimatableEffect>>();
	private final SortedSet<Class<? extends AnimatableEffect>> effects = new TreeSet<Class<? extends AnimatableEffect>>(
			new EffectComparator());

	private EffectRegistry()
	{
		//register this as an AnimatableFactory, so that it can deserialize effect instances from XML
		AnimatableFactoryRegistry.instance.registerFactory(this);
	}

	/**
	 * Simple {@link Comparator} that sorts {@link AnimatableEffect}s by name.
	 */
	private class EffectComparator implements Comparator<Class<? extends AnimatableEffect>>
	{
		@Override
		public int compare(Class<? extends AnimatableEffect> o1, Class<? extends AnimatableEffect> o2)
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
	public SortedSet<Class<? extends AnimatableEffect>> getEffects()
	{
		return Collections.unmodifiableSortedSet(effects);
	}

	/**
	 * Register an effect
	 * 
	 * @param effect
	 *            Effect to register
	 */
	public void registerEffect(Class<? extends AnimatableEffect> effect)
	{
		AnimatableEffect effectInstance = AnimatableInstanciator.instantiate(effect);
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
	public String getEffectName(Class<? extends AnimatableEffect> effect)
	{
		AnimatableEffect effectInstance = AnimatableInstanciator.instantiate(effect);
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
		Class<? extends AnimatableEffect> effectClass = elementToEffectMap.get(element.getNodeName());
		if (effectClass == null)
		{
			return null;
		}
		return AnimatableInstanciator.instantiate(effectClass).fromXml(element, version, context);
	}
}
