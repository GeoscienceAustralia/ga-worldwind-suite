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

import java.net.URL;

import au.gov.ga.worldwind.common.ui.lazytree.ILoadingNode;
import au.gov.ga.worldwind.common.util.Loader;
import au.gov.ga.worldwind.viewer.application.Application;

/**
 * Represents an layer node in the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerNode extends INode
{
	/**
	 * @return URL to the layer definition or file
	 */
	URL getLayerURL();

	/**
	 * @return URL pointing at this layer's legend
	 */
	URL getLegendURL();

	/**
	 * Set this layer's legend URL
	 * 
	 * @param legendURL
	 */
	void setLegendURL(URL legendURL);

	/**
	 * @return URL used to query the data at a particular latlon (eg WFS bbox
	 *         url)
	 */
	URL getQueryURL();

	/**
	 * Set the URL used to query the data at a particular latlon. A '#bbox#'
	 * placeholder in the query URL is replaced by small bounding box around the
	 * selected point. A '#latlon#' placeholder is replaced by the lat,lon of
	 * the selected point, '#lonlat#' is replaced by the lon,lat.
	 * 
	 * @param queryURL
	 * @see Application#initDataQuery(URL)
	 */
	void setQueryURL(URL queryURL);

	/**
	 * @return Is this layer enabled?
	 */
	boolean isEnabled();

	/**
	 * Enable/disable this layer. Should only be called by the
	 * {@link LayerTreeModel}.
	 * 
	 * @param enabled
	 */
	void setEnabled(boolean enabled);

	/**
	 * @return The opacity of this layer.
	 */
	double getOpacity();

	/**
	 * Set the opacity of this layer. Should only be called by the
	 * {@link LayerTreeModel}.
	 * 
	 * @param opacity
	 */
	void setOpacity(double opacity);

	/**
	 * @return Did this layer generate an error when loading?
	 */
	boolean hasError();

	/**
	 * @return This layer node's loading error.
	 */
	Exception getError();

	/**
	 * Set this layer node's error.
	 * 
	 * @param error
	 */
	void setError(Exception error);

	/**
	 * @return Is this layer node currently loading its layer?
	 */
	boolean isLayerLoading();

	/**
	 * Mark this layer node as loading its layer.
	 * 
	 * @param layerLoading
	 */
	void setLayerLoading(boolean layerLoading);

	/**
	 * @return Is the layer associated with this node currently loading its
	 *         data?
	 */
	boolean isLayerDataLoading();

	/**
	 * Mark this layer node as loading its layer's data.
	 * 
	 * @param layerDataLoading
	 */
	void setLayerDataLoading(boolean layerDataLoading);

	/**
	 * Assign a {@link Loader} to this layer node. If this loader is loading,
	 * the {@link ILoadingNode#isLoading()} function should return true.
	 * 
	 * @param loader
	 */
	void setLoader(Loader loader);

	/**
	 * @return Expiry time of this layer node. Used to override the layer's
	 *         expiry time.
	 */
	Long getExpiryTime();

	/**
	 * Set this node's expiry time. Overrides the layer's expiry time (if
	 * earlier). Used to refresh the layer's data.
	 * 
	 * @param expiryTime
	 */
	void setExpiryTime(Long expiryTime);
}
