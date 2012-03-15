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
package au.gov.ga.worldwind.animator.layers.accessible;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;

/**
 * {@link BasicTiledImageLayer} subclass that provides access to its internal
 * filelock object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AccessibleBasicTiledImageLayer extends BasicTiledImageLayer
{
	private final FileLockAccessor fileLockAccessor = new FileLockAccessor(this);

	public AccessibleBasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public AccessibleBasicTiledImageLayer(AVList params)
	{
		super(params);
	}

	public AccessibleBasicTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	protected Object getFileLock()
	{
		return fileLockAccessor.getFileLock();
	}

	protected static class FileLockAccessor extends DownloadPostProcessor
	{
		public FileLockAccessor(BasicTiledImageLayer layer)
		{
			super(null, layer);
		}

		@Override
		public Object getFileLock()
		{
			return super.getFileLock();
		}
	}
}
