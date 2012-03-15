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
package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.URL;

/**
 * A service interface to services that can retrieve WMS capabilities from a URL.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface WmsCapabilitiesService
{
	/**
	 * @return the WMS Capabilites for the provided URL, or <code>null</code> if the URL
	 * is not a valid WMS capabilities URL 
	 */
	WMSCapabilities retrieveCapabilities(URL url) throws Exception;
}
