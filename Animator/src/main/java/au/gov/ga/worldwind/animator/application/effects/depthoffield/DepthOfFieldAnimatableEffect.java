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
package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDepthOfFieldNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffect;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffectBase;
import au.gov.ga.worldwind.common.effects.depthoffield.DepthOfFieldEffect;

/**
 * {@link AnimatableEffect} implementation that provides a depth-of-field effect, with
 * animatable near, far, and focus length parameters. The parameter values
 * default to the near clipping plane, far clipping plane, and animation camera
 * look-at point respectively.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldAnimatableEffect extends AnimatableEffectBase<DepthOfFieldEffect>
{
	private Parameter focusParameter;
	private Parameter nearParameter;
	private Parameter farParameter;

	public DepthOfFieldAnimatableEffect(String name, Animation animation)
	{
		super(name, animation, new DepthOfFieldEffect());
		initializeParameters();
	}

	@SuppressWarnings("unused")
	private DepthOfFieldAnimatableEffect()
	{
		super(new DepthOfFieldEffect());
	}

	@Override
	public String getDefaultName()
	{
		return getMessageOrDefault(getDepthOfFieldNameKey(), "Depth of Field");
	}

	/**
	 * Initialize the parameters for this effect (create them if they don't
	 * exist). The parameters are unarmed and disabled by default.
	 */
	protected void initializeParameters()
	{
		if (focusParameter == null || nearParameter == null || farParameter == null)
		{
			focusParameter = new DepthOfFieldFocusParameter(null, animation, this);
			nearParameter = new DepthOfFieldNearParameter(null, animation, this);
			farParameter = new DepthOfFieldFarParameter(null, animation, this);
		}

		focusParameter.setArmed(false);
		focusParameter.setEnabled(false);
		nearParameter.setArmed(false);
		nearParameter.setEnabled(false);
		farParameter.setArmed(false);
		farParameter.setEnabled(false);

		parameters.clear();
		parameters.add(focusParameter);
		parameters.add(nearParameter);
		parameters.add(farParameter);
	}

	@Override
	public String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getDepthOfFieldEffectElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		XPath xpath = WWXML.makeXPath();

		DepthOfFieldAnimatableEffect effect = new DepthOfFieldAnimatableEffect(name, animation);
		context.setValue(constants.getCurrentEffectKey(), effect);

		effect.focusParameter =
				new DepthOfFieldFocusParameter()
						.fromXml(WWXML.getElement(element, constants.getDepthOfFieldFocusElementName(), xpath),
								version, context);
		effect.nearParameter =
				new DepthOfFieldNearParameter().fromXml(
						WWXML.getElement(element, constants.getDepthOfFieldNearElementName(), xpath), version, context);
		effect.farParameter =
				new DepthOfFieldFarParameter().fromXml(
						WWXML.getElement(element, constants.getDepthOfFieldFarElementName(), xpath), version, context);

		effect.initializeParameters();

		return effect;
	}

	@Override
	public AnimatableEffect createWithAnimation(Animation animation)
	{
		return new DepthOfFieldAnimatableEffect(null, animation);
	}
}
