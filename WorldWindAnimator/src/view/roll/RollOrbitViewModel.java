package view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.OrbitViewModel;

/**
 * @author Michael de Hoog
 */
public interface RollOrbitViewModel extends OrbitViewModel
{
	Angle getRoll();

	void setRoll(Angle roll);
}
