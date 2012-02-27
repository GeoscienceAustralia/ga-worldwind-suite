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
package au.gov.ga.worldwind.animator.layers;

import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

/**
 * A interface for classes that can load an animation {@link Layer} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 *
 */
public interface AnimationLayerLoader
{
	
	/**
	 * Load the layer identified by the provided identifier.
	 * 
	 * @param identifier The identifier that specifies the layer to open
	 * 
	 * @return The layer identified by the provided identifier, or <code>null</code> if an error occurred during layer load
	 */
	public Layer loadLayer(LayerIdentifier identifier);
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL.
	 */
	public Layer loadLayer(String url);
	
	/**
	 * Load the layer identified by the provided URL.
	 * 
	 * @param url The URL that identifies the layer to open
	 * 
	 * @return The layer identified by the provided URL, or <code>null</code> if an error occurred during layer loading.
	 */
	public Layer loadLayer(URL url);

}
