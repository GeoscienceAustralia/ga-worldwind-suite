package au.gov.ga.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.layers.Bounded;
import au.gov.ga.worldwind.common.util.Setupable;

/**
 * Interface for all Point layers. Point classes can extend the specific class
 * for the type of points to display (such as Markers or Icons), and simply need
 * to implement this interface.
 * 
 * @author Michael de Hoog
 */
public interface PointLayer extends AVList, Setupable, Bounded, Layer
{
	/**
	 * Add a point to this layer. Called by the {@link PointProvider}.
	 * 
	 * @param position
	 *            Point to add
	 * @param attributeValues
	 *            Attribute values for this point
	 */
	void addPoint(Position position, AVList attributeValues);

	/**
	 * Called by the {@link PointProvider} after all points have been loaded.
	 */
	void loadComplete();

	/**
	 * @return The download url for this point layer's data
	 * @throws MalformedURLException
	 */
	URL getUrl() throws MalformedURLException;

	/**
	 * @return The filename under which to store the downloaded data in the
	 *         cache
	 */
	String getDataCacheName();
}
