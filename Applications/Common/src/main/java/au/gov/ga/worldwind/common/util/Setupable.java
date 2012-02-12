package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.WorldWindow;

/**
 * Represents an object that can be setup after the {@link WorldWindow} is
 * created and ready.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Setupable
{
	void setup(WorldWindow wwd);
}
