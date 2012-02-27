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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * Default implementation of the {@link WmsLayerExporter} interface
 */
public class WmsLayerExporterImpl implements WmsLayerExporter
{

	private static Logger logger = Logging.logger();
	
	@Override
	public void exportLayer(String fileName, WMSLayerInfo layerInfo)
	{
		Validate.notBlank(fileName, "A file name is required");
		Validate.notNull(layerInfo, "Layer information is required");
		
		try
		{
			File outputFile = new File(fileName);
			FileOutputStream targetStream = new FileOutputStream(outputFile);
			exportLayer(targetStream, layerInfo);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to export layer to file " + fileName, e);
			return;
		}
		
	}
	
	@Override
	public void exportLayer(File outputFile, WMSLayerInfo layerInfo)
	{
		Validate.notNull(outputFile, "A file is required");
		
		try
		{
			FileOutputStream targetStream = new FileOutputStream(outputFile);
			exportLayer(targetStream, layerInfo);
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Unable to export layer to file " + outputFile.getAbsolutePath(), e);
			return;
		}
	}
	
	@Override
	public void exportLayer(OutputStream targetStream, WMSLayerInfo layerInfo)
	{
		Validate.notNull(targetStream, "A target output stream is required");
		Validate.notNull(layerInfo, "Layer information is required");
		
		AVList wmsParameters = WMSTiledImageLayer.wmsGetParamsFromCapsDoc((WMSCapabilities)layerInfo.getCaps(), layerInfo.getParams());
		Document document = WMSTiledImageLayer.createTiledImageLayerConfigDocument(wmsParameters);
		
		XMLUtil.saveDocumentToFormattedStream(document, targetStream);
	}
}
