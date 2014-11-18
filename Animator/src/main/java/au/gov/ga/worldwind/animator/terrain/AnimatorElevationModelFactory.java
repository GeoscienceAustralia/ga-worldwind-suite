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
package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;

/**
 * {@link ElevationModelFactory} subclass that creates immediate* versions of
 * the non-compound elevation models.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnimatorElevationModelFactory extends ElevationModelFactory
{
	@Override
	protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
	{
		String serviceName = WWXML.getText(domElement, "Service/@serviceName");
		if ("FileTileService".equals(serviceName))
		{
			//only enable the immediate file elevation model; others request too many tiles from the server
			return new ImmediateFileElevationModel(domElement, params);
		}
		return super.createNonCompoundModel(domElement, params);
	}
}
