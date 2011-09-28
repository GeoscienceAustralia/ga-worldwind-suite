package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

/**
 * An interface for classes that want to receive WMS layers selected by the user
 */
public interface WmsLayerReceiver
{

	/**
	 * Receive the selected WMS layer
	 */
	void receive(WMSLayerInfo wmsLayer);
	
}
