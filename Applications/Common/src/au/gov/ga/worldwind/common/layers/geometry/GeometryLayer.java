package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import au.gov.ga.worldwind.common.layers.Bounded;
import au.gov.ga.worldwind.common.layers.data.DataLayer;
import au.gov.ga.worldwind.common.util.Setupable;

/**
 * The interface for all geometry layers.
 * <p/>
 * A geometry layer is composed of multiple {@link Position}s, grouped into shapes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface GeometryLayer extends AVList, Setupable, Bounded, DataLayer
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
	 * Invoked when this layer's shape source has been loaded.
	 * <p/>
	 * Provides a hook for implementing classes to perform post-load processing.
	 */
	void loadComplete();
	
	/**
	 * Render the geometry in this layer
	 */
	void renderGeometry(DrawContext dc);
}
