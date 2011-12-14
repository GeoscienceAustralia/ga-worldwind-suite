package au.gov.ga.worldwind.common.layers.data;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

/**
 * Helper class for {@link DataLayer} subclass factories to use.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DataLayerFactory
{
	/**
	 * Call the standard {@link Layer} setters for values in the params AVList.
	 */
	public static void setLayerParams(Layer layer, AVList params)
	{
		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			layer.setName(s);

		Double d = (Double) params.getValue(AVKey.OPACITY);
		if (d != null)
			layer.setOpacity(d);

		d = (Double) params.getValue(AVKey.MAX_ACTIVE_ALTITUDE);
		if (d != null)
			layer.setMaxActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MIN_ACTIVE_ALTITUDE);
		if (d != null)
			layer.setMinActiveAltitude(d);

		Boolean b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
			layer.setNetworkRetrievalEnabled(b);

		Object o = params.getValue(AVKey.URL_CONNECT_TIMEOUT);
		if (o != null)
			layer.setValue(AVKey.URL_CONNECT_TIMEOUT, o);

		o = params.getValue(AVKey.URL_READ_TIMEOUT);
		if (o != null)
			layer.setValue(AVKey.URL_READ_TIMEOUT, o);

		o = params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (o != null)
			layer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, o);

		/*ScreenCredit sc = (ScreenCredit) params.getValue(AVKey.SCREEN_CREDIT);
		if (sc != null)
			layer.setScreenCredit(sc);*/

		layer.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());
	}
}
