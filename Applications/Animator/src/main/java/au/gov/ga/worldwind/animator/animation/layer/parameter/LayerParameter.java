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
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface for parameters that control properties of an {@link Layer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface LayerParameter extends Parameter
{
	/**
	 * @return The layer this parameter is associated with
	 */
	Layer getLayer();

	/**
	 * @return The type of this layer parameter
	 */
	Type getType();

	/**
	 * Apply this parameter's state to it's associated {@link Layer} for the
	 * current frame
	 */
	void apply();

	/**
	 * An enumeration of the valid types of layer parameters.
	 * <p/>
	 * Used to help identify which attribute of the associated {@link Layer} is
	 * being controlled by the {@link LayerParameter}.
	 * 
	 */
	public static enum Type
	{
		OPACITY, NEAR, FAR, OUTLINE_OPACITY;
	}
}
