package au.gov.ga.worldwind.common.layers.borehole;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import au.gov.ga.worldwind.common.layers.Bounded;
import au.gov.ga.worldwind.common.layers.data.DataLayer;
import au.gov.ga.worldwind.common.util.Setupable;

/**
 * Layer used to visualise borehole data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface BoreholeLayer extends AVList, Bounded, DataLayer, Setupable
{
	/**
	 * Add a borehole sample to this layer. Called by the
	 * {@link BoreholeProvider}.
	 * 
	 * @param position
	 *            Borehole sample position
	 * @param attributeValues
	 *            Attribute values for this point
	 */
	void addBoreholeSample(Position position, AVList attributeValues);

	/**
	 * Called by the {@link BoreholeProvider} after all boreholes have been
	 * loaded.
	 */
	void loadComplete();

	/**
	 * @return The minimum distance a Borehole must be from the camera to render
	 *         it. Any borehole closer to the camera than this minimum distance
	 *         should be rendered. If null, there's no minimum distance.
	 */
	Double getMinimumDistance();
}
