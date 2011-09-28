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
