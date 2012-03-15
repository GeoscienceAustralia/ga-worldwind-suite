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
package au.gov.ga.worldwind.wmsbrowser.search;

import java.net.URL;

import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * An interface for search results yielded by a {@link WmsServerSearchService}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface WmsServerSearchResult
{
	
	/**
	 * @return The WMS server this result relates to. May or may not be returned in a 'loaded' state.
	 */
	WmsServer getWmsServer();
	
	/**
	 * @return The title of the server this result is for
	 */
	String getTitle();
	
	/**
	 * @return The abstract of the server this result is for
	 */
	String getAbstract();
	
	/**
	 * @return The capabilities URL for this result 
	 */
	URL getCapabilitiesUrl();
	
	/**
	 * @return The publisher for this result
	 */
	String getPublisher();
}
