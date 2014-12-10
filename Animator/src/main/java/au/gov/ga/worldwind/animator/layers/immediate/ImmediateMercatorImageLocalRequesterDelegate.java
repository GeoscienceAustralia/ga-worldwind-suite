/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.worldwind.common.layers.mercator.delegate.DelegatorMercatorTextureTile;
import au.gov.ga.worldwind.common.layers.mercator.delegate.MercatorImageLocalRequesterDelegate;

/**
 * A local requester delegate that will immediately perform texture load from a
 * local tileset if {@link ImmediateMode#isImmediate()} returns
 * <code>true</code>.
 * <p/>
 * Used to ensure hi-res versions of layers are available at render time for an
 * animation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateMercatorImageLocalRequesterDelegate extends MercatorImageLocalRequesterDelegate
{
	@Override
	public Runnable createRequestTask(DelegatorMercatorTextureTile tile,
			IDelegatorLayer<DelegatorMercatorTextureTile> layer)
	{
		Runnable task = super.createRequestTask(tile, layer);
		if (!ImmediateMode.isImmediate())
		{
			return task;
		}

		//run immediately to load texture
		task.run();
		return null;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new ImmediateMercatorImageLocalRequesterDelegate();
		return null;
	}
}
