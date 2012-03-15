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
package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getFogFarParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;

import org.w3c.dom.Element;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A layer parameter controlling the far factor of a {@link FogLayer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@EditableParameter
public class FogFarFactorParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 1L;

	public FogFarFactorParameter(Animation animation, FogLayer layer)
	{
		this(null, animation, layer);
	}

	public FogFarFactorParameter(String name, Animation animation, FogLayer layer)
	{
		super(name, animation, layer);
		setDefaultValue(layer.getFarFactor());
	}

	@SuppressWarnings("unused")
	private FogFarFactorParameter()
	{
	}
	
	@Override
	protected String getDefaultName()
	{
		return getMessage(getFogFarParameterNameKey());
	}

	@Override
	public Type getType()
	{
		return Type.FAR;
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return ParameterValueFactory.createParameterValue(this, ((FogLayer) getLayer()).getFarFactor(),
				animation.getCurrentFrame());
	}

	@Override
	protected void doApplyValue(double value)
	{
		((FogLayer) getLayer()).setFarFactor((float) value);
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		Layer parameterLayer = (Layer) context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer,
				"No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		Validate.isTrue(parameterLayer instanceof FogLayer, "Layer found in context is incorrect type: '"
				+ parameterLayer.getClass().getCanonicalName() + "'");

		return new FogFarFactorParameter(name, animation, (FogLayer) parameterLayer);
	}
}
