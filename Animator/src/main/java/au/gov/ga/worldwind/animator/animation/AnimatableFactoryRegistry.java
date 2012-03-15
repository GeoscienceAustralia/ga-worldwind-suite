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
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.avlist.AVList;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * {@link Animatable} factory registry singleton which calls multiple
 * {@link AnimatableFactory}s that instanciate animatables from XML (calls them
 * in order until one returns a non-null {@link Animatable}).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum AnimatableFactoryRegistry implements AnimatableFactory
{
	instance;

	static
	{
		instance.registerFactory(new DefaultAnimatableFactory());
	}

	private final List<AnimatableFactory> factories = new ArrayList<AnimatableFactory>();

	/**
	 * Register an {@link AnimatableFactory} factory.
	 * 
	 * @param factory
	 *            Factory to register
	 */
	public void registerFactory(AnimatableFactory factory)
	{
		factories.add(factory);
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version is required");
		Validate.notNull(context, "A context is required");

		for (AnimatableFactory factory : factories)
		{
			Animatable animatable = factory.fromXml(element, version, context);
			if (animatable != null)
			{
				return animatable;
			}
		}

		return null;
	}
}
