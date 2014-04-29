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
package au.gov.ga.worldwind.animator.application.effects.edge;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getEdgeDetectionNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffect;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffectBase;
import au.gov.ga.worldwind.common.effects.edge.EdgeDetectionEffect;

/**
 * Example {@link AnimatableEffect} that convolves the input with a kernel matrix,
 * producing different filter effects like blurring, edge detection, sharpening,
 * embossing, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EdgeDetectionAnimatableEffect extends AnimatableEffectBase<EdgeDetectionEffect>
{
	public EdgeDetectionAnimatableEffect(String name, Animation animation)
	{
		super(name, animation, new EdgeDetectionEffect());
	}

	protected EdgeDetectionAnimatableEffect()
	{
		super(new EdgeDetectionEffect());
	}

	@Override
	public AnimatableEffect createWithAnimation(Animation animation)
	{
		return new EdgeDetectionAnimatableEffect(null, animation);
	}

	@Override
	public String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getEdgeDetectionEffectElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		return new EdgeDetectionAnimatableEffect(name, animation);
	}

	@Override
	public String getDefaultName()
	{
		return getMessageOrDefault(getEdgeDetectionNameKey(), "Edge Detection");
	}
}
