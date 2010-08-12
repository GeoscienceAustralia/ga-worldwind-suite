package au.gov.ga.worldwind.animator.view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.orbit.OrbitView;

/**
 * @author Michael de Hoog
 */
public interface RollOrbitView extends OrbitView
{
	Angle getRoll();

	void setRoll(Angle roll);
}
