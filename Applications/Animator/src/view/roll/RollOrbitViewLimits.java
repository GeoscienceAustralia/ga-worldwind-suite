package view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

/**
 * @author Michael de Hoog
 */
public interface RollOrbitViewLimits extends OrbitViewLimits
{
	Angle[] getRollLimits();

	void setRollLimits(Angle minAngle, Angle maxAngle);
}
