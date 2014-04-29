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

import java.lang.reflect.Constructor;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * Simple helper class for instanciating new {@link AnimatableEffect} instances.
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
	public static <E extends AnimatableEffect> E createEffect(Class<E> c, Animation animation)
	{
		try
		{
			Constructor<E> constructor = c.getDeclaredConstructor();
			constructor.setAccessible(true);
			AnimatableEffect effectInstance = constructor.newInstance();
			return (E) effectInstance.createWithAnimation(animation);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not instanciate new instance of Effect subclass '"
					+ c.getCanonicalName() + "': " + e.getLocalizedMessage());
		}
	}
}
