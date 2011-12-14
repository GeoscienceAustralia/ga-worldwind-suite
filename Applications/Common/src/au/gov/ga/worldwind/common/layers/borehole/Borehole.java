package au.gov.ga.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.Position;

import java.util.List;

/**
 * Represents a single borehole in the {@link BoreholeLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Borehole
{
	/**
	 * @return List of samples associated with this borehole
	 */
	List<BoreholeSample> getSamples();

	/**
	 * @return This borehole's position
	 */
	Position getPosition();

	/**
	 * @return The display text associated with this borehole; eg to show as a
	 *         tooltip
	 */
	String getText();

	/**
	 * @return A URL string to a website that describes this borehole (null if
	 *         none)
	 */
	String getLink();
}
