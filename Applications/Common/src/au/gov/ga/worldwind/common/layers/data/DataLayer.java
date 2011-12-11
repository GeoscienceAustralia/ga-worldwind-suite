package au.gov.ga.worldwind.common.layers.data;

import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.util.Loader;

/**
 * {@link Layer} which reads it's data from a single URL, using a
 * {@link DataProvider} subclass to download it's data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface DataLayer extends Layer, Loader
{
	/**
	 * @return The download url for this layer's data
	 * @throws MalformedURLException
	 */
	URL getUrl() throws MalformedURLException;

	/**
	 * @return The filename under which to store the downloaded data in the
	 *         cache
	 */
	String getDataCacheName();
}
