package au.gov.ga.worldwind.common.layers.geometry;

import java.net.MalformedURLException;
import java.net.URL;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.common.layers.Bounded;
import au.gov.ga.worldwind.common.util.Setupable;

/**
 * The interface for all geometry layers.
 * <p/>
 * A geometry layer is composed of multiple {@link Position}s, grouped into shapes.
 */
public interface GeometryLayer extends AVList, Setupable, Bounded, Layer
{

	/**
	 * @return The shapes associated with this layer
	 */
	Iterable<? extends Shape> getShapes();
	
	/**
	 * Add the provided shape to this layer
	 */
	void addShape(Shape shape);
	
	/**
	 * @return The download url for this layer's shape data
	 */
	URL getShapeSourceUrl() throws MalformedURLException;

	/**
	 * @return The filename under which to store the downloaded data in the cache
	 */
	String getDataCacheName();
	
	/**
	 * Invoked when this layer's shape source has been loaded.
	 * <p/>
	 * Provides a hook for implementing classes to perform post-load processing.
	 */
	void loadComplete();
}
