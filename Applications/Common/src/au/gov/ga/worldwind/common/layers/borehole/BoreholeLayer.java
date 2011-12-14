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
}
