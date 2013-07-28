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
package au.gov.ga.worldwind.viewer.panels.layers;

import javax.swing.event.TreeModelListener;

/**
 * Listener interface for listening to {@link LayerTreeModel} change events.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface LayerTreeModelListener extends TreeModelListener
{
	/**
	 * Called when a layer node is enabled or disabled.
	 * 
	 * @param layer
	 *            Layer enabled/disabled
	 * @param enabled
	 *            Enabled state
	 */
	void enabledChanged(ILayerNode layer, boolean enabled);

	/**
	 * Called when a layer node's opacity changes.
	 * 
	 * @param layer
	 *            Changed layer
	 * @param opacity
	 *            New opacity
	 */
	void opacityChanged(ILayerNode layer, double opacity);
}
