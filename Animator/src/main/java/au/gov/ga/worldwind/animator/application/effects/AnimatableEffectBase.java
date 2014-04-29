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

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.common.effects.Effect;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Abstract base implementation of the {@link AnimatableEffect} interface. Most
 * {@link AnimatableEffect} implementations should use this as their base class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AnimatableEffectBase<E extends Effect> extends AnimatableBase implements AnimatableEffect
{
	/**
	 * The animatable parameters used by this effect.
	 */
	protected final List<Parameter> parameters = new ArrayList<Parameter>();

	/**
	 * Actual effect implementation
	 */
	protected final E effect;

	public AnimatableEffectBase(String name, Animation animation, E effect)
	{
		super(name, animation);
		this.effect = effect;
	}

	protected AnimatableEffectBase(E effect)
	{
		super();
		this.effect = effect;
		setName(getDefaultName());
	}

	public E getEffect()
	{
		return effect;
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public void addParameter(EffectParameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		Validate.isTrue(this.equals(parameter.getEffect()), "Parameter is not linked to the correct layer. Expected '"
				+ this + "'.");
		parameters.add(parameter);

		parameter.addChangeListener(this);
	}

	@Override
	protected void doApply()
	{
		for (Parameter parameter : parameters)
		{
			Validate.isTrue(parameter instanceof EffectParameter, "Incorrect Parameter type"); //should never occur
			((EffectParameter) parameter).apply();
		}
	}

	@Override
	public final void bindFrameBuffer(DrawContext dc, Dimension dimensions)
	{
		effect.bindFrameBuffer(dc, dimensions);
	}

	@Override
	public final void unbindFrameBuffer(DrawContext dc, Dimension dimensions)
	{
		effect.unbindFrameBuffer(dc, dimensions);
	}

	@Override
	public final void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions)
	{
		effect.drawFrameBufferWithEffect(dc, dimensions);
	}

	@Override
	public final void releaseResources(DrawContext dc)
	{
		effect.releaseResources(dc);
	}
}
