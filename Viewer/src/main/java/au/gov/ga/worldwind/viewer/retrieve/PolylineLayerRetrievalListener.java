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
package au.gov.ga.worldwind.viewer.retrieve;

import gov.nasa.worldwind.retrieve.Retriever;
import au.gov.ga.worldwind.viewer.settings.Settings;

/**
 * {@link au.gov.ga.worldwind.common.retrieve.PolylineLayerRetrievalListener}
 * extension that only shows the polylines if the Viewer setting is enabled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PolylineLayerRetrievalListener extends au.gov.ga.worldwind.common.retrieve.PolylineLayerRetrievalListener
{
	@Override
	public void beforeRetrieve(Retriever retriever)
	{
		if (Settings.get().isShowDownloads())
		{
			super.beforeRetrieve(retriever);
		}
	}
}
