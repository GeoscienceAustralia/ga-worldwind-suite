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
import gov.nasa.worldwind.util.TileKey;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.common.terrain.FileElevationModel;

/**
 * {@link FileElevationModel} subclass that requests tiles immediately when
 * {@link ImmediateMode} is enabled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateFileElevationModel extends FileElevationModel
{
	private boolean requestingTile;

	public ImmediateFileElevationModel(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	protected ElevationTile getTileFromMemory(TileKey tileKey)
	{
		if (ImmediateMode.isImmediate() && !requestingTile)
		{
			requestingTile = true;
			requestTile(tileKey);
			requestingTile = false;
		}
		return super.getTileFromMemory(tileKey);
	}
}
