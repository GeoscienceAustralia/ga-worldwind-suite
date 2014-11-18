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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.camera.CameraImpl;
import au.gov.ga.worldwind.animator.animation.camera.HeadImpl;
import au.gov.ga.worldwind.animator.animation.camera.StereoCameraImpl;
import au.gov.ga.worldwind.animator.animation.elevation.DefaultAnimatableElevation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.layer.DefaultAnimatableLayer;
import au.gov.ga.worldwind.animator.animation.sun.SunPositionAnimatableImpl;

/**
 * A default implementation of AnimatableFactory that contains the Animatables
 * reponsible for instanciating built-in Animatables (such as the camera and
 * animatable layers).
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultAnimatableFactory implements AnimatableFactory
{
	/**
	 * A map of element name -> instance for use as factories in creating
	 * animatables
	 */
	private Map<String, Animatable> factoryMap = new HashMap<String, Animatable>();

	public DefaultAnimatableFactory()
	{
		// Add additional Animatables here as they are created
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getCameraElementName(),
				AnimatableInstanciator.instantiate(CameraImpl.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getStereoCameraElementName(),
				AnimatableInstanciator.instantiate(StereoCameraImpl.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getAnimatableLayerElementName(),
				AnimatableInstanciator.instantiate(DefaultAnimatableLayer.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getAnimatableElevationElementName(),
				AnimatableInstanciator.instantiate(DefaultAnimatableElevation.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getSunPositionElementName(),
				AnimatableInstanciator.instantiate(SunPositionAnimatableImpl.class));
		factoryMap.put(AnimationFileVersion.VERSION020.getConstants().getHeadElementName(),
				AnimatableInstanciator.instantiate(HeadImpl.class));
	}

	@Override
	public Animatable fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Animatable animatable = factoryMap.get(element.getNodeName());
		if (animatable == null)
		{
			return null;
		}
		return animatable.fromXml(element, version, context);
	}
}
