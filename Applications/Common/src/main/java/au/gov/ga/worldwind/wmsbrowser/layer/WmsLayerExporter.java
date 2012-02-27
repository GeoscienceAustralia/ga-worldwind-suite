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
package au.gov.ga.worldwind.wmsbrowser.layer;

import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.io.File;
import java.io.OutputStream;

/**
 * Allows WMS layers to be exported to an XML layer definition file for 
 * later re-use within the WorldWind system.
 *
 */
public interface WmsLayerExporter
{

	/**
	 * Export the given WMS layer to the provided target output stream
	 */
	void exportLayer(OutputStream targetStream, WMSLayerInfo layerInfo);
	
	/**
	 * Export the given WMS layer to a file with the provided file name
	 */
	void exportLayer(String fileName, WMSLayerInfo layerInfo);
	
	/**
	 * Export the given WMS layer to the provided file
	 */
	void exportLayer(File file, WMSLayerInfo layerInfo);
	
}
