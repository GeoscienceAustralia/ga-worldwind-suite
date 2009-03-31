package view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.OrbitViewLimits;

public interface RollOrbitViewLimits extends OrbitViewLimits
{
	Angle[] getRollLimits();

	void setRollLimits(Angle minAngle, Angle maxAngle);
}
