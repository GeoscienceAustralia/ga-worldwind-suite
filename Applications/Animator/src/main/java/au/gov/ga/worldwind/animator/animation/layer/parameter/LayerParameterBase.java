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

import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A base implementation of the {@link LayerParameter} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public abstract class LayerParameterBase extends ParameterBase implements LayerParameter
{
	private static final long serialVersionUID = 20100907L;
	
	/** The layer this parameter is associated with */
	private Layer layer;
	
	/**
	 * @param name The name of this parameter
	 * @param animation The animation associated with this parameter
	 * @param layer The layer associated with this parameter
	 */
	public LayerParameterBase(String name, Animation animation, Layer layer)
	{
		super(name, animation);
		Validate.notNull(layer, "A layer is required");
		this.layer = layer;
	}
	
	/**
	 * Constructor used for deserialization. Not for general consumption.
	 */
	protected LayerParameterBase()
	{
		super();
	}

	@Override
	public Layer getLayer()
	{
		return layer;
	}

	protected void setLayer(Layer layer)
	{
		this.layer = layer;
	}
	
	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return getType().name().toLowerCase();
	}
	
	@Override
	public void apply()
	{
		int frame = animation.getCurrentFrame();
		ParameterValue value = getValueAtFrame(frame);
		applyValueIfEnabled(value.getValue(), frame);
	}
}
