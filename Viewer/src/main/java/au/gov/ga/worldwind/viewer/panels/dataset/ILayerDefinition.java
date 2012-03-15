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
package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

/**
 * Interface representing a layer definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerDefinition extends IData
{
	/**
	 * @return URL pointing at the layer definition file.
	 */
	URL getLayerURL();

	/**
	 * @return Should this layer be added to the layers panel by default? This
	 *         is done when loading the Viewer for the first time (and the
	 *         layers persistance file does not yet exist).
	 */
	boolean isDefault();

	/**
	 * @return If this layer is added by default, should it be added in the
	 *         enabled state?
	 * @see ILayerDefinition#isDefault()
	 */
	boolean isEnabled();
}
