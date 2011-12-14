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
	List<BoreholeSample> getSamples();

	Position getPosition();

	String getText();

	String getLink();
}
